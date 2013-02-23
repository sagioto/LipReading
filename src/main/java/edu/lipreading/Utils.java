package edu.lipreading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JProgressBar;

import javazoom.jl.player.Player;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.xml.XStream;
import au.com.bytecode.opencsv.CSVWriter;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;

import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.LinearStretchTimeNormalizer;

public class Utils {


	/**
	 * this function will download the file from the url into the filename specified
	 * in the current directory
	 * 
	 * @param urlToDownload
     * @param progressBar
	 * @throws IOException
	 */
	public static void get(String urlToDownload, JProgressBar progressBar) throws IOException{
		URL url = new URL(urlToDownload);
		InputStream in = url.openStream();
		String filename = getFileNameFromUrl(urlToDownload);
		int size = tryGetFileSize(url);
		OutputStream out = new FileOutputStream(filename);
		byte[] buf = new byte[4096];
		int len;
		DecimalFormat formatter = new DecimalFormat("###,###,###,###");
		System.out.println("downloading " + filename + " (" + formatter.format(size / 1024) +" kB) from " + url + ":");
		int i = 0;
		int totalLen = 0;
		while ((len = in.read(buf)) >= 0) {
			out.write(buf, 0, len);
			totalLen += len;
			i = (100 * totalLen) / size;
			if(progressBar != null)
			    progressBar.setValue(i);
			String print = "\r[                                                  ]" + i + "%";
			for (int j = 0; j < i; j += 2) {
				print = print.replaceFirst(" ", "*");                
			}
			System.out.print(print);
		}

		System.out.println();
		in.close();
		out.close();
	}
	
	public static void get(String urlToDownload) throws IOException{
	    get(urlToDownload, null);
	}

	public static boolean isCI() {
		return System.getProperty("user.name").equals("travis");
	}
	
	public static void textToSpeech(String text) throws Exception{
		text = text.replace(" ", "+");
		HttpClient httpclient = new DefaultHttpClient();
		String stringURL = "http://translate.google.com/translate_tts?ie=utf-8&tl=en&q=" + text;
		URLDecoder.decode(stringURL, "ISO-8859-1");
		HttpGet httpget = new HttpGet(stringURL);
		HttpResponse get = httpclient.execute(httpget);

		InputStream in = get.getEntity().getContent();
		Player player = new Player(in);
		player.play();

		in.close();
	}

	public static String getFileNameFromUrl(String urlToDownload) throws UnsupportedEncodingException {
		String[] split = urlToDownload.split("/");
		String fileName = split[split.length - 1];
		return URLDecoder.decode(fileName, "ISO-8859-1");
	}

	private static int tryGetFileSize(URL url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			return -1;
		} finally {
			conn.disconnect();
		}
	}

	public static String getFileName(String source) {
		String s = System.getProperty("file.separator");
		if(!s.equals("/"))
			s = "\\\\";
		String[] split = source.split(s);
		return split[split.length - 1];
	}

	public static List<Sample> getTrainingSetFromZip(String zipUrl) throws Exception {
		if(!new File(getFileNameFromUrl(zipUrl)).exists())
			Utils.get(zipUrl);
		ZipFile samplesZip = new ZipFile(getFileNameFromUrl(zipUrl));
		List<Sample> trainingSet = new Vector<Sample>();
		Enumeration<? extends ZipEntry> entries = samplesZip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			Sample read = (Sample) XStream.read(samplesZip.getInputStream(entry));
			trainingSet.add(read);
		}
		samplesZip.close();
		return trainingSet;
	}


	public static void dataSetToCSV(String zipFileInput, String outputFile) throws Exception {
		CSVWriter writer = new CSVWriter(new FileWriter(outputFile));
		List<Sample> trainingSetFromZip = Utils.getTrainingSetFromZip(zipFileInput);
		int instanceSize = (Constants.FRAMES_COUNT * Constants.POINT_COUNT * 2) + 1;
		String[] title = new String[instanceSize];
		title[0] = "word";
		for (int i = 1; i < instanceSize; i++) {
			title[i] = String.valueOf(i);
		}
		List<String[]> samplesStrings = new ArrayList<String[]>();
		samplesStrings.add(title);
		for (Sample sample : trainingSetFromZip) {
			sample = LipReading.normelize(sample, new CenterNormalizer(), new LinearStretchTimeNormalizer());
			samplesStrings.add(sample.toCSV());
		}
		writer.writeAll(samplesStrings);
		writer.close();
	}

	public static void matrixToCSV(double[][] matrix, String outputFile) throws Exception {
		CSVWriter writer = new CSVWriter(new FileWriter(outputFile));
		List<String[]> rows = new ArrayList<String[]>();
		for (double[] ds : matrix) {
			String[] row = new String[ds.length];
			for (int i = 0; i < ds.length; i++) {
				row[i] = String.valueOf(ds[i]);
			}
			rows.add(row);
		}
		writer.writeAll(rows);
		writer.close();
	}

	public static void dataSetToARFF(String zipFileInput, String outputFile) throws Exception {
		String csvFileName = outputFile.split("\\.")[0] + ".csv";
		dataSetToCSV(zipFileInput, csvFileName);
		CSVLoader csvLoader = new CSVLoader();
		ArffSaver arffSaver = new ArffSaver();
		File csvFile = new File(csvFileName);
		File arffFile = new File(outputFile);
		csvLoader.setFile(csvFile);
		arffSaver.setFile(arffFile);
		arffSaver.setStructure(csvLoader.getStructure());
		arffSaver.setInstances(csvLoader.getDataSet());
		arffSaver.writeBatch();
		csvFile.delete();
    }

	public static List<String> readFile(String resource){
		String string = convertStreamToString(ClassLoader.getSystemResourceAsStream(resource));
		return Arrays.asList(string.toLowerCase().split("\n"));
	}

	public static String convertStreamToString(InputStream is) {
		String ans = "";	
		Scanner s = new Scanner(is);
		s.useDelimiter("\\A");
		ans = s.hasNext() ? s.next() : "";
		s.close();
		return ans;	
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	public static CvScalar getCvScalar(String property) {
		String[] split = property.split(",");
		return new CvScalar(Double.valueOf(split[0]),
				Double.valueOf(split[1]),
				Double.valueOf(split[2]),
				Double.valueOf(split[3]));
	}

	public static int getMinIndex(double[] ds) {
		return getMinIndex(ds, true);
	}

	public static int getMinIndex(double[] ds, boolean includeZero) {
		int ans = 0;
		double min = Double.MAX_VALUE;
		for (int i = 0; i < ds.length; i++) {
			if(includeZero || ds[i] != 0){
				min = Math.min(min, ds[i]);
				if(min == ds[i])
					ans = i;
			}
		}
		return ans;
	}
	
	public static boolean isSourceFile(String source) {
        return null != source && !isSourceUrl(source);
    }

    public static boolean isSourceUrl(String source) {
        return null != source && source.contains("://");
    }

    public static void writeSampleToXML(String folderPath, String sampleName, Sample sample) throws Exception{
		File samplesDir = new File(folderPath);
		if (!samplesDir.exists()) 
			samplesDir.mkdirs();
    	File sampleFile = new File(samplesDir.getAbsolutePath()  + "/" + sampleName);
    	FileOutputStream fos = new FileOutputStream(sampleFile);
        XStream.write(sampleFile , sample);
        fos.close();
    }
}

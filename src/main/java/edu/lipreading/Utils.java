package edu.lipreading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.xml.XStream;
import au.com.bytecode.opencsv.CSVWriter;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.SimpleTimeNormalizer;

public class Utils {


	/**
	 * this function will download the file from the url into the filename specified
	 * in the current directory
	 * 
	 * @param urlToDownload
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void get(String urlToDownload) throws MalformedURLException, IOException{
		URL url = new URL(urlToDownload);
		InputStream in = url.openStream();
		String filename = getFileNameFromUrl(urlToDownload);
		int size = tryGetFileSize(url);
		OutputStream out = new FileOutputStream(filename);
		byte[] buf = new byte[4096];
		int len;
		DecimalFormat formatter = new DecimalFormat("###,###,###,###");
		System.out.println("downloading " + filename + " (" + formatter.format(size) +" Bytes) from " + url + ":");
		int i = 0;
		int totalLen = 0;
		while ((len = in.read(buf)) >= 0) {
			out.write(buf, 0, len);
			totalLen += len;
			i = (100 * totalLen) / size;
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

	public static boolean isCI() {
		return System.getProperty("user.name").equals("travis");
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
		Utils.get(zipUrl);
		ZipFile samplesZip = new ZipFile(Utils.getFileNameFromUrl(zipUrl));
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

	public static void dataSetToCSV(String zipFileInput, String outputFile) throws IOException, Exception {
		CSVWriter writer = new CSVWriter(new FileWriter(outputFile));
		List<Sample> trainingSetFromZip = Utils.getTrainingSetFromZip(zipFileInput);
		String[] title = new String[801];
		title[0] = "word";
		for (int i = 1; i < 801; i++) {
			title[i] = String.valueOf(i);
		}
		List<String[]> samplesStrings = new ArrayList<String[]>();
		samplesStrings.add(title);
		for (Sample sample : trainingSetFromZip) {
			sample = LipReading.normelize(sample, new CenterNormalizer(), new SimpleTimeNormalizer());
			samplesStrings.add(sample.toCSV());
		}
		writer.writeAll(samplesStrings);
		writer.close();
	}


	public static void dataSetToARFF(String zipFileInput, String outputFile) throws IOException, Exception {
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


}

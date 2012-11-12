package edu.lipreading;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;

public class Utils {
	
	
	/**
	 * this function will download the file from the url into the filename specified
	 * in the current directory
	 * 
	 * @param urlToDownload
	 * @param filename
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
		while ((len = in.read(buf)) > 0) {
			String print = "\r[                                                  ]" + (i * 2) + "%";
			out.write(buf, 0, len);
			totalLen += len;
			if(totalLen > i * (size / 50)){
				int j = 0;
				while( j < i){
					print = print.replaceFirst(" ", "*");
					j++;
				}
				System.out.print(print);
				i++;
			}
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
}

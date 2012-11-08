package edu.lipsreading.vision;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import com.googlecode.javacv.FrameGrabber;


public class LipReading {

	public static void main(String...args) throws Exception{
		String filename = null;
		FrameGrabber grabber = null;

		if (args.length == 0) {
			try{
				grabber = FrameGrabber.createDefault(0);
				grabber.start();
			}
			catch(Exception e){
				System.err.println("could not start camera.");
				System.exit(-1);				
			}
		}
		else {
			try{
				String[] split = args[0].split("/");
				filename = split[split.length - 1];
				get(args[0], filename);
				grabber = FrameGrabber.createDefault(filename);
			}
			catch (Exception e){
				System.err.println("could not open file: " + args[0]);
				System.exit(-1);								
			}
		}
		grabber.grab();

		System.exit(0);
	}

	public static void get(String urlToDownload, String filename) throws MalformedURLException, IOException{
		URL url = new URL(urlToDownload);
		InputStream in = url.openStream();
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

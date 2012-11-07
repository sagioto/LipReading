package edu.lipsreading.vision;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
		System.out.println("size: " + size);
		System.out.print("downloading " + filename + ":[");//          ]");
		int i = 0;
		int totallen = 0;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
			totallen += len;
			if(totallen > i * (size / 10)){
				System.out.print("*");
				i++;
			}
		}
		System.out.println("]");
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

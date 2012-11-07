package edu.lipsreading.vision;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;
import com.googlecode.javacv.cpp.opencv_highgui.CvVideoWriter;


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

	public static void get(String url, String filename) throws MalformedURLException, IOException{
		InputStream in = new URL(url).openStream();
		OutputStream out = new FileOutputStream(filename);
		byte[] buf = new byte[4096];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
}

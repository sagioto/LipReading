package edu.lipreading.vision;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.VideoInputFrameGrabber;

import edu.lipreading.Point;
import edu.lipreading.Utils;

public abstract class AbstractFeatureExtractor {

	protected FrameGrabber grabber;

	public List<List<Point>> extract(String source) throws Exception, MalformedURLException, IOException{
		grabber = getGrabber(source);
		grabber.start();
		
		List<List<Point>> trackPoints = getPoints(); 
		
		grabber.stop();
		return trackPoints;
	}

	abstract protected List<List<Point>> getPoints() throws Exception;

	private FrameGrabber getGrabber(String source)
			throws MalformedURLException, IOException, Exception {
		FrameGrabber grabber = null;
		if(isSourceUrl(source)){
			String[] split = source.split("/");
			String fileName = split[split.length - 1];
			Utils.get(source, fileName);
			grabber = FFmpegFrameGrabber.createDefault(fileName);
		}
		else if(isSourceFile(source)){
			grabber = FFmpegFrameGrabber.createDefault(source);
		}
		else{
			//try open the default camera
			grabber = VideoInputFrameGrabber.createDefault(0);
		}
		return grabber;
	}

	private boolean isSourceFile(String source) {
		return null != source && !isSourceUrl(source);
	}

	private boolean isSourceUrl(String source) {
		return null != source && source.contains("://");
	}
}

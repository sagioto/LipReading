package edu.lipreading.vision;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;

import edu.lipreading.Sample;
import edu.lipreading.Utils;

public abstract class AbstractFeatureExtractor {

	protected FrameGrabber grabber;
	protected static String sampleName;
	private boolean utput = false;
	
	public Sample extract(String source) throws Exception {
		grabber = getGrabber(source);
		grabber.start();
		
		Sample sample = getPoints(); 
		
		grabber.stop();
		return sample;
	}

	abstract protected Sample getPoints() throws Exception, InterruptedException, ExecutionException;

	private FrameGrabber getGrabber(String source)
			throws MalformedURLException, IOException, Exception {
		FrameGrabber grabber = null;
		if(isSourceUrl(source)){		
			Utils.get(source);
			sampleName = Utils.getFileNameFromUrl(source);
			grabber = FFmpegFrameGrabber.createDefault(sampleName);
		}
		else if(isSourceFile(source)){
			sampleName = Utils.getFileName(source);
			grabber = FFmpegFrameGrabber.createDefault(source);
		}
		else{
			//try open the default camera
			grabber = VideoInputFrameGrabber.createDefault(0);
			sampleName = "liveCam";
		}
		return grabber;
	}

	private boolean isSourceFile(String source) {
		return null != source && !isSourceUrl(source);
	}

	private boolean isSourceUrl(String source) {
		return null != source && source.contains("://");
	}

	public boolean isOutput() {
		return utput;
	}

	public void setOutput(boolean shouldOutput) {
		this.utput = shouldOutput;
	}
}

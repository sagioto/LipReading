package edu.lipreading.vision;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;

import edu.lipreading.Sample;
import edu.lipreading.Utils;

public abstract class AbstractFeatureExtractor {

	protected FrameGrabber grabber;
	private Sample sample;
	private boolean output = false;
	private boolean gui = true;
	
	
	public Sample extract(String source) throws Exception {
		grabber = getGrabber(source);
		grabber.start();
		
		Sample sample = getPoints(); 
		
		grabber.stop();
		return sample;
	}

	abstract protected Sample getPoints() throws Exception, InterruptedException, ExecutionException;

	public FrameGrabber getGrabber(String source)
			throws MalformedURLException, IOException, Exception {
		FrameGrabber grabber = null;
		String sampleName;
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
			sampleName = "web cam " + new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());;
		}
		setSample(new Sample(sampleName));
		return grabber;
	}

	private boolean isSourceFile(String source) {
		return null != source && !isSourceUrl(source);
	}

	private boolean isSourceUrl(String source) {
		return null != source && source.contains("://");
	}
	
	public Sample getSample() {
		return sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	public boolean isGui() {
		return this.gui && !Utils.isCI();
	}

	public void setGui(boolean gui) {
		this.gui = gui;
	}
	
	public boolean isOutput() {
		return output;
	}

	public void setOutput(boolean shouldOutput) {
		this.output = shouldOutput;
	}
	
}

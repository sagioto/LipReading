package edu.lipreading.vision;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

	public FrameGrabber getGrabber(String source) throws Exception {
		String sampleName;
		if(Utils.isSourceUrl(source)){
			Utils.get(source);
			sampleName = Utils.getFileNameFromUrl(source);
			grabber = FFmpegFrameGrabber.createDefault(sampleName);
		}
		else if(Utils.isSourceFile(source)){
			sampleName = Utils.getFileName(source);
			grabber = FFmpegFrameGrabber.createDefault(source);
		}
		else{
			//try open the default camera
			grabber = VideoInputFrameGrabber.createDefault(0);
			sampleName = "web cam " + new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
		}
		setSample(new Sample(sampleName));
		return grabber;
	}


	protected Sample getPoints() throws Exception {
		IplImage grabbed;
		CanvasFrame frame = null;
		FrameRecorder recorder = null;

		if(isGui()){
			frame = new CanvasFrame(getSample().getId(), CanvasFrame.getDefaultGamma()/grabber.getGamma());
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			if(isOutput()){
				String[] sampleNameSplit = getSample().getId().split("\\.");
				recorder = FFmpegFrameRecorder.createDefault(sampleNameSplit[0] + "-output." + sampleNameSplit[1],grabber.getImageWidth(), grabber.getImageHeight());
				recorder.setFrameRate(grabber.getFrameRate());
				recorder.start();
			}
		}

		while((grabbed = grabber.grab()) != null){
			List<Integer> frameCoordinates = getPoints(grabbed);

			if(isGui()){
				paintCoordinates(grabbed, frameCoordinates);
                frame.showImage(grabbed);
				if(isOutput()){
					recorder.record(grabbed);
				}
			}
			getSample().getMatrix().add(frameCoordinates);
		}
		if(isGui()){
			frame.dispose();
			if(isOutput()){
				recorder.stop();
			}
		}
		return getSample();
	}

	abstract public void paintCoordinates(IplImage grabbed, List<Integer> frameCoordinates);

	abstract public List<Integer> getPoints(IplImage grabbed) throws Exception;

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

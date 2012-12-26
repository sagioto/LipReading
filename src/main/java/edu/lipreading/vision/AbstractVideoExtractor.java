package edu.lipreading.vision;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Sample;

public abstract class AbstractVideoExtractor {
	
	protected Sample sample;
	
	public void initSample(String sampleName)
	{
		sample = new Sample(sampleName);
	}
	
	public Sample getSample()
	{
		return sample;
	}
	
	public abstract void savePoints(IplImage img) throws Exception;
	
	abstract protected List<Integer> getPoints(IplImage img) throws Exception, InterruptedException, ExecutionException;
	
}

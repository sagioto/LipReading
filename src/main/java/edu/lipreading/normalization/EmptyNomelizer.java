package edu.lipreading.normalization;

import java.util.List;

import edu.lipreading.Point;

public class EmptyNomelizer implements Normelizer{

	@Override
	public List<List<Point>> normelize(List<List<Point>> data) {
		return data;
	}
	
}

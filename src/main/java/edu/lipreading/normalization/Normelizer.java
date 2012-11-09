package edu.lipreading.normalization;

import java.util.List;

import edu.lipreading.Point;

public interface Normelizer {
	public List<List<Point>> normelize(List<List<Point>> data);
}

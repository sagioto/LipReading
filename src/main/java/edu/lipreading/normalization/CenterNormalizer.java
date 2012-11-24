package edu.lipreading.normalization;

import java.util.List;

import edu.lipreading.Sample;

public class CenterNormalizer implements Normalizer{

	protected static final int X_INDEX = 0;
	protected static final int Y_INDEX = 1;
	
	@Override
	public Sample normelize(Sample sample) {
		for (List<Integer> vector : sample.getMatrix()) {
			int[] center = getCenter(vector);
			for (int i = 0; i < vector.size(); i++) {
				if(i % 2 == 0)
					vector.set(i, vector.get(i)- center[X_INDEX]);
				else
					vector.set(i, vector.get(i)- center[Y_INDEX]);
			}
		}
		
		return sample;
	}

	protected int[] getCenter(List<Integer> vector) {
		int[] center = {0,0};
		for (int i = 0; i < vector.size(); i++) {
			if(i % 2 == 0)
				center[X_INDEX] += vector.get(i);
			else
				center[Y_INDEX] += vector.get(i);
		}
		center[X_INDEX] /= (vector.size() / 2);
		center[Y_INDEX] /= (vector.size() / 2);
		return center;
	}

}

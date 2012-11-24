package edu.lipreading.normalization;

import java.util.List;

import edu.lipreading.Sample;

public class StretchNormelizer extends CenterNormelizer {
	
	public static final double STRECH_COEFFICIENT = 1000;
	
	@Override
	public Sample normelize(Sample sample) {
		for (List<Integer> vector : sample.getMatrix()) {
			int[] center = getCenter(vector);
			double factor = STRECH_COEFFICIENT / center[X_INDEX];
			for (int i = 0; i < vector.size(); i++) {
					vector.set(i, (int)(vector.get(i) * factor));
			}
			
		}
		return /*super.normelize(*/sample/*)*/;
	}

}

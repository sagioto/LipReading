package edu.lipreading.normalization;

import edu.lipreading.Sample;

public interface Normalizer {
    /**
     *  Gets a Sample and returns normalized Sample
     * @param sample
     * @return normalized Sample
     * @precondition sample != null
     * @postcondition sample`s normalized data contains the same needed classification data. In other words - normalization process should not damage Sample`s data
     */
	public Sample normalize(Sample sample);
}

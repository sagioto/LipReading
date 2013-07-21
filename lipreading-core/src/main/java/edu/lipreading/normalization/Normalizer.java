package edu.lipreading.normalization;

import edu.lipreading.Sample;

/**
 * This interface should be implemented by classes that perform some sort of normalization on samples.
 * Normalizers should usually keep the original sample intact, and manipulate its data in a non-corruptive way,
 * although this is eventually up to the implementing class to decide.
 */
public interface Normalizer {
    /**
     * Gets a Sample and returns a normalized Sample as defined by the normalizer
     * @param sample
     * @return normalized Sample
     * @precondition sample != null
     * @postcondition sample`s normalized data contains the same needed classification data. In other words - normalization process should not damage Sample`s data
     */
	public Sample normalize(Sample sample);
}

package edu.lipreading.normalization;

import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;

import edu.lipreading.Sample;
import edu.lipreading.normalization.RotationNormalizer;

public class RotationNormalizerTest {

	@Test
	public void simpleRotationTest() {
		List<Integer> v1 = new Vector<Integer>();
		List<Integer> v2 = new Vector<Integer>();
		v1.add(-6);
		v1.add(3);
		v2.add(6);
		v2.add(-3);
		List<List<Integer>> matrix = new Vector<List<Integer>>();
		matrix.add(v1);
		matrix.add(v2);

		Sample sample = new Sample("Rotation Normalizer Test", matrix);
		RotationNormalizer rn = new RotationNormalizer();
		Sample normalizedSample = rn.normalize(sample);

		Assert.assertTrue("A point was not rotated correctly",
				(normalizedSample.getMatrix().get(0).get(0) == -7)
						&& (normalizedSample.getMatrix().get(0).get(1) == 0));
		Assert.assertTrue("A point was not rotated correctly",
				(normalizedSample.getMatrix().get(1).get(0) == 7)
						&& (normalizedSample.getMatrix().get(1).get(1) == 0));
	}

	@Test
	public void complexAxisRotationTest() {
		List<Integer> v1 = new Vector<Integer>();
		List<Integer> v2 = new Vector<Integer>();
		v1.add(6);
		v1.add(3);
		v2.add(14);
		v2.add(9);
		List<List<Integer>> matrix = new Vector<List<Integer>>();
		matrix.add(v1);
		matrix.add(v2);

		Sample sample = new Sample("Rotation Normalizer Test", matrix);
		RotationNormalizer rn = new RotationNormalizer();
		Sample normalizedSample = rn.normalize(sample);

		Assert.assertTrue("A point was not rotated correctly",
				(normalizedSample.getMatrix().get(0).get(0) == 5)
						&& (normalizedSample.getMatrix().get(0).get(1) == 6));
		Assert.assertTrue("A point was not rotated correctly",
				(normalizedSample.getMatrix().get(1).get(0) == 15)
						&& (normalizedSample.getMatrix().get(1).get(1) == 6));
	}
}

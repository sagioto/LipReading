package edu.lipreading;

import java.util.List;

/**
 * Signelton class of training set
 * @author Dor Leitman
 *
 */

public class TrainingSet {

	private TrainingSet(){}

	public static List<Sample> trainingSet = null;

	private static final Object monitor = new Object();

	public static List<Sample> get() {
		if (trainingSet == null){
			synchronized (monitor) {
				if (trainingSet == null) try {
                    trainingSet = Utils.getTrainingSetFromZip(Constants.DEFAULT_TRAINING_SET_ZIP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
			}
		}
		return trainingSet;
	}


}

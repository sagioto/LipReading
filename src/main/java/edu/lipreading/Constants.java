package edu.lipreading;

import java.util.List;


public class Constants {
	public static final String MPC_MODEL_URL = "https://dl.dropbox.com/u/8720454/weka/mp-10folds.model";
	public static final String DEFAULT_TRAINING_SET_ZIP_URL = "https://dl.dropbox.com/u/8720454/xmls/xmls.zip";
	public static final String DEFAULT_ARFF_FILE = "https://dl.dropbox.com/u/8720454/weka/dataset2.arff";
	public static final String DEFAULT_VOCABULARY_FILE = "primitive.txt";
	public static final List<String> VOCABULARY = Utils.readFile(DEFAULT_VOCABULARY_FILE);
}

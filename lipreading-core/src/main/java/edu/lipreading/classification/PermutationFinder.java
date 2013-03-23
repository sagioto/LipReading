/**
 * 
 */
package edu.lipreading.classification;

import edu.lipreading.Constants;

import java.util.ArrayList;

/**
 * @author Dor Leitman
 *
 */
public class PermutationFinder {
	public static int counter;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		counter = 0;
		/*
		for (int i=2; i<=Constants.VOCABULARY.size(); i++){
			permute(i, 0, new ArrayList<String>());
		}
		*/
		permute(4, 0, new ArrayList<String>()); // TODO Delete
		System.out.println("Number of permutations: " + counter);
		
	}
	
	
	
	public static void permute(int lengthOfSet, int wordIndexInVocabulary, ArrayList<String> currentSet){
		if (wordIndexInVocabulary > Constants.VOCABULARY.size()){
			return;
		}
		if (currentSet.size() == lengthOfSet){
			for (String string : currentSet) {
				System.out.print(string + " ");
			}
			System.out.println();
			counter++;
			//TODO Classify & save rate
		}
		else{
			if (wordIndexInVocabulary < Constants.VOCABULARY.size()){
				@SuppressWarnings("unchecked")
				ArrayList<String> newSet = (ArrayList<String>) currentSet.clone();
				newSet.add(Constants.VOCABULARY.get(wordIndexInVocabulary));
				permute(lengthOfSet, wordIndexInVocabulary + 1, newSet);
			}
			permute(lengthOfSet, wordIndexInVocabulary + 1, currentSet);
			
		}
		
	}

}

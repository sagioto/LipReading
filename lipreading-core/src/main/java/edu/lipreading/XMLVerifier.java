package edu.lipreading;

import weka.core.xml.XStream;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class XMLVerifier {

	/**
	 * This tool scans a directory that is given as an argument for XML files
	 * that represent recorded Samples. It builds a Sample from each file and
	 * scans for <0,0> entries in the vectors, and presents some statistics.
	 *
     * CAUTION!!!!!!!! this tool can also update Sample XMLs between LipReading versions, and may corrupt your files.
	 * @param args
	 *            should contain one argument that is a directory with XML
	 *            files.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("please supply location of XML files to verify");
			return;
		}


        // WARNING!!!! this flag will convert your xmls to new xmls. Please make sure you want to do this.
		boolean convert = false;

		int converted = 0;
		String xmlsLocation = args[0];

		List<String> invalidXMLs = new ArrayList<String>();
		int numOfInconsistencies = 0;
		int biggestInconsistency = 0;
		int numOfFiles = 0;
		int numOfInvalidXMLs = 0;
		int consistentSamples = 0;
		String biggestInconsistentSample = "";

		File xmlPath = new File(xmlsLocation);
		File convertDir = new File(xmlsLocation + "\\convert");
		convertDir.mkdirs();

		for (File xml : xmlPath.listFiles()) {
			if (xml.getName().endsWith(".xml")) {
                Sample samp;
                try {
                    samp = (Sample) XStream.read(xml);
                } catch (Exception e) {
                    invalidXMLs.add(xml.getName());
                    numOfInvalidXMLs++;
                    continue;
                }
					numOfFiles++;
					int sampInconsistency = 0;
					for (List<Integer> vector : samp.getMatrix()) {
						for (int coord : vector) {
							if (coord == 0) {
								sampInconsistency++;
								numOfInconsistencies++;
							}
						}
					}
					if(convert) {
						samp = swapLeftAndRight(samp);
						try {
						XStream.write(convertDir.getAbsolutePath() + "\\" + xml.getName(), samp);
						converted++;
						} catch(Exception e) {
							System.out.println("Could not convert file " + xml.getName());
						}
					}
					if (biggestInconsistency == 0
							|| sampInconsistency > biggestInconsistency) {
						biggestInconsistency = sampInconsistency;
						biggestInconsistentSample = xml.getName();
					}
					if(sampInconsistency == 0) {
						consistentSamples++;
					}
			}
		}

		numOfInconsistencies = numOfInconsistencies / 2;
		biggestInconsistency = biggestInconsistency / 2;

		for(String badXML : invalidXMLs) {
			System.out.println("Error parsing XML file: "
					+ badXML);
		}
		System.out.println("Number of XMLs scanned: " + numOfFiles);
		System.out.println("Number of Invalid XML Files: " + numOfInvalidXMLs);
		System.out.println("Number of Inconsistencies found: "
				+ numOfInconsistencies);
		System.out.println("Number of samples without inconsistencies: " + consistentSamples);
		System.out.println("Sample '" + biggestInconsistentSample
				+ "' with most inconsistencies contained: "
				+ biggestInconsistency);
		if(convert) {
			System.out.println("Successfully converted " + converted + " files");
		}
	}

    public static Sample swapLeftAndRight(Sample beforeSwap) {
        List<List<Integer>> oldMatrix = beforeSwap.getMatrix();
        List<Integer> oldVec = oldMatrix.get(0);
        if(oldVec.get(0) > oldVec.get(2)) {
            return beforeSwap; //x value of right is larger than x value of left, sample is fine.
        }

        //build the new matrix
        List<List<Integer>> newMatrix = new Vector<List<Integer>>();

        for(int i=0; i<oldMatrix.size(); i++) {
            oldVec = oldMatrix.get(i);
            List<Integer> newVec = new Vector<Integer>();

            //second point is the actual right
            int xRight = oldVec.get(2);
            int yRight = oldVec.get(3);
            //first point is the actual left
            int xLeft = oldVec.get(0);
            int yLeft = oldVec.get(1);
            //up and down positions are fine
            int xUp = oldVec.get(4);
            int yUp = oldVec.get(5);
            int xDown = oldVec.get(6);
            int yDown = oldVec.get(7);

            //build the new vector
            newVec.add(0, xRight);
            newVec.add(1, yRight);
            newVec.add(2, xLeft);
            newVec.add(3, yLeft);
            newVec.add(4, xUp);
            newVec.add(5, yUp);
            newVec.add(6, xDown);
            newVec.add(7, yDown);

            //insert the vector to the new matrix
            newMatrix.add(i, newVec);
        }

        Sample afterSwap = new Sample();

        //Copy all primitive fields
        afterSwap.setHeight(beforeSwap.getHeight());
        afterSwap.setLabel(beforeSwap.getLabel());
        afterSwap.setId(beforeSwap.getId());
        afterSwap.setOriginalMatrixSize(beforeSwap.getOriginalMatrixSize());
        afterSwap.setWidth(beforeSwap.getWidth());

        //set the matrix in the new sample
        afterSwap.setMatrix(newMatrix);

        //return the modified sample
        return afterSwap;
    }

    public static Sample swapAllVectors(Sample beforeSwap) {
        Sample afterSwap = new Sample();

        //Copy all primitive fields
        afterSwap.setHeight(beforeSwap.getHeight());
        afterSwap.setLabel(beforeSwap.getLabel());
        afterSwap.setId(beforeSwap.getId());
        afterSwap.setOriginalMatrixSize(beforeSwap.getOriginalMatrixSize());
        afterSwap.setWidth(beforeSwap.getWidth());

        List<List<Integer>> oldMatrix = beforeSwap.getMatrix();
        List<List<Integer>> newMatrix = new Vector<List<Integer>>();

        for(int i=0; i<oldMatrix.size(); i++) {
            List<Integer> oldVec = oldMatrix.get(i);
            List<Integer> newVec = new Vector<Integer>();

            //each vec holds 8 points that should be swapped together
            //extract the 4 points from the old sample
            int xUp = oldVec.get(0);
            int yUp = oldVec.get(1);
            int xDown = oldVec.get(2);
            int yDown = oldVec.get(3);
            int xLeft = oldVec.get(4);
            int yLeft = oldVec.get(5);
            int xRight = oldVec.get(6);
            int yRight = oldVec.get(7);

            //build the new vector from the extracted points.
            newVec.add(0, xRight);
            newVec.add(1, yRight);
            newVec.add(2, xLeft);
            newVec.add(3, yLeft);
            newVec.add(4, xUp);
            newVec.add(5, yUp);
            newVec.add(6, xDown);
            newVec.add(7, yDown);

            //set the vector in the new matrix
            newMatrix.add(i, newVec);

        }
        //set the matrix in the new sample
        afterSwap.setMatrix(newMatrix);

        //return the modified sample
        return afterSwap;
    }

}

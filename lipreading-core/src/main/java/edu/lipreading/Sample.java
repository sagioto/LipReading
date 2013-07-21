package edu.lipreading;

import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * A representation of a lips video sample.
 * The main features are the points from each frame, represented by a Matrix which is a vector of vectors,
 * where each line in the matrix represents one frame.
 * Each frame vector is 8 integers, ordered as following: (xRight, yRight, xLeft, yLeft, xUpper, yUpper, xLower, yLower)
 */
public class Sample {

    private String id;
    private List<List<Integer>> matrix;
    private int width;
    private int height;
    private String label;
    private int originalMatrixSize;
    private Point leftEye;
    private Point rightEye;

    public Sample(String id, List<List<Integer>> matrix) {
        super();
        this.id = id;
        this.matrix = matrix;
    }

    public Sample(String id) {
        super();
        this.id = id;
        this.matrix = new Vector<List<Integer>>();
    }

    public Sample() {
        super();
        this.matrix = new Vector<List<Integer>>();
    }

    public String getId() {
        return id;
    }

    public Sample setId(String id) {
        this.id = id;
        return this;
    }

    public List<List<Integer>> getMatrix() {
        return matrix;
    }

    public Sample setMatrix(List<List<Integer>> matrix) {
        this.matrix = matrix;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public Sample setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Sample setHeight(int height) {
        this.height = height;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Sample setLabel(String label) {
        this.label = label;
        return this;
    }

    public int getOriginalMatrixSize() {
        return originalMatrixSize;
    }

    public void setOriginalMatrixSize(int originalMatrixSize) {
        this.originalMatrixSize = originalMatrixSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sample other = (Sample) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equalsIgnoreCase(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Sample [id=" + id + ", matrix=" + matrix + "]";
    }

    public String[] toCSV() {
        if (this.getMatrix() != null && !this.getMatrix().isEmpty()
                && this.getMatrix().get(0) != null) {
            int size = this.getMatrix().size() * this.getMatrix().get(0).size() + Constants.SAMPLE_ROW_SHIFT;
            String[] ans = new String[size];
            ans[0] = this.getLabel();
            ans[1] = String.valueOf(this.getOriginalMatrixSize());
            ans[2] = String.valueOf(this.getWidth());
            ans[3] = String.valueOf(this.getHeight());
            for (int i = 0; i < this.getMatrix().size(); i++) {
                for (int j = 0; j < this.getMatrix().get(0).size(); j++) {
                    try {
                        ans[Constants.SAMPLE_ROW_SHIFT + (i * this.getMatrix().get(0).size()) + j] =
                                String.valueOf(this.getMatrix().get(i).get(j));
                    } catch (RuntimeException e) {
                        System.out.print(this.getId());
                    }
                }
            }
            return ans;
        }
        return null;
    }

    public Point getLeftEye() {
        return leftEye;
    }

    public void setLeftEye(Point leftEye) {
        this.leftEye = leftEye;
    }

    public Point getRightEye() {
        return rightEye;
    }

    public void setRightEye(Point rightEye) {
        this.rightEye = rightEye;
    }
}

package edu.lipreading;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: Dagan
 * Date: 26/03/13
 * Time: 23:59
 */
@XmlRootElement
public class SamplePacket {
    private String id;
    private int[][] matrix;
    private int width;
    private int height;
    private String label;
    private int originalMatrixSize;

    public SamplePacket() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getOriginalMatrixSize() {
        return originalMatrixSize;
    }

    public void setOriginalMatrixSize(int originalMatrixSize) {
        this.originalMatrixSize = originalMatrixSize;
    }
}

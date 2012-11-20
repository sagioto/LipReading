package edu.lipreading;

import java.util.List;
import java.util.Vector;

public class Sample {
	private String id;
	private List<List<Integer>> matrix;
	
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

	@Override
	public String toString() {
		return "Sample [id=" + id + ", matrix=" + matrix + "]";
	}
	
	
}

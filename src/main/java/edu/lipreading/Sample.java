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
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Sample [id=" + id + ", matrix=" + matrix + "]";
	}
	
	
}

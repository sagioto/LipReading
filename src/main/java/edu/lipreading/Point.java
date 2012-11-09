package edu.lipreading;

public class Point {
	private short x;
	private short y;
	
	public Point() {
		super();
	}

	public Point(short x, short y) {
		super();
		this.x = x;
		this.y = y;
	}

	public short getX() {
		return x;
	}

	public Point setX(short x) {
		this.x = x;
		return this;
	}

	public short getY() {
		return y;
	}

	public Point setY(short y) {
		this.y = y;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		Point other = (Point) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	
	
}

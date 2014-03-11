package ckling.geometry3d;

public class Point {

	public double x;
	public double y;
	public double z;
	//optional id of a point
	public int id = -1;

	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point(double x, double y, double z, int id) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.id = id;
	}
	
}

package ckling.geometry3d;

public class Triangle {

	public Point[] vertices;

	public Triangle(double x1, double y1, double z1,
			double x2, double y2, double z2,
			double x3, double y3, double z3) {
		vertices = new Point[3];
		vertices[0] = new Point(x1,y1,z1);
		vertices[1] = new Point(x2,y2,z2);
		vertices[2] = new Point(x3,y3,z3);
	}
	
	public Triangle(Point p1,Point p2,Point p3) {
		vertices = new Point[3];
		vertices[0] = p1;
		vertices[1] = p2;
		vertices[2] = p3;
	}

	public Triangle(Point[] points) {
		if (points.length==3) {
			this.vertices = points;
		}
		else {
			System.err.print("Triangle has less than three points");
		}
	}
	
	public int seen (Point p) {

		double[][] m = new double[4][4];

		//first columns are the triangle points
		for (int i=0;i<3;i++) {
			m[0][i] = vertices[i].x;
			m[1][i] = vertices[i].y;
			m[2][i] = vertices[i].z;
		}
		
		//last column is the point
		m[0][3] = p.x;
		m[1][3] = p.y;
		m[2][3] = p.z;
		
		//last row is set to 1
		for (int i=0;i<m[0].length;i++) {
			m[3][i]=1;
		}
		
		
		double determinant = m[0][3] * m[1][2] * m[2][1] * m[3][0] - m[0][2] * m[1][3] * m[2][1] * m[3][0]-
				m[0][3] * m[1][1] * m[2][2] * m[3][0]+m[0][1] * m[1][3] * m[2][2] * m[3][0]+
				m[0][2] * m[1][1] * m[2][3] * m[3][0]-m[0][1] * m[1][2] * m[2][3] * m[3][0]-
				m[0][3] * m[1][2] * m[2][0] * m[3][1]+m[0][2] * m[1][3] * m[2][0] * m[3][1]+
				m[0][3] * m[1][0] * m[2][2] * m[3][1]-m[0][0] * m[1][3] * m[2][2] * m[3][1]-
				m[0][2] * m[1][0] * m[2][3] * m[3][1]+m[0][0] * m[1][2] * m[2][3] * m[3][1]+
				m[0][3] * m[1][1] * m[2][0] * m[3][2]-m[0][1] * m[1][3] * m[2][0] * m[3][2]-
				m[0][3] * m[1][0] * m[2][1] * m[3][2]+m[0][0] * m[1][3] * m[2][1] * m[3][2]+
				m[0][1] * m[1][0] * m[2][3] * m[3][2]-m[0][0] * m[1][1] * m[2][3] * m[3][2]-
				m[0][2] * m[1][1] * m[2][0] * m[3][3]+m[0][1] * m[1][2] * m[2][0] * m[3][3]+
				m[0][2] * m[1][0] * m[2][1] * m[3][3]-m[0][0] * m[1][2] * m[2][1] * m[3][3]-
				m[0][1] * m[1][0] * m[2][2] * m[3][3]+m[0][0] * m[1][1] * m[2][2] * m[3][3];

		//System.out.println(determinant);
		
		if (determinant>0) return 1;
		if (determinant<0) return -1;

		return 0;

	}
	
	public int contains(Point p) {
		if (vertices[0]==p) return 0;
		if (vertices[1]==p) return 1;
		if (vertices[2]==p) return 2;
		
		return -1;
	}
	
	
	public int contains(Point p1, Point p2) {
		if (vertices[0]==p1) {
			if (vertices[1] == p2 || vertices[2]==p2) return 0;
		}
		if (vertices[1]==p1) {
			if (vertices[0] == p2 || vertices[2]==p2) return 1;
		}
		if (vertices[2]==p1) {
			if (vertices[0] == p2 || vertices[1]==p2) return 2;
		}
			
		return -1;
	}
	
	//get point after this point, counter clockwise
	public Point after(Point p) {
		if (vertices[0]==p) return vertices[1];
		if (vertices[1]==p) return vertices[2];
		if (vertices[2]==p) return vertices[0];
		return null;
	}
	

}

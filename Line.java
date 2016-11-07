import javax.vecmath.Vector3d;
public class Line {

	/*
	Long oLat;
	Long dLat;
	Long oLng;
	Long dLng;
	*/
	Point o;
	Point d;
	
	public static Double earthRadius = 6371.0;
	
	// close to zero threshold
	final static  double eps = 1E-10;


	public Long getdLat() { return d.getLatE6(); }
	public Long getdLng() { return d.getLngE6(); }
	public void setdLat(Long l) { d.setLat(l); }
	public void setdLng(Long l) { d.setLng(l); }

	public Long getoLat() { return o.getLatE6(); }
	public Long getoLng() { return o.getLngE6(); }
	
	public Double getoLatAsDouble() { return new Double(o.getLatE6()); }
	public Double getoLngAsDouble() { return new Double(o.getLngE6()); }
	public Double getdLatAsDouble() { return new Double(d.getLatE6()); }
	public Double getdLngAsDouble() { return new Double(d.getLngE6()); }

	public Vector3d getoVect() { return new Vector3d(getoX(),getoY(),getoZ()); }
	public Vector3d getdVect() { return new Vector3d(getdX(),getdY(),getdZ()); }
	
	public double getoX() { return Math.cos(Math.toRadians(o.getLat())) * Math.cos(Math.toRadians(o.getLng())); }
	public double getoY() { return Math.cos(Math.toRadians(o.getLat())) * Math.sin(Math.toRadians(o.getLng())); }
	public double getoZ() { return Math.sin(Math.toRadians(o.getLat())); }

	public double getdX() { return Math.cos(Math.toRadians(d.getLat())) * Math.cos(Math.toRadians(d.getLng())); }
	public double getdY() { return Math.cos(Math.toRadians(d.getLat())) * Math.sin(Math.toRadians(d.getLng())); }
	public double getdZ() { return Math.sin(Math.toRadians(d.getLat())); }

	
	public void setoLat(Long l) { o.setLat(l); }
	public void setoLng(Long l) { o.setLng(l); }

	public Point getD() { return d; }
	public Point getO() { return o; }

	public Line (Point d, Point o) 
	{
		this.d = new Point(d);
		this.o = new Point(o);
	}

	/*
	public Line (Portal d, Portal o) {
		this(d.getPointE6(), o.getPointE6());
	}
	*/
	public Line (Long dla,Long dlo, Long ola,Long olo)
	{
		this.d = new Point(dla,dlo);
		this.o = new Point(ola,olo);
	}
	
	public boolean equals(Line l) 
	{
	
		return (this.getO().equals(l.getO()) && this.getD().equals(l.getD())) || (this.getO().equals(l.getD()) && this.getD().equals(l.getO()));
/*
		return ((l.getdLat().equals(getdLat())) && (l.getoLat().equals(getoLat())) && (l.getdLng().equals(getdLng())) && (l.getoLng().equals(getoLng())) ||
				(l.getdLat().equals(getoLat())) && (l.getoLat().equals(getdLat())) && (l.getdLng().equals(getoLng())) && (l.getoLng().equals(getdLng())));
*/
	}
	
	public boolean equals(Line[] lines)
	{
		for (Line line : lines)
		{
			if (equals(line)) { return true; }
		}
		
		return false;
	}
	
	
	public int greaterCircleIntersectType (Line l)
	{

		Vector3d p0 = this.getoVect();
		Vector3d p1 = this.getdVect();
		Vector3d p2 = l.getoVect();
		Vector3d p3 = l.getdVect();

		Vector3d V = new Vector3d();
		V.cross(p0,p1);
		V.normalize();

		Vector3d U = new Vector3d();
		U.cross(p2,p3);
		U.normalize();

		Vector3d D = new Vector3d();
		D.cross( V, U );
		if (D.length() == 0)
			return 0; // equal
		D.normalize();
		
				
		Vector3d S1 = new Vector3d();
		Vector3d S2 = new Vector3d();
		Vector3d S3 = new Vector3d();
		Vector3d S4 = new Vector3d();

		S1.cross(p0,V);
		S2.cross(p1,V);
		S3.cross(p2,U);
		S4.cross(p3,U);

		double s0 = -S1.dot(D);
		double s1 = S2.dot(D);
		double s2 = -S3.dot(D);
		double s3 = S4.dot(D);

		//System.out.println("Signs: " + s0 + " " + s1 + " " + s2 + " " + s3 );

		int count=0,zero=0;

		if (Math.abs(s0) < eps) 
			zero++; 
		else 
			count += Math.signum(s0);
		if (Math.abs(s1) < eps) 
			zero++; 	
		else 
			count += Math.signum(s1);
		if (Math.abs(s2) < eps) 
			zero++; 
		else 
			count += Math.signum(s2);

		if (Math.abs(s3) < eps) 
			zero++; 
		else 
			count += Math.signum(s3);

		if (count == -4 || count == 4)
			return 1;

		if (zero==4)
			return 0;

		if (zero > 0)
			return 3;

		return 2;

	}
/*
	private int intersectType(Line l)
	{
		//  System.out.println ( this.getoLat() + "," + this.getoLng() + " - " + this.getdLat() + "," + this.getdLng());
		// System.out.println ( l.getoLat() + "," + l.getoLng() + " - " + l.getdLat() + "," + l.getdLng());
		
		Point s2 = new Point (this.getoLat() - this.getdLat(), this.getoLng() - this.getdLng());
		Point s1 = new Point (l.getoLat() - l.getdLat(), l.getoLng() - l.getdLng());
		
		Long base = ((-s2.getLng().longValue()) * s1.getLat().longValue() + s1.getLng().longValue() * s2.getLat().longValue()) ;
		
		// i assume this means that the two lines are the same
		// means that a link already exists.
		
		if (base == 0) { return 0; } // equal or paralell
		
		Double s = ((-s1.getLat().longValue()) * (this.getoLng().longValue() - l.getoLng().longValue()) + s1.getLng().longValue() * (this.getoLat().longValue() - l.getoLat().longValue())) / ( base * 1.0);
		Double t = (s2.getLng().longValue() * (this.getoLat().longValue() -l.getoLat().longValue()) - s2.getLat().longValue() * (this.getoLng().longValue() -l.getoLng().longValue())) / ( base * 1.0);
		
		// System.out.println("Base:  " + base + " s:t " + s + " : " + t);
		
		// don't care if the end points touch
		if (s > 0 && s < 1 && t > 0 && t < 1) { return 1; } // intersects without touching
		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) { return 3; } // intersects with touching

		return 2; // no intersection
	}
	*/
	public Boolean intersects(Line l) { 

		// DrawTools dt = new DrawTools();
		//dt.addLine(this);
		//dt.addLine(l);

		//int i = intersectType(l);
		int gi = greaterCircleIntersectType(l);

	/*
		if ( i != gi ) {
                        dt.addLine(this);
                        dt.addLine(l);
                        System.out.println (dt.out());

			System.out.println ("linear intersect: " + intersectType(l) + " greater intersect: " + greaterCircleIntersectType(l));
		}
	*/
		// really would like some unit tests now.
		return (gi == 1);
	}
	public Boolean intersectsOrEqual(Line l) { return (greaterCircleIntersectType(l) != 2);	}
    public Boolean equalLine(Line l) { return (greaterCircleIntersectType(l) == 0);	}

	
	

	public boolean intersects (Line[] lines)
	{
		
		for (Line line : lines)
		{
			if (intersects(line)) { return true; }
		}
		
		return false;
	}
	
	public boolean intersectsOrEquals (Line[] lines)
	{		
		for (Line line : lines)
		{
			if (intersectsOrEqual(line)) { return true; }
		}
		
		return false;
	}
	
	
	public Double getGeoDistance() {
		return this.getO().getGeoDistance(this.getD());
	}
	
	public String toString() {
		
		return new String ( "{\"type\":\"polyline\",\"latLngs\":[{\"lat\":" + this.getoLat()/1000000.0 + 
						   ",\"lng\":" + this.getoLng()/1000000.0 +
						   "},{\"lat\":" + this.getdLat()/1000000.0 +
						   ",\"lng\":" + this.getdLng()/1000000.0 +
						   "}],\"color\":\"#F00000\"}" );
		
		// return new String ( getoLatAsDouble() + "," + getoLngAsDouble() + " - " + getdLatAsDouble() +","+ getdLngAsDouble());
		
	}
	
}

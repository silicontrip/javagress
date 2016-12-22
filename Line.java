import javax.vecmath.Vector3d;
public class Line {
	
	Point o;
	Point d;
	
	public static final Double earthRadius = 6371.0;
	
	// close to zero threshold
	final static  double eps = 1E-10;
	
	public Long getdLat() { return d.getLatE6(); }
	public Long getdLng() { return d.getLngE6(); }
	//public void setdLat(Long l) { d.setLat(l); }
	//public void setdLng(Long l) { d.setLng(l); }
	
	public Long getoLat() { return o.getLatE6(); }
	public Long getoLng() { return o.getLngE6(); }
	
	public Double getoLatAsDouble() { return new Double(o.getLatE6()); }
	public Double getoLngAsDouble() { return new Double(o.getLngE6()); }
	public Double getdLatAsDouble() { return new Double(d.getLatE6()); }
	public Double getdLngAsDouble() { return new Double(d.getLngE6()); }
	
	public Vector3d getoVect() { return new Vector3d(getoX(),getoY(),getoZ()); }
	public Vector3d getdVect() { return new Vector3d(getdX(),getdY(),getdZ()); }
	
	public double getoX() { return o.getX(); }
	public double getoY() { return o.getY(); }
	public double getoZ() { return o.getZ(); }
	
	public double getdX() { return d.getX(); }
	public double getdY() { return d.getY(); }
	public double getdZ() { return d.getZ(); }
	
	//public void setoLat(Long l) { o.setLat(l); }
	//public void setoLng(Long l) { o.setLng(l); }
	
	public Point getD() { return d; }
	public Point getO() { return o; }
	
	public Line (Point d, Point o)
	{
		this.d = new Point(d);
		this.o = new Point(o);
		
		// not sure if this is wise.
		
		//this.d = d;
		//this.o = o;
		
	}
	
	public Line (Long dla,Long dlo, Long ola,Long olo)
	{
		this.d = new Point(dla,dlo);
		this.o = new Point(ola,olo);
	}
	
	@Override
	public final boolean equals(Object obj2)
	{
  		if (obj2 == this) return true;
  		if (!(obj2 instanceof Line)) return false;
  		Line l = (Line) obj2;
		
		return (this.getO().equals(l.getO()) && this.getD().equals(l.getD())) || (this.getO().equals(l.getD()) && this.getD().equals(l.getO()));
	}
	
	@Override
	public int hashCode()
	{
		return getO().hashCode() | getD().hashCode();
	}
	
	public boolean findIn(Line[] lines)
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
		
		// System.out.println("["+p0 +" - "+p1+"] x [" + p2 + " - " + p3 + "]");
		
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
			return 1; // intersect
		
		if (zero==4)
			return 0; // equal
		
		if (zero > 0)
			return 3; // parallel?
		
		return 2; // not intersect
		
	}
	public boolean intersects(Line l) {
		
		// DrawTools dt = new DrawTools();
		//dt.addLine(this);
		//dt.addLine(l);
		
		//int i = intersectType(l);
		int gi = greaterCircleIntersectType(l);
		
	//	System.out.println(">>> Intersects: " + gi);
		
		// really would like some unit tests now.
		return (gi == 1);
	}
	public boolean intersectsOrEqual(Line l) { return (greaterCircleIntersectType(l) != 2);	}
	public boolean equalLine(Line l) { return (greaterCircleIntersectType(l) == 0);	}
	
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


	public Double getGeoDistance(Point p) {
		
		Vector3d p0 = this.getoVect();
                Vector3d p1 = this.getdVect();

		Vector3d C = new Vector3d (p.getX(), p.getY(), p.getZ());

		Vector3d N = new Vector3d();

		N.cross(p0,p1);
		N.normalize();
		//double dot = N.dot(C);
		//System.out.println("dot: " + dot);
		double adist = Math.PI / 2 - Math.acos(N.dot(C));

		return earthRadius * adist;
	
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
	
	public Double getBearing() { return o.getBearingTo(d); }
	public Double getReverseBearing() { return d.getBearingTo(o); }
	
}

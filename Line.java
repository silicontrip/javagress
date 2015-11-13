import javax.vecmath.Vector3d;
public class Line {

	Long oLat;
	Long dLat;
	Long oLng;
	Long dLng;
	
	public static Double earthRadius = 6371.0;
	
	// close to zero threshold
	final static  double eps = 0.001;


	public Long getdLat() { return dLat; }
	public Long getdLng() { return dLng; }
	public void setdLat(Long l) { dLat=l; }
	public void setdLng(Long l) { dLng=l; }

	public Long getoLat() { return oLat; }
	public Long getoLng() { return oLng; }
	
	public Double getoLatAsDouble() { return new Double(oLat); }
	public Double getoLngAsDouble() { return new Double(oLng); }
	public Double getdLatAsDouble() { return new Double(dLat); }
	public Double getdLngAsDouble() { return new Double(dLng); }

	public Vector3d getoVect() { return new Vector3d(getoX(),getoY(),getoZ()); }
	public Vector3d getdVect() { return new Vector3d(getdX(),getdY(),getdZ()); }
	
	public double getoX() { return Math.cos(Math.toRadians(oLat)) * Math.cos(Math.toRadians(oLng)); }
	public double getoY() { return Math.cos(Math.toRadians(oLat)) * Math.sin(Math.toRadians(oLng)); }
	public double getoZ() { return Math.sin(Math.toRadians(oLat)); }

	public double getdX() { return Math.cos(Math.toRadians(dLat)) * Math.cos(Math.toRadians(dLng)); }
	public double getdY() { return Math.cos(Math.toRadians(dLat)) * Math.sin(Math.toRadians(dLng)); }
	public double getdZ() { return Math.sin(Math.toRadians(dLat)); }

	
	public void setoLat(Long l) { oLat=l; }
	public void setoLng(Long l) { oLng=l; }

	
	public Line (Point d, Point o) 
	{
	
		setdLat(d.getLat());
		setdLng(d.getLng());

		setoLat(o.getLat());
		setoLng(o.getLng());

		
	}
	
	public Line (Portal d, Portal o) {
		this(d.getPoint(), o.getPoint());
	}
	
	public Line (Long dla,Long dlo, Long ola,Long olo)
	{
		setdLat(dla);
		setdLng(dlo);
		setoLat(ola);
		setoLng(olo);
	}
	
	public boolean equals(Line l) 
	{
	
		return ((l.getdLat().equals(getdLat())) && (l.getoLat().equals(getoLat())) && (l.getdLng().equals(getdLng())) && (l.getoLng().equals(getoLng())) ||
				(l.getdLat().equals(getoLat())) && (l.getoLat().equals(getdLat())) && (l.getdLng().equals(getoLng())) && (l.getoLng().equals(getdLng())));
		
	}
	
	public boolean equals(Link[] links)
	{
		for (Link link : links)
		{
			if (equals(link.getLine())) { return true; }
		}
		
		return false;
	}
	
	
	// looks like I need a 3d point class
	public int greaterCircleIntersectType (Line l)
	{


		Vector3d V = new Vector3d();
		V.cross( this.getoVect(),this.getdVect());
		V.normalize();

		Vector3d U = new Vector3d();
		U.cross( l.getoVect(), l.getdVect());
		U.normalize();

		Vector3d D = new Vector3d();
		D.cross( V, U );
		D.normalize();
		
				
		Vector3d S1 = new Vector3d();
		Vector3d S2 = new Vector3d();
		Vector3d S3 = new Vector3d();
		Vector3d S4 = new Vector3d();

		S1.cross(this.getoVect(),V);
		S2.cross(this.getdVect(),V);
		S3.cross(l.getoVect(),U);
		S4.cross(l.getdVect(),U);

		double sign1 = -S1.dot(D);
		double sign2 = S2.dot(D);
		double sign3 = -S3.dot(D);
		double sign4 = S4.dot(D);

		//System.out.println("Signs: " +sign1 + " " + sign2 + " " + sign3 + " " + sign4);

		if (sign1 < 0  && sign2 < 0 && sign3 < 0 && sign4 < 0) 
			return 1;
		if (sign1 > 0  && sign2 > 0 && sign3 > 0 && sign4 > 0) 
			return 1;

		return 2;
		
	}

	private int intersectType(Line l)
	{
		//  System.out.println ( this.getoLat() + "," + this.getoLng() + " - " + this.getdLat() + "," + this.getdLng());
		// System.out.println ( l.getoLat() + "," + l.getoLng() + " - " + l.getdLat() + "," + l.getdLng());
		
		Point s2 = new Point (this.getoLat() - this.getdLat(), this.getoLng() - this.getdLng());
		Point s1 = new Point (l.getoLat() - l.getdLat(), l.getoLng() - l.getdLng());
		
		Long base = ((-s2.getLng().longValue()) * s1.getLat().longValue() + s1.getLng().longValue() * s2.getLat().longValue()) ;
		
		// i assume this means that the two lines are the same
		// means that a link already exists.
		
		if (base == 0) { return 0; } // equal
		
		Double s = ((-s1.getLat().longValue()) * (this.getoLng().longValue() - l.getoLng().longValue()) + s1.getLng().longValue() * (this.getoLat().longValue() - l.getoLat().longValue())) / ( base * 1.0);
		Double t = (s2.getLng().longValue() * (this.getoLat().longValue() -l.getoLat().longValue()) - s2.getLat().longValue() * (this.getoLng().longValue() -l.getoLng().longValue())) / ( base * 1.0);
		
		// System.out.println("Base:  " + base + " s:t " + s + " : " + t);
		
		// don't care if the end points touch
		if (s > 0 && s < 1 && t > 0 && t < 1) { return 1; } // intersects without touching
		// if (s >= 0 && s <= 1 && t >= 0 && t <= 1) { return 3; } // intersects with touching

		return 2; // no intersection
	}
		
	public Boolean intersects(Line l) { 

	//	int i = intersectType(l);
		int gi = greaterCircleIntersectType(l);

	//	if ( i != gi ) {
	//		System.out.println ("linear intersect: " + intersectType(l));
	//		System.out.println ("greater intersect: " + greaterCircleIntersectType(l));
	//	}
		// really would like some unit tests now.
		return (gi == 1);
	}
	public Boolean intersectsOrEqual(Line l) { return (intersectType(l) != 2);	}
    public Boolean equalLine(Line l) { return (intersectType(l) == 0);	}

    
	public boolean intersects (Link[] links)
	{
		
		for (Link link : links)
		{
			if (intersects(link.getLine())) { return true; }
		}
		
		return false;
	}
	
	public boolean intersectsOrEquals (Link[] links)
	{		
		for (Link link : links)
		{
			if (intersectsOrEqual(link.getLine())) { return true; }
		}
		
		return false;
	}
	
	
	public Double getGeoDistance() {
	
		Double oLat = this.getoLat()/1000000.0;
		Double oLng = this.getoLng()/1000000.0;
		Double dLat = this.getdLat()/1000000.0;
		Double dLng = this.getdLng()/1000000.0;
		
//System.err.println("point: " + dLat + "," + dLng + " - " + oLat + "," + oLng); 
		
		Double lat = Math.toRadians(dLat - oLat);
		Double lng = Math.toRadians(dLng - oLng);
		
		Double a = (Math.sin(lat / 2.0) * Math.sin(lat / 2.0)) +
		Math.cos(Math.toRadians(oLat)) * Math.cos(Math.toRadians(dLat)) *
		Math.sin(lng / 2.0) * Math.sin(lng/2.0);
		
		
		
		Double c = 2.0 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
		
		Double len = earthRadius * c;
		
	//	System.err.println("point: " + dLat + "," + dLng + " - " + oLat + "," + oLng + " = " + len + " A: " + a); 

		
		return len;
		
		
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

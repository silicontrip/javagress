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

	public double getoX() { return Math.cos(Math.toRadians(oLat)) * Math.cos(Math.toRadians(oLng)); }
	public double getoY() { return Math.cos(Math.toRadians(oLat)) * Math.sin(Math.toRadians(oLng)); }
	public double getoZ() { return Math.sin(Math.toRadians(oLat)); }

	public double getdX() { return Math.cos(Math.toRadians(oLat)) * Math.cos(Math.toRadians(oLng)); }
	public double getdY() { return Math.cos(Math.toRadians(oLat)) * Math.sin(Math.toRadians(oLng)); }
	public double getdZ() { return Math.sin(Math.toRadians(oLat)); }

	
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
	private int greaterCircleIntersectType (Line l)
	{
	
		double vx1 = this.getdX() - this.getoX();
		double vy1 = this.getdY() - this.getoY();
		double vz1 = this.getdZ() - this.getoZ();
		

		double vx2 = l.getdX() - l.getoX();
		double vy2 = l.getdY() - l.getoY();
		double vz2 = l.getdZ() - l.getoZ();

		
		double l1 = Math.sqrt(vx1 * vx1 + vy1 * vy1 + vz1 * vz1);
		double l2 = Math.sqrt(vx2 * vx2 + vy2 * vy2 + vz2 * vz2);
		
		double ux1 = vx1 / l1;
		double uy1 = vy1 / l1;
		double uz1 = vz1 / l1;
		
		double ux2 = vx2 / l2;
		double uy2 = vy2 / l2;
		double uz2 = vz2 / l2;
		
		
		if (Math.abs(ux1-ux2) < eps &&
			Math.abs(uy1-uy2) < eps &&
			Math.abs(uz1-uz2) < eps )
			return 0;
		
		
		// cross product of u1 and u2
		double dx = uy1 * uz2 - uy2 * uz1;
		double dy = ux2 * uz1 - ux1 * uz2;
		double dz = ux1 * uy2 - ux2 * uy1;
		
		double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);

		// errr now what?
		
		return 1;
		
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
		
	public Boolean intersects(Line l) { return (intersectType(l) == 1);	}
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

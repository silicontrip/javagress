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

		double x1,y1,z1,x2,y2,z2;
		double ll;
	
		x1 = this.getdX();
		y1 = this.getdY();
		z1 = this.getdZ();

		//System.out.println ("PD: " + x1 + ", " +y1+", " + z1);

		x2 =  this.getoX();
		y2 =  this.getoY();
		z2 =  this.getoZ();
		//System.out.println ("OD: " + x2 + ", " +y2+", " + z2);
		
		// cross v1 x v2
		double vx = y1 * z2 - y2 * z1;
		double vy = x2 * z1 - x1 * z2;
		double vz = x1 * y2 - x2 * y1;

		// normalise the vector
		ll = Math.sqrt(vx * vx + vy * vy + vz * vz);

		vx = vx / ll;
		vy = vy / ll;
		vz = vz / ll;

		Vector3d V = new Vector3d();
		V.cross( this.getoVect(),this.getdVect());
		V.normalize();

		System.out.println ("norm V: " + vx + ", " +vy+", " + vz);

		x1 = l.getdX();
		y1 = l.getdY();
		z1 = l.getdZ();

		x2 = l.getoX();
		y2 = l.getoY();
		z2 = l.getoZ();
		
		// cross v1 x v2
		double ux = y1 * z2 - y2 * z1;
		double uy = x2 * z1 - x1 * z2;
		double uz = x1 * y2 - x2 * y1;


		// normalise the vector
		ll = Math.sqrt(ux * ux + uy * uy + uz * uz);

		ux = ux / ll;
		uy = uy / ll;
		uz = uz / ll;
		
		System.out.println ("norm U: " + ux + ", " +uy+", " + uz);

		// lines equal
		// this bombs out earlier if they are equal as the |cross product| == 0
		if (Math.abs(ux-vx) < eps &&
			Math.abs(uy-vy) < eps &&
			Math.abs(uz-vz) < eps )
			return 0;
		
		
		// cross product of u and v
		double dx = vy * uz - uy * vz;
		double dy = ux * vz - vx * uz;
		double dz = vx * uy - ux * vy;

		System.out.println ("D: " + dx + ", " +dy+", " + dz);
		
		ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

		double sx = dx / ll;
		double sy = dy / ll;
		double sz = dz / ll;

		double s1x = -dx / ll;
		double s1y = -dy / ll;
		double s1z = -dz / ll;

		//check if s or s1 are on the lines...

		// convert s and s1 to lat lng

		System.out.println ("S: " + sx + ", " +sy+", " + sz);

		double lat = Math.asin(sz);
		double tmp = Math.cos(lat);
		double sign = Math.asin(sy/tmp);
		double lng = Math.acos(sx/tmp) * sign;

		System.out.println ("lat " + lat + " tmp " + tmp + " sign " + sign + " long " + lng);


		Long latd = new Double(lat / Math.PI * 180 * 1000000).longValue();
		Long lngd = new Double(lng / Math.PI * 180 * 1000000).longValue();

		System.out.println("intersect at: " + latd + ", " + lngd);

		Line os = new Line(latd , lngd, this.getoLat(), this.getoLng());
		Line ds = new Line(latd , lngd, this.getdLat(), this.getdLng());

		System.out.println ( "distance: " + os.getGeoDistance() + " " + ds.getGeoDistance());

		if (os.getGeoDistance() == 0 || ds.getGeoDistance() == 0)
			return 3;
	
		double test1 = this.getGeoDistance() - os.getGeoDistance() - ds.getGeoDistance();	

		Line los = new Line(latd  , lngd , l.getoLat(), l.getoLng());
		Line lds = new Line(latd , lngd , l.getdLat(), l.getdLng());

		System.out.println ( "l distance: " + los.getGeoDistance() + " " + lds.getGeoDistance());
		if (los.getGeoDistance() == 0 || lds.getGeoDistance() == 0)
			return 3;

		double test2 = this.getGeoDistance() - los.getGeoDistance() - lds.getGeoDistance();	

		System.out.println ("test: " + test1 + " " + test2);

		if (test1 == 0 && test2 == 0) 	
			return 1;

		lat = Math.asin(s1z);
		tmp = Math.cos(lat);
		sign = Math.asin(s1y/tmp);
		lng = Math.acos(s1x/tmp) * sign;
		latd = new Double(lat / Math.PI * 180 * 1000000).longValue();
		lngd = new Double(lng / Math.PI * 180 * 1000000).longValue();

		System.out.println("intersect at: " + latd + ", " + lngd);

		Line os2 = new Line(latd, lngd, this.getoLat(), this.getoLng());
		Line ds2 = new Line(latd , lngd, this.getdLat(), this.getdLng());

		System.out.println ( "distance 2: " + os2.getGeoDistance() + " " + ds2.getGeoDistance());
		if (os2.getGeoDistance() == 0 || ds2.getGeoDistance() == 0)
			return 3;
	
		test1 = this.getGeoDistance() - os2.getGeoDistance() - ds2.getGeoDistance();	

		

		Line los2 = new Line(latd , lngd, l.getoLat(), l.getoLng());
		Line lds2 = new Line(latd , lngd, l.getdLat(), l.getdLng());

		System.out.println ( "l distance 2: " + los2.getGeoDistance() + " " + lds2.getGeoDistance());
		if (los2.getGeoDistance() == 0 || lds2.getGeoDistance() == 0)
			return 3;

		test2 = this.getGeoDistance() - los2.getGeoDistance() - lds2.getGeoDistance();	

		System.out.println ("test 2: " + test1 + " " + test2);
		if (test1 == 0 && test2 == 0) 	
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

		int i = intersectType(l);
		int gi = greaterCircleIntersectType(l);

		if ( i != gi ) {
			System.out.println ("linear intersect: " + intersectType(l));
			System.out.println ("greater intersect: " + greaterCircleIntersectType(l));
		}
		// reall would like some unit tests now.
		return (i == 1);
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

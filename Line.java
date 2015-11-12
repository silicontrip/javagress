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
	
		double x1 = this.getdX();
		double y1 = this.getdY();
		double z1 = this.getdZ();

		double x2 =  this.getoX();
		double y2 =  this.getoY();
		double z2 =  this.getoZ();
		
		// cross v1 x v2
		double vx = y1 * z2 - y2 * z1;
		double vy = x2 * z1 - x1 * z2;
		double vz = x1 * y2 - x2 * y1;

		// normalise the vector
		double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);

		vx = vx / dl;
		vy = vy / dl;
		vz = vz / dl;

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
		double el = Math.sqrt(ux * ux + uy * uy + uz * uz);

		ux = ux / el;
		uy = uy / el;
		uz = uz / el;
		
		if (Math.abs(ux-vx) < eps &&
			Math.abs(uy-vy) < eps &&
			Math.abs(uz-vz) < eps )
			return 0;
		
		
		// cross product of u and v
		double dx = vy * uz - uy * vz;
		double dy = ux * vz - vx * uz;
		double dz = vx * uy - ux * vy;
		
		double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);

		double sx = dx / dl;
		double sy = dy / dl;
		double sz = dz / dl;

		double s1x = -dx / dl;
		double s1y = -dy / dl;
		double s1x = -dz / dl;

		//check if s or s1 are on the lines...

		// convert s and s1 to lat lng

		double lat = Math.asin(sz);

		double tmp = Math.cos(lat);
		double sign = Math.asin(sy/tmp);
		double lng = Math.acos(sx/tmp) * sign;

		Line os = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, this.getoLat(), this.getoLng());
		Line ds = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, this.getdLat(), this.getdLng());

		if (os.getGeoDistance() == 0 || ds.getGeoDistance() == 0)
			return 3;
	
		double test1 = this.getGeoDistance() - os.getGeoDistance() - ds.getGeoDistance();	

		

		Line los = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, l.getoLat(), l.getoLng());
		Line lds = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, l.getdLat(), l.getdLng());

		if (los.getGeoDistance() == 0 || lds.getGeoDistance() == 0)
			return 3;

		double test2 = this.getGeoDistance() - los.getGeoDistance() - lds.getGeoDistance();	

		if (test1 == 0 && test2 == 0) 	
			return 1;

		lat = Math.asin(s1z);

		tmp = Math.cos(lat);
		sign = Math.asin(s1y/tmp);
		lng = Math.acos(s1x/tmp) * sign;

		Line os1 = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, this.getoLat(), this.getoLng());
		Line ds1 = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, this.getdLat(), this.getdLng());

		if (os1.getGeoDistance() == 0 || ds1.getGeoDistance() == 0)
			return 3;
	
		 test1 = this.getGeoDistance() - os1.getGeoDistance() - ds1.getGeoDistance();	

		

		Line los = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, l.getoLat(), l.getoLng());
		Line lds = new Line(lat / Math.PI * 180 * 1000000 , lng / Math.PI * 180 * 1000000, l.getdLat(), l.getdLng());

		if (los.getGeoDistance() == 0 || lds.getGeoDistance() == 0)
			return 3;

		double test2 = this.getGeoDistance() - los.getGeoDistance() - lds.getGeoDistance();	

		if (test1 == 0 && test2 == 0) 	
			return 1;

		return 2;
		
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	private Double getDst() {
		return 2*Math.asin(Math.sqrt((Math.sin((deg2rad(this.getoLat()-this.getdLat()))/2))^2+ Math.cos(deg2rad(this.getoLat()))*Math.cos(deg2rad(this.getdLat()))*Math.sin(deg2rad(this.getoLng()-this.getdLng())/2)^2));
	}

	private Double getCrs() {

		Double dst12=this.getDst();

		if (Math.sin(this.getdLng() - this.getoLng())<0)
			return Math.acos((Math.sin(this.getdLat())-Math.sin(this.getoLat())*Math.cos(dst12))/(Math.sin(dst12)*Math.cos(this.getoLat())));
		else
			return 2*Math.PI-Math.acos((Math.sin(this.getdLat())-Math.sin(this.getoLat())*Math.cos(dst12))/(Math.sin(dst12)*Math.cos(this.getoLat())));
	}

	private Double getRCrs() {

		Double dst12=this.getDst();

		if (Math.sin(this.getdLng() - this.getoLng())<0)
			return 2*Math.PI-Math.acos((Math.sin(this.getoLat())-Math.sin(this.getdLat())*Math.cos(dst12))/(Math.sin(dst12)*Math.cos(this.getdLat())));
		else
			return Math.acos((Math.sin(this.getoLat())-Math.sin(this.getdLat())*Math.cos(dst12))/(Math.sin(dst12)*Math.cos(this.getdLat())));
	}

	private int greaterCircleIntersectType2(Line l) 
	{

		Double crs12 = this.getCrs();
		Double crs21 = this.getRCrs();
		
		Double crs13 = l.getCrs();
		Double crs23 = l.getRCrs();

		Double ang1=(crs13-crs12+Math.pi)-Math.pi;
		Double ang2=(crs21-crs23+Math.pi)-Math.pi;

		if (Math.sin(ang1)==0 && Math.sin(ang2)==0)
			return 0; // "infinity of intersections"
		else if (Math.sin(ang1)*Math.sin(ang2)<0)
			return 2; //"intersection ambiguous"
		else
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
		
	public Boolean intersects(Line l) { 
		System.out.println ("linear intersect: " + intersectType(l));
		System.out.println ("greater intersect: " + greaterCircleIntersectType(l));
		return (intersectType(l) == 1);
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

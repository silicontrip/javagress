import com.google.common.geometry.*;
public class Line {

	Long oLat;
	Long dLat;
	Long oLng;
	Long dLng;
	
	public static Double earthRadius = 6371.0;
	
	// close to zero threshold
	final static  double eps = 1E-9;


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

	public S2Point getoVect() { return new S2Point(getoX(),getoY(),getoZ()); }
	public S2Point getdVect() { return new S2Point(getdX(),getdY(),getdZ()); }
 /*
	public S2Point getNormVect() { 

		return new S2Point(
			Math.sin(

	}
*/
	
	public double getoX() { return Math.cos(Math.toRadians(oLat/1000000.0)) * Math.cos(Math.toRadians(oLng/1000000.0)); }
	public double getoY() { return Math.cos(Math.toRadians(oLat/1000000.0)) * Math.sin(Math.toRadians(oLng/1000000.0)); }
	public double getoZ() { return Math.sin(Math.toRadians(oLat/1000000.0)); }

	public double getdX() { return Math.cos(Math.toRadians(dLat/1000000.0)) * Math.cos(Math.toRadians(dLng/1000000.0)); }
	public double getdY() { return Math.cos(Math.toRadians(dLat/1000000.0)) * Math.sin(Math.toRadians(dLng/1000000.0)); }
	public double getdZ() { return Math.sin(Math.toRadians(dLat/1000000.0)); }

	
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
	
	
	public int greaterCircleIntersectType (Line l)
	{

		S2Point a = this.getoVect();
		S2Point b = this.getdVect();
		S2Point c = l.getoVect();
		S2Point d = l.getdVect();


		//System.out.println("" + getoLat() + ", " + getoLng() + ": " + getdLat() + ", " + getdLng());

		S2Point abx = S2Point.crossProd(a,b);
		S2Point cdx = S2Point.crossProd(c,d);

		S2Point t = S2Point.normalize(S2Point.crossProd(abx,cdx));

		double s0 = S2Point.crossProd(abx,a).dotProd(t);
		double s1 = S2Point.crossProd(b,abx).dotProd(t);
		double s2 = S2Point.crossProd(cdx,c).dotProd(t);
		double s3 = S2Point.crossProd(d,cdx).dotProd(t);

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

		//System.out.println ("Zeros: " + zero + " sign: " + count);

		//System.out.println ("simpleCrossing: " + S2EdgeUtil.simpleCrossing(a,b,c,d));
		//System.out.println ("vertexCrossing: " + S2EdgeUtil.vertexCrossing(a,b,c,d));
		//System.out.println ("smartCrossing: " + S2EdgeUtil.edgeOrVertexCrossing(a,b,c,d));
		//System.out.println ("intersect: " + S2EdgeUtil.getIntersection(a,b,c,d));
		//int cross = S2EdgeUtil.robustCrossing(a,b,c,d);

		//if (cross==-1)
		//	return 2;

		// test for touching lines.

		//return cross;
		
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
		
		if (base == 0) { return 0; } // equal or paralell
		
		Double s = ((-s1.getLat().longValue()) * (this.getoLng().longValue() - l.getoLng().longValue()) + s1.getLng().longValue() * (this.getoLat().longValue() - l.getoLat().longValue())) / ( base * 1.0);
		Double t = (s2.getLng().longValue() * (this.getoLat().longValue() -l.getoLat().longValue()) - s2.getLat().longValue() * (this.getoLng().longValue() -l.getoLng().longValue())) / ( base * 1.0);
		
		// System.out.println("Base:  " + base + " s:t " + s + " : " + t);
		
		// don't care if the end points touch
		if (s > 0 && s < 1 && t > 0 && t < 1) { return 1; } // intersects without touching
		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) { return 3; } // intersects with touching

		return 2; // no intersection
	}
		
	public Boolean intersects(Line l) { 

		DrawTools dt = new DrawTools();
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

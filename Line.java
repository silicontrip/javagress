import com.google.common.geometry.*;
public class Line {

	//Long oLat;
	//Long dLat;
	//Long oLng;
	//Long dLng;

	S2LatLng orig;
	S2LatLng dest;
	
	public static Double earthRadius = 6371.0;
	
	// close to zero threshold
	final static  double eps = 1E-9;


	//public Long getdLat() { return dLat; }
	//public Long getdLng() { return dLng; }
	//public void setdLat(Long l) { dLat=l; }
	//public void setdLng(Long l) { dLng=l; }

	//public Long getoLat() { return oLat; }
	//public Long getoLng() { return oLng; }
	
	//public Double getoLatAsDouble() { return new Double(oLat); }
	//public Double getoLngAsDouble() { return new Double(oLng); }
	//public Double getdLatAsDouble() { return new Double(dLat); }
	//public Double getdLngAsDouble() { return new Double(dLng); }

	public S2Point getoVect() { return orig.toPoint(); }
	public S2Point getdVect() { return dest.toPoint(); }
	
	//public void setoLat(Long l) { oLat=l; }
	//public void setoLng(Long l) { oLng=l; }

	
	public Line (S2LatLng d, S2LatLng o) 
	{
	
		orig = o;
		dest = d;
		//setdLat(d.getLat());
		//setdLng(d.getLng());

		//setoLat(o.getLat());
		//setoLng(o.getLng());

		
	}
	
	public Line (Portal d, Portal o) {
		this(d.getLatLng(), o.getLatLng());
	}
	
/*
	public Line (Long dla,Long dlo, Long ola,Long olo)
	{
		setdLat(dla);
		setdLng(dlo);
		setoLat(ola);
		setoLng(olo);
	}
*/	
	public boolean equals(Object o) 
	{
		if (o == null || !(o instanceof Line)) {
		      return false;
		}
		Line l = (Line) o;

		return ( (getoVect().equals(l.getoVect()) && getdVect().equals(l.getdVect())) || (getdVect().equals(l.getoVect()) && getoVect().equals(l.getdVect())));
		
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

		
	}

	public Boolean intersects(Line l) { return greaterCircleIntersectType(l) ==1; }
	public Boolean intersectsOrEqual(Line l) { return (greaterCircleIntersectType(l) != 2);	}
    //public Boolean equalLine(Line l) { return (intersectType(l) == 0);	}

    
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
		return orig.getEarthDistance(dest);
	}
	
	public String toString() {
		
	   return String.format("Edge: (%s -> %s)\n   or [%s -> %s]",
		orig.toStringDegrees(), dest.toStringDegrees(), orig, dest);

		
	}
	
}

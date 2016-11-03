
public class Point {

	protected Long lat;
	protected Long lng;

	public Long getLat() { return lat; }
	public Long getLng() { return lng; }
	public Double getLatAsDouble() { return new Double(lat); }
	public Double getLngAsDouble() { return new Double(lng); }

	
	// Greater Circle maths functions
	public Double getLatAsRad() { return Math.toRadians(lat / 1000000.0 ); }
	public Double getLngAsRad() { return Math.toRadians(lng / 1000000.0 ); }
	public Double getX() { return Math.cos(getLatAsRad()) * Math.cos(getLngAsRad()); }
	public Double getY() { return Math.cos(getLatAsRad()) * Math.sin(getLngAsRad()); }
	public Double getZ() { return Math.sin(getLatAsRad()); }

	
	public void setLat(Long l) { lat=l; }
	public void setLng(Long l) { lng=l; }

	public boolean equals(Point p) 
	{
		return (getLat().equals(p.getLat()) && getLng().equals(p.getLng()));
	}
	
	public Point (java.lang.Long la, java.lang.Long ln) {
		setLat(la);
		setLng(ln);
	}

	public Point (Float la,Float ln) {
		la *= new Float(1000000.0);
		ln *= new Float(1000000.0);
		setLat(la.longValue());
		setLng(ln.longValue());
	}

	public Point(Point p) {
		setLat(p.getLat());
		setLng(p.getLng());
	}


	public String toString() {
		return new String (lat + ", " + lng);
	}

	public Point inverse() {
		return new Point(-this.getLat(),180000000 - this.getLng());
	}

	public Double getGeoDistance(Point p) {

                Double oLat = this.getLat()/1000000.0;
                Double oLng = this.getLng()/1000000.0;
                Double dLat = p.getdLat()/1000000.0;
                Double dLng = p.getdLng()/1000000.0;

//System.err.println("point: " + dLat + "," + dLng + " - " + oLat + "," + oLng);

                Double lat = Math.toRadians(dLat - oLat);
                Double lng = Math.toRadians(dLng - oLng);

                Double a = (Math.sin(lat / 2.0) * Math.sin(lat / 2.0)) +
                Math.cos(Math.toRadians(oLat)) * Math.cos(Math.toRadians(dLat)) *
                Math.sin(lng / 2.0) * Math.sin(lng/2.0);



                Double c = 2.0 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

                Double len = earthRadius * c;

        //      System.err.println("point: " + dLat + "," + dLng + " - " + oLat + "," + oLng + " = " + len + " A: " + a);


                return len;
	}
	
}

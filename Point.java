
public class Point {

	public static Double earthRadius = 6371.0;
	protected Long lat;
	protected Long lng;

	public Long getLatE6() { return lat; }
	public Long getLngE6() { return lng; }
	public Double getLatAsDouble() { return new Double(lat); }
	public Double getLngAsDouble() { return new Double(lng); }
	public Double getLat() { return lat/1000000.0; }
	public Double getLng() { return lng/1000000.0; }

	
	// Greater Circle maths functions
	public Double getLatAsRad() { return Math.toRadians(lat / 1000000.0 ); }
	public Double getLngAsRad() { return Math.toRadians(lng / 1000000.0 ); }
	public Double getX() { return Math.cos(getLatAsRad()) * Math.cos(getLngAsRad()); }
	public Double getY() { return Math.cos(getLatAsRad()) * Math.sin(getLngAsRad()); }
	public Double getZ() { return Math.sin(getLatAsRad()); }

	
	public void setLat(Long l) { lat=l; }
	public void setLng(Long l) { lng=l; }

	public void setLat(Double d) {  d *= new Double(1000000.0); setLat(d.longValue()); }
	public void setLng(Double d) {  d *= new Double(1000000.0); setLng(d.longValue()); }

	public void setLat(String s) { setLat(Double.parseDouble(s)); }
	public void setLng(String s) { setLng(Double.parseDouble(s)); }

	@Override
	public final boolean equals(Object obj2)
	{
		if (obj2 == this) return true;
		if (!(obj2 instanceof Point)) return false;
		Point p = (Point) obj2;
		return (getLatE6().equals(p.getLatE6()) && getLngE6().equals(p.getLngE6()));
	}
	public int hashCode()
	{
		return (getLatE6().hashCode() * 2  +1 )+ ( getLngE6().hashCode() * 2 );
	}
	
	public Point (java.lang.Long la, java.lang.Long ln) {
		setLat(la);
		setLng(ln);
	}

	public Point (String ld)
	{

		String[] coord = ld.split(",");
		setLat(coord[0]);
		setLng(coord[1]);
		
	}
	
	public Point (String la, String ln)
	{
		setLat(la);
		setLng(ln);
	}
	public Point (Double la,Double ln) {
		la *= new Double(1000000.0);
		ln *= new Double(1000000.0);
		setLat(la.longValue());
		setLng(ln.longValue());
	}

	public Point(Point p) {
		setLat(p.getLatE6());
		setLng(p.getLngE6());
	}


	public String toString() {
		return new String (lat + ", " + lng);
	}

	public Point inverse() {
		return new Point(-this.getLatE6(),180000000 - this.getLngE6());
	}

	public Double getAngDistance(Point p) {
	
		Double oLat = this.getLat();
		Double oLng = this.getLng();
		Double dLat = p.getLat();
		Double dLng = p.getLng();
		
		Double lat = Math.toRadians(dLat - oLat);
		Double lng = Math.toRadians(dLng - oLng);
		
		Double a = (Math.sin(lat / 2.0) * Math.sin(lat / 2.0)) +
		Math.cos(Math.toRadians(oLat)) * Math.cos(Math.toRadians(dLat)) *
		Math.sin(lng / 2.0) * Math.sin(lng/2.0);
		
		return 2.0 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
	}
	
	public Double getGeoDistance(Point p) {
		return earthRadius * getAngDistance(p);
	}

	// gets the angle centred on this point made by the lines to p1 and p2
	public Double getAngle(Point p1, Point p2) {

		Double a = p1.getAngDistance(p2);
		Double b = this.getAngDistance(p1);
		Double c = this.getAngDistance(p2);

		return Math.acos((Math.cos(a) - (Math.cos(b) * Math.cos(c))) / (Math.sin(b) * Math.sin(c)));
	}
	public Double getBearingTo(Point p)
	{
		Double oLat = this.getLatAsRad();
		Double oLng = this.getLngAsRad();
		Double dLat = p.getLatAsRad();
		Double dLng = p.getLngAsRad();

		Double ddLng = dLng - oLng;
		
		Double y = Math.sin(ddLng) * Math.cos(dLat);
		Double x = Math.cos(oLat)*Math.sin(dLat) - Math.sin(oLat) * Math.cos(dLat) * Math.cos(ddLng);
		return Math.toDegrees(Math.atan2(y,x));
		
	}
	
	
}

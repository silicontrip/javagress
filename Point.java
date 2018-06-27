import com.google.common.geometry.*;
import javax.vecmath.Vector3d;

public class Point {

	//public static Double earthRadius = 6371.0;
	public static Double earthRadius = 6367.0;
	protected Long lat;
	protected Long lng;

	protected Vector3d v;

	/*
	protected Double x;
	protected Double y;
	protected Double z;
	*/

	public Long getLatE6() { return lat; }
	public Long getLngE6() { return lng; }
	public Double getLatAsDouble() { return new Double(lat); }
	public Double getLngAsDouble() { return new Double(lng); }
	public Double getLat() { return lat/1000000.0; }
	public Double getLng() { return lng/1000000.0; }

	public S2LatLng getS2LatLng() { return S2LatLng.fromDegrees(getLat(),getLng()); }
	
	// Greater Circle maths functions
	public Double getLatAsRad() { return Math.toRadians(lat / 1000000.0 ); }
	public Double getLngAsRad() { return Math.toRadians(lng / 1000000.0 ); }
	/*
	public Double getX() { return x; }
	public Double getY() { return y; }
	public Double getZ() { return z; }
*/
	public Vector3d getVector() { return v; }

	/*
	protected void setX(Double l) { x = l; }
	protected void setY(Double l) { y = l; }
	protected void setZ(Double l) { z = l; }
*/
	protected void setXYZ(double x, double y, double z) { v.set(x,y,z); }

	protected void setXYZ() 
	{
		setXYZ(
			Math.cos(getLatAsRad()) * Math.cos(getLngAsRad()),
			Math.cos(getLatAsRad()) * Math.sin(getLngAsRad()),
			Math.sin(getLatAsRad())
		);
	}
	
	protected void setLat(Long l) { lat=l; }
	protected void setLng(Long l) { lng=l; }

	protected void setLatFromRad(Double l) { setLat(l * 180 / Math.PI); }
	protected void setLngFromRad(Double l) { setLng(l * 180 / Math.PI); }


	protected void setLat(Double d) {  d *= new Double(1000000.0); setLat(d.longValue()); }
	protected void setLng(Double d) {  d *= new Double(1000000.0); setLng(d.longValue()); }

	protected void setLat(String s) { setLat(Double.parseDouble(s)); }
	protected void setLng(String s) { setLng(Double.parseDouble(s)); }

	@Override
	public final boolean equals(Object obj2)
	{
		if (obj2 == this) return true;
		if (!(obj2 instanceof Point)) return false;
		Point p = (Point) obj2;
		return (getLatE6().equals(p.getLatE6()) && getLngE6().equals(p.getLngE6()));
	}
	@Override
	public int hashCode()
	{
		return (getLatE6().hashCode() * 2  +1 )+ ( getLngE6().hashCode() * 2 );
	}
	
	public Point (Vector3d vec)
	{
		this.v = new Vector3d(vec);
		this.v.normalize();

		double lng = - (Math.atan2(-v.z,-v.x))- Math.PI / 2;
		if (lng < -Math.PI) lng += Math.PI*2;

		Vector3d p  = new Vector3d (v.x,0,v.z);
		p.normalize();

		double lat = Math.acos(p.dot(v));
		if (v.y < 0) lat = -lat;

		setLatFromRad(lat);
		setLngFromRad(lng);

	}

	public Point (java.lang.Long la, java.lang.Long ln) {
		setLat(la);
		setLng(ln);
		v = new Vector3d();
		setXYZ();
	}

	public Point (String ld)
	{

		String[] coord = ld.split(",");
		setLat(coord[0]);
		setLng(coord[1]);
		v = new Vector3d();

		setXYZ();
	}
	
	public Point (String la, String ln)
	{
		setLat(la);
		setLng(ln);
		v = new Vector3d();

		setXYZ();
	}
	public Point (Double la,Double ln) {
		la *= new Double(1000000.0);
		ln *= new Double(1000000.0);
		setLat(la.longValue());
		setLng(ln.longValue());
		v = new Vector3d();

		setXYZ();
	}

	public Point(Point p) {
		setLat(p.getLatE6());
		setLng(p.getLngE6());
		v = new Vector3d();

		setXYZ();
	}


	public String toString() {
		return new String (lat + ", " + lng);
	}

	public Point inverse() {
		if (this.getLngE6() >0)
			return new Point(-this.getLatE6(),this.getLngE6()-180000000);
		else
			return new Point(-this.getLatE6(),this.getLngE6()+180000000);
	}

	public Double getAngDistance(Point p) {

		return this.getVector().angle(p.getVector());

/*
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
*/
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

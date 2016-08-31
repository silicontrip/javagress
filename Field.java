import java.util.ArrayList; 
import com.google.common.geometry.*;

public class Field {


	//Long[] lat;
	//Long[] lng;	
	
	Portal[] portals;
	Point[] points;
	
	public Field () {
		//lat = new Long[3];
		//lng = new Long[3];
		portals = new Portal[3];
		points = new Point[3];
	}
		
	public Field (Point p0, Point p1, Point p2) {
		this();
		
		if (sign(p0,p1,p2) > 0) { 
			points[0] = new Point(p0);
			points[1] = new Point(p1);
			points[2] = new Point(p2);
		} else {
			points[0] = new Point(p0);
			points[1] = new Point(p2);
			points[2] = new Point(p1);
		}
		
	}
	
	public Field (Portal p0, Portal p1, Portal p2) {
		this(p0.getPoint(),p1.getPoint(),p2.getPoint());
		if (sign(p0.getPoint(),p1.getPoint(),p2.getPoint()) > 0) { 
			portals[0] = p0;
			portals[1] = p1;
			portals[2] = p2;
		} else {
			portals[0] = p0;
			portals[1] = p2;
			portals[2] = p1;
		}

	}

	private void printCell(S2Cell cell) {

			// quick and dirty drawtools output
		S2LatLng p0 = new S2LatLng(cell.getVertex(0));
		S2LatLng p1 = new S2LatLng(cell.getVertex(1));
		S2LatLng p2 = new S2LatLng(cell.getVertex(2));
		S2LatLng p3 = new S2LatLng(cell.getVertex(3));

		System.out.print("[{\"type\":\"polygon\",\"color\":\"#ffffff\",\"latLngs\":[");
		System.out.print("{\"lat\": " + Double.toString(p0.latDegrees()) + ",\"lng\": " + Double.toString(p0.lngDegrees()) + "},");
		System.out.print("{\"lat\": " + Double.toString(p1.latDegrees()) + ",\"lng\": " + Double.toString(p1.lngDegrees()) + "},");
		System.out.print("{\"lat\": " + Double.toString(p2.latDegrees()) + ",\"lng\": " + Double.toString(p2.lngDegrees()) + "},");
		System.out.print("{\"lat\": " + Double.toString(p3.latDegrees()) + ",\"lng\": " + Double.toString(p3.lngDegrees()) + "}");
		System.out.println("]}]");

	}
	
        private static S2Polygon polyFromCell (S2Cell cell) 
        {
                S2PolygonBuilder pb = new S2PolygonBuilder(S2PolygonBuilder.Options.UNDIRECTED_UNION);
                pb.addEdge(cell.getVertex(0),cell.getVertex(1));
                pb.addEdge(cell.getVertex(1),cell.getVertex(2));
                pb.addEdge(cell.getVertex(2),cell.getVertex(3));
                pb.addEdge(cell.getVertex(3),cell.getVertex(0));
                return pb.assemblePolygon();
                
        }

	public double getEstMu() {

		double ttmu=0;
		double area;
		double mukm;

		CellMUDB mudb = CellMUDB.getInstance();

		S2Polygon thisField = getS2Polygon();

		//mukm = 1000;
		for (S2CellId cell: getS2CellUnion()) {

			S2Cell s2cell = new S2Cell(cell);
			S2Polygon intPoly = new S2Polygon();
			S2Polygon cellPoly = polyFromCell(s2cell);

			intPoly.initToIntersection(thisField, cellPoly);
			area = intPoly.getArea() * 6371 * 6371 ;

		//	System.out.print(cell.toToken() + " " );
		//	printCell(s2cell);
			
			// get mu for cellid
			//mudb.getHash().put(cell.toToken(),new Double(mukm));	
			mukm = mudb.getMUKM(cell);

			ttmu += area * mukm;

		}
		//mudb.write();
		return ttmu;
	}


	public S2CellUnion getS2CellUnion() {
		S2RegionCoverer rc = new S2RegionCoverer();

		rc.setMaxLevel(13);
		rc.setMinLevel(0);
		rc.setMaxCells(20);     

		return rc.getCovering(getS2Polygon());
	
	}
	
	public S2Polygon getS2Polygon() {
		S2PolygonBuilder pb = new S2PolygonBuilder(S2PolygonBuilder.Options.UNDIRECTED_UNION);
		pb.addEdge(getS2LatLng(0).toPoint(),getS2LatLng(1).toPoint());
		pb.addEdge(getS2LatLng(1).toPoint(),getS2LatLng(2).toPoint());
		pb.addEdge(getS2LatLng(2).toPoint(),getS2LatLng(0).toPoint());

		return pb.assemblePolygon();

	}
	
	public Portal getPortal(int index) { return portals[index]; }
	public Point getPoint(int index) { return points[index]; }
	public S2LatLng getS2LatLng(int index) { return S2LatLng.fromE6(getLat(index),getLng(index)); }
	
	public void setLat(int index, Long l)
	{
		if (index >=0 && index <= 2) 
		{
			//lat[index] = l;
			points[index].setLat(l);
		}
	}
	
	public void setLng(int index, Long l)
	{
		if (index >=0 && index <= 2) 
		{
			//lng[index] = l;
			points[index].setLng(l);
		}
	}
	
	public Long getLat(int index)
	{
		if (index >=0 && index <= 2) 
		{
			//return lat[index];
			return points[index].getLat();
		}
		return 0L;
	}

	public Long getLng(int index)
	{
		if (index >=0 && index <= 2) 
		{
			//return lng[index];
			return points[index].getLng();
		}
		return 0L;
	}
	
//	public Double getArea () 
////	{
//	
//		return (Math.abs(lng[0] * (lat[1] - lat[2]) +
//				   lng[1] * (lat[2] - lat[0]) +
//				   lng[2] * (lat[0] - lat[1])))/200000.0;
//		
//	}
	
	// this is not true spherical area
	public Double getGeoArea () 
	{

		double a = getLine(0).getGeoDistance();
		double b = getLine(1).getGeoDistance();
		double c = getLine(2).getGeoDistance();
		
		//System.err.println("length: " + a + " - " + b + " - " + c);
		
		double s = ( a + b + c ) / 2.0;
		
		return Math.sqrt(s * (s-a) * (s-b) * (s-c));
		
	}
	
    public Double getGeoPerimeter() {
        return 		getLine(0).getGeoDistance() + getLine(1).getGeoDistance() +getLine(2).getGeoDistance();
    }
	
	public Line getLine(int index) {
		
			
			if (index == 0) {
				return new Line (getLat(0),getLng(0),getLat(1),getLng(1));
			}
		if (index == 1)
		{
			return new Line (getLat(1),getLng(1),getLat(2),getLng(2));
		}
	
		if (index == 2)
		{
			return new Line (getLat(2),getLng(2),getLat(0),getLng(0));
		}

		return null;
		
		
	}

	public boolean touches(Field f) 
	{


		return (f.getPortal(0) == this.getPortal(0)  ||
			f.getPortal(0) == this.getPortal(1)  ||
			f.getPortal(0) == this.getPortal(2)  ||
			f.getPortal(1) == this.getPortal(1)  ||
			f.getPortal(1) == this.getPortal(2)  ||
			f.getPortal(2) == this.getPortal(2)) ;

	}
	
	public boolean intersects(Field f)
	{
		
		boolean intersect = f.getLine(0).intersects(getLine(0)) ||
		f.getLine(0).intersects(getLine(1)) ||
		f.getLine(0).intersects(getLine(2)) ||
		f.getLine(1).intersects(getLine(0)) ||
		f.getLine(1).intersects(getLine(1)) ||
		f.getLine(1).intersects(getLine(2)) ||
		f.getLine(2).intersects(getLine(0)) ||
		f.getLine(2).intersects(getLine(1)) ||
		f.getLine(2).intersects(getLine(2));
	
		/*
		if (!intersect)
		{		
			System.err.println("[" + getLine(0) + "," + getLine(1) + "," + getLine(2) + "," +
								   f.getLine(0) + "," + f.getLine(1) + "," + f. getLine(2) + "]" );
		}
		*/
		
		return intersect;

		
	}
	
    
	protected double sign (Point p1, Point p2, Point p3) 
	{
		return sign (p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng(), p3.getLat(), p3.getLng());
	}
		
    
	protected double sign (double p1a, double p1o, double p2a, double p2o, double p3a, double p3o)
	{
	
		return (p1o - p3o) * (p2a - p3a) - (p2o - p3o) * (p1a - p3a);
		
	}
	
	public boolean inside (Point p)
	{
		//boolean b1 = sign (p.getLat(),p.getLng(),lat[0],lng[0],lat[1],lng[1]) <= 0.0;
		boolean b1 = sign (p,points[0],points[1]) <= 0.0;
		//boolean b2 = sign (p.getLat(),p.getLng(),lat[1],lng[1],lat[2],lng[2]) <= 0.0;
		boolean b2 = sign (p,points[1],points[2]) <= 0.0;
		//boolean b3 = sign (p.getLat(),p.getLng(),lat[2],lng[2],lat[0],lng[0]) <= 0.0;
		boolean b3 = sign (p,points[2],points[0]) <= 0.0;
		
		return (b1 == b2) && (b2 == b3);
		
	}
	public ArrayList<Portal> getNextPortalLink (ArrayList<Portal> p) { return getPortalLinkInc(p,1); }
	public ArrayList<Portal> getPrevPortalLink (ArrayList<Portal> p) { return getPortalLinkInc(p,-1); }
	public ArrayList<Point> getNextLink (ArrayList<Point> p) { return getLinkInc(p,1); }
	public ArrayList<Point> getPrevLink (ArrayList<Point> p) { return getLinkInc(p,-1); }

	public ArrayList<Point> getLinkInc (ArrayList<Point> p,int inc)
	{
		int i1 = getPointIndex(p.get(0));
		int i2 = getPointIndex(p.get(1));
		ArrayList<Point> next = new ArrayList<Point>();

		i1 = (i1 + inc) % 3;
		i2 = (i2 + inc) % 3;

		if (i1 < 0) i1 += 3;
		if (i2 < 0) i2 += 3;

		next.add(getPoint(i1));
		next.add(getPoint(i2));
		return next;

	}
	public ArrayList<Portal> getPortalLinkInc (ArrayList<Portal> p,int inc)
	{
		int i1 = getPointIndex(p.get(0).getPoint());
		int i2 = getPointIndex(p.get(1).getPoint());
		ArrayList<Portal> next = new ArrayList<Portal>();

		i1 = (i1 + inc) % 3;
		i2 = (i2 + inc) % 3;

		if (i1 < 0) i1 += 3;
		if (i2 < 0) i2 += 3;

		next.add(getPortal(i1));
		next.add(getPortal(i2));
		return next;

	}

	public int getPointIndex (Point p)
	{
		if (p.equals(points[0])) return 0;
		if (p.equals(points[1])) return 1;
		if (p.equals(points[2])) return 2;
		return -1;
	}
	
	public boolean equals(Field f) 
	{
	
		return (((f.getLat(0) == this.getLat(0) && f.getLng(0) == this.getLng(0)) ||
				(f.getLat(0) == this.getLat(0) && f.getLng(1) == this.getLng(1)) ||
				(f.getLat(0) == this.getLat(0) && f.getLng(2) == this.getLng(2))) &&
				
				((f.getLat(1) == this.getLat(1) && f.getLng(1) == this.getLng(1)) ||
				 (f.getLat(1) == this.getLat(1) && f.getLng(2) == this.getLng(2)) ||
				 (f.getLat(1) == this.getLat(1) && f.getLng(0) == this.getLng(0))) &&
				 ((f.getLat(2) == this.getLat(2) && f.getLng(2) == this.getLng(2)) ||
				  (f.getLat(2) == this.getLat(2) && f.getLng(0) == this.getLng(0)) ||
				 (f.getLat(2) == this.getLat(2) && f.getLng(1) == this.getLng(1))));
	
				}
	
	public String toString() { return this.getDraw(); } 
	
	public String getDraw() 
	{
		
		return new String ( "{\"type\":\"polygon\",\"latLngs\":[{\"lat\":" + this.getLat(0)/1000000.0 + 
						   ",\"lng\":" + this.getLng(0)/1000000.0 +
						   "},{\"lat\":" + this.getLat(1)/1000000.0 +
						   ",\"lng\":" + this.getLng(1)/1000000.0 +
						   "},{\"lat\":" + this.getLat(2)/1000000.0 +
						   ",\"lng\":" + this.getLng(2)/1000000.0 +
						   "}],\"color\":\"#a05000\"}" );
		
	}
		
}

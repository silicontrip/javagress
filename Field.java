import java.util.ArrayList; 
import java.util.Collection;
import com.google.common.geometry.*;

import javax.xml.parsers.*;
import java.io.*;

public class Field {

	 public static final Double earthRadius = 6367.0;
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
		this((Point)p0,(Point)p1,(Point)p2);
		if (sign(p0,p1,p2) > 0) {
			portals[0] = p0;
			portals[1] = p1;
			portals[2] = p2;
		} else {
			portals[0] = p0;
			portals[1] = p2;
			portals[2] = p1;
		}

	}

	public double getEstMu() throws ParserConfigurationException, IOException {
		CellMUDB mudb = CellMUDB.getInstance();
		return mudb.getEstMu(this);
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
			return points[index].getLatE6();
		}
		return 0L;
	}

	public Long getLng(int index)
	{
		if (index >=0 && index <= 2) 
		{
			//return lng[index];
			return points[index].getLngE6();
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
	
	public Double getGeoArea () 
	{

		double a = getLine(0).getGeoDistance() / earthRadius;
		double b = getLine(1).getGeoDistance() / earthRadius;
		double c = getLine(2).getGeoDistance() / earthRadius;

		double s = ( a + b + c ) / 2.0;
		double e = Math.sqrt( Math.tan(s/2.0) * Math.tan((s-a)/2.0) * Math.tan((s-b)/2.0) * Math.tan((s-c)/2.0) );
		
		return 4 * Math.atan(e) * earthRadius * earthRadius;
		
	}
	
    public Double getGeoPerimeter() {
        return 		getLine(0).getGeoDistance() + getLine(1).getGeoDistance() +getLine(2).getGeoDistance();
    }
	
	// returns the line (link) for the index
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

	public boolean intersects(ArrayList<Field> f) 
	{
	
		for (Field fi: f)
			if (this.intersects(fi))
				return true;
		return false;
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
		
		return intersect;

	}

	public boolean intersects(Line l)
	{
		return l.intersects(getLine(0)) || l.intersects(getLine(1)) || l.intersects(getLine(2));
	}

	public teamCount countIntersects(Collection<Link> links)
	{
		ArrayList<Link> blocks = getIntersects(links);
                teamCount block = new teamCount();

                for (Link li: blocks)
                        block.incTeamEnum(li.getTeamEnum());

                return block;
        }

	public ArrayList<Link> getIntersects (Collection<Link> links)
	{
		ArrayList<Link> allLinks = new ArrayList<Link>();
		Line l1 = getLine(0);
		Line l2 = getLine(1);
		Line l3 = getLine(2);

		for (Link li: links)
		{
			// Line ll = li.getLine();

			if (l1.intersects(li)) {
				allLinks.add(li); 
			} else if (l2.intersects(li)) {
				allLinks.add(li); 
			} else if (l3.intersects(li)) {
				allLinks.add(li); 
			}
		}

	
		return allLinks;	
		
	
	}
    
	protected double sign (Point p1, Point p2, Point p3) 
	{
		return sign (p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng(), p3.getLat(), p3.getLng());
	}
		
    
	protected double sign (double p1a, double p1o, double p2a, double p2o, double p3a, double p3o)
	{
	
		return (p1o - p3o) * (p2a - p3a) - (p2o - p3o) * (p1a - p3a);
		
	}
	
	// not true geo inside
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
	
	public boolean inside(ArrayList<Point> pa)
	{
		
		for (Point pi: pa)
			if (!this.inside(pi))
				return false;
		return true;
		
	}
	
	public Field getInverseCornerField(int corner)
	{
		//throw error or return null

		int alt1=1, alt2=2;
		
		if (corner<0 || corner>2)
			return null;
		
		if (corner == 0) { alt1=1; alt2=2; }
		if (corner == 1) { alt1=0; alt2=2; }
		if (corner == 2) { alt1=0; alt2=1; }
		
		return new Field(points[corner],points[alt1].inverse(),points[alt2].inverse());
		
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
		int i1 = getPointIndex(p.get(0));
		int i2 = getPointIndex(p.get(1));
		ArrayList<Portal> next = new ArrayList<Portal>();

		i1 = (i1 + inc) % 3;
		i2 = (i2 + inc) % 3;

		if (i1 < 0) i1 += 3;
		if (i2 < 0) i2 += 3;

		next.add(getPortal(i1));
		next.add(getPortal(i2));
		return next;

	}

	// determine which anchor equals the supplied point
	public int getPointIndex (Point p)
	{
		if (p.equals(points[0])) return 0;
		if (p.equals(points[1])) return 1;
		if (p.equals(points[2])) return 2;
		return -1;
	}

	public Double difference (Field f)
	{

		Double total = 0.0;
		Double least;

		for (int f1 =0; f1 < 3; f1++)
		{
			least = 9999.9; // max link is 6881km
			for (int f2=0; f2<3; f2++)
			{
				Double ff = this.getPoint(f1).getGeoDistance(f.getPoint(f2));
				if (ff < least)
					least = ff;
			}
			if (least > total)
				total = least;
			//total += least;
		}	

		return total;

	}

	
	
	// compare two fields have the same anchor points
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

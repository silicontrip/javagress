import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.io.IOException;



public class DrawTools {

	private ArrayList<PolyObject> entities;
	private String colour;
	private int outputType = 0;
	private final double zoomView = 0.075;

	public DrawTools() {
		entities = new ArrayList<PolyObject>();	
		colour = "#ffffff";
	}

	public DrawTools(String clusterDescription) throws IOException
	{
		this();
		ObjectMapper mapper = new ObjectMapper();
                
		JsonNode dtObj = mapper.readTree(clusterDescription);
		for (JsonNode node: dtObj)
		{
			String type = node.path("type").asText();
			String colour = node.path("color").asText();
			if ("polygon".equalsIgnoreCase(type)) 
			{ 
				ArrayList<PolyPoint> pp = makeLatLngs(node.path("latLngs"));
				entities.add(new Polygon(pp,colour));
			}
			if ("polyline".equalsIgnoreCase(type)) { 
				ArrayList<PolyPoint> pp = makeLatLngs(node.path("latLngs"));
				entities.add(new Polyline(pp,colour));
			}
			if ("marker".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.path("latLng"));
				entities.add(new Marker(pp,colour));
			}
			if ("circle".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.path("latLng"));
				entities.add(new Circle(pp,node.path("radius").asText(),colour));
			}
		
		}

                
		// should do some validity checking.
                
	
	}

	public void jsonNodeParser(String jsonDrawtools) throws IOException
	{

		ObjectMapper mapper = new ObjectMapper();
		JsonNode dtObj = mapper.readTree(jsonDrawtools);

		for (JsonNode node: dtObj)
		{
			String type = node.path("type").asText();
			String colour = node.path("color").asText();
			if ("polygon".equalsIgnoreCase(type)) 
			{ 
				ArrayList<PolyPoint> pp = makeLatLngs(node.path("latLngs"));
				entities.add(new Polygon(pp,colour));
			}
			if ("polyline".equalsIgnoreCase(type)) { 
				ArrayList<PolyPoint> pp = makeLatLngs(node.path("latLngs"));
				entities.add(new Polyline(pp,colour));
			}
			if ("marker".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.path("latLng"));
				entities.add(new Marker(pp,colour));
			}
			if ("circle".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.path("latLng"));
				entities.add(new Circle(pp,node.path("radius").asText(),colour));
			}
		
		}

	}

	public void erase() {entities = new ArrayList<PolyObject>(); }
	public void setOutputAsPolyline() { outputType = 1; }  // I know I should ENUM this
	public void setOutputAsPolygon() { outputType = 2; }
	public void setOutputAsIntel() { outputType = 3; }
	public void setOutputAsIs() { outputType = 0; }

	public void setDefaultColour(String c) { colour = c; }

	public void addLine (Line l) {
		Polyline pg = new Polyline();
		pg.addPoint(new PolyPoint(l.getoLat()/1000000.0,l.getoLng()/1000000.0));
		pg.addPoint(new PolyPoint(l.getdLat()/1000000.0,l.getdLng()/1000000.0));
		pg.setColour(colour);
		entities.add(pg);
	}	
	
	public void addField (Field f) 
	{
		Polygon pg = new Polygon();
		pg.addPoint(new PolyPoint(f.getLat(0)/1000000.0,f.getLng(0)/1000000.0));
		pg.addPoint(new PolyPoint(f.getLat(1)/1000000.0,f.getLng(1)/1000000.0));
		pg.addPoint(new PolyPoint(f.getLat(2)/1000000.0,f.getLng(2)/1000000.0));
		pg.setColour(colour);
		entities.add(pg);
	}	

	public void addMarker (PolyPoint p) { entities.add(new Marker(p,colour)); }
	public void addMarker (String lat, String lng) { addMarker(new PolyPoint(lat,lng)); }
	public void addMarker (Double lat, Double lng) { addMarker(new PolyPoint(lat,lng)); }
	public void addCircle (PolyPoint p, String r) { entities.add(new Circle(p,r,colour)); }
	public void addCircle (String lat, String lng, String r) { addCircle(new PolyPoint(lat,lng),r); }

	public void addLine (PolyPoint p1, PolyPoint p2) {
		Polyline pg = new Polyline();
		pg.addPoint(p1);
		pg.addPoint(p2);
		pg.setColour(colour);
		entities.add(pg);
	}

	public Double getMu() throws javax.xml.parsers.ParserConfigurationException, java.io.IOException
	{

		// normalise plan
		//toLines();
		entities = this.getAsLines();
		entities = this.getAsFields();

		Double totmu = 0.0;
		for (int l1 =0; l1< entities.size(); l1++) {
			PolyObject po = entities.get(l1);
			//System.out.println("Type: " + po.type + ", " + po.EnumType());
			
			if (po.EnumType() == PolyType.POLYGON) {
				Polygon pg = (Polygon)po;
				Point p0 = new Point(pg.latLngs.get(0).lat,pg.latLngs.get(0).lng);
				Point p1 = new Point(pg.latLngs.get(1).lat,pg.latLngs.get(1).lng);
				Point p2 = new Point(pg.latLngs.get(2).lat,pg.latLngs.get(2).lng);
				Field fi = new Field(p0,p1,p2);
			//	System.out.println("sz: " + fi.getGeoArea());
				totmu += fi.getEstMu();
			}
		}
		return totmu;	
	}
	
	public ArrayList<PolyObject> getAsFields() {
		ArrayList<PolyObject> asFields = new ArrayList<PolyObject>();
		HashSet<PolyObject> ff = new HashSet<PolyObject>();
		for (int l1 =0; l1< entities.size(); l1++) {
			PolyObject dto1 = entities.get(l1);
			int l3=0;
			PolyObject dto3;
			if (dto1.EnumType() == PolyType.POLYLINE) {
				Polyline do1 = (Polyline)dto1;
				for (int l2=0; l2 <entities.size(); l2++) {

					PolyObject dto2 = entities.get(l2);
					//var nff={};
					if (dto2.EnumType() == PolyType.POLYLINE) {
						Polyline do2 = (Polyline)dto2;
						if (do1.latLngs.get(0).equals(do2.latLngs.get(0))) {
							for ( l3=0; l3 <entities.size(); l3++) {
								dto3 = entities.get(l3);
								if (dto3.EnumType() == PolyType.POLYLINE) {
									Polyline do3 = (Polyline)dto3;
									if ((do1.latLngs.get(1).equals(do3.latLngs.get(0)) && do2.latLngs.get(1).equals(do3.latLngs.get(1))) || (do1.latLngs.get(1).equals(do3.latLngs.get(1)) && do2.latLngs.get(1).equals(do3.latLngs.get(0)) )) {
										Polygon nff = new Polygon();
										nff.addPoint(do1.latLngs.get(0));
										nff.addPoint(do1.latLngs.get(1));
										nff.addPoint(do2.latLngs.get(0));
										nff.setColour(colour);
										ff.add(nff);
									}
								}
							}
						}
						if (do1.latLngs.get(0).equals(do2.latLngs.get(1))) {
							for ( l3=0; l3 <entities.size(); l3++) {
								dto3 = entities.get(l3);
								if (dto3.EnumType() == PolyType.POLYLINE) {
									Polyline do3 = (Polyline)dto3;
									if ((do1.latLngs.get(1).equals(do3.latLngs.get(0)) && do2.latLngs.get(0).equals(do3.latLngs.get(1))) || (do1.latLngs.get(1).equals(do3.latLngs.get(1)) && do2.latLngs.get(0).equals(do3.latLngs.get(0)) )) {
										Polygon nff = new Polygon();
										nff.addPoint(do1.latLngs.get(0));
										nff.addPoint(do1.latLngs.get(1));
										nff.addPoint(do2.latLngs.get(0));
										nff.setColour(colour);
										ff.add(nff);

									}
								}
							}
						}
					}
				}
			} else {
				asFields.add(dto1);
			}

		}
		asFields.addAll(ff);
		return asFields;
	}

	public ArrayList<PolyObject> getAsLines() {

		ArrayList<PolyObject> asLines = new ArrayList<PolyObject>();
		HashSet<PolyObject> lines = new HashSet<PolyObject>();

		//for (PolyObject po: entities)
		for (int i=0; i < entities.size(); i++)
		{
			PolyObject po = entities.get(i);
			if (po.EnumType() == PolyType.POLYGON)
			{
				//System.out.println("Drawtools::toLines polygon");
				// delete this and add line
				// would like to check for duplicates
				Polygon pog = (Polygon)po;
				PolyPoint oldpoint = null;
				PolyPoint firstpoint = null;
				for (PolyPoint pp: pog.latLngs)
				{
					if (oldpoint != null)
					{
						Polyline pg = new Polyline();
						pg.addPoint(oldpoint);
						pg.addPoint(pp);
						pg.setColour(colour);
						lines.add(pg);
						
						// addLine(oldpoint,pp);
					} else {
						firstpoint = pp;
					}
					oldpoint = pp;  
					
				}
				Polyline pg = new Polyline();
				pg.addPoint(oldpoint);
				pg.addPoint(firstpoint);
				pg.setColour(colour);
				lines.add(pg);

				// addLine(oldpoint,firstpoint);
				
			} else {
				asLines.add(po);
			}
		}

		// add hashSet to arraylist
		asLines.addAll(lines);

		return asLines;
		
	}
	
	public String asIntelLink() {
		
		ArrayList<PolyObject> pos  = this.getAsLines();
		String intelLink = "https://www.ingress.com/intel?pls=";
		boolean first = true;
		double maxLength = 0;
		double centreLat =0;
		double centreLng =0;
		double pointCount = 0;
		for (PolyObject po: pos)
			if (po.EnumType() == PolyType.POLYLINE)
			{
				Polyline pl = (Polyline) po;

				double d = pl.asLine().getGeoDistance();
				if (d> maxLength)
					maxLength = d;

				centreLat += Double.parseDouble(pl.latLngs.get(0).lat);
				centreLng += Double.parseDouble(pl.latLngs.get(0).lng);
				centreLat += Double.parseDouble(pl.latLngs.get(1).lat);
				centreLng += Double.parseDouble(pl.latLngs.get(1).lng);
				pointCount +=2;

				if (!first) 
					intelLink = new String(intelLink + "_");
			
				intelLink = new String (intelLink + pl);
				first = false;
			}
		// add zoom and centre
			// 21 = max browser zoom.
			Double zoom =  21 - Math.log(maxLength / zoomView) / Math.log(2);
			//System.out.println("max: " + maxLength + " zoom: " + zoom);

			centreLat /= pointCount;
			centreLng /= pointCount;
			// round these to E6 format...
			centreLat = Math.round(centreLat*1000000)/1000000;
			centreLng = Math.round(centreLng*1000000)/1000000;

			intelLink = new String (intelLink + "&ll=" + centreLat + "," + centreLng+"&z="+zoom.intValue());
		return intelLink;
	}
	public String toString() { 
		if (outputType == 1)
			return entToString(this.getAsLines());
		if (outputType == 2)
			return entToString(this.getAsFields());
		if (outputType == 3)
			return this.asIntelLink();
	// default as is
		return entToString(entities);
	}
			

	protected static String entToString(ArrayList<PolyObject> ent)
	{

		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(ent);
		} catch (Exception e) {
			// probably not the best thing to do with the exception.
			return e.getMessage();
		}
	}
// thinking about deprecating this.
	public String out () { return this.toString();	}

	public int size ()  { return entities.size(); }
	public int countPolygons() {
		int count =0;
		for (PolyObject po: entities)
			if (po.EnumType() == PolyType.POLYGON)
				count++;
		return count;
	}
	private ArrayList<PolyPoint> makeLatLngs(JsonNode latLngs)
	{
		ArrayList<PolyPoint> pt = new ArrayList<PolyPoint>();
		for (JsonNode jPoint: latLngs)
			pt.add(new PolyPoint(jPoint));
		return pt;
	}
			
		


}
	
//*********************************************************************************************
//** Poly objects
//*********************************************************************************************

abstract class PolyObject {
	public final String type;
	public String color = "#ffffff";

	PolyObject (String t) { type = t; }	
	public void setColour (String c) { color = c; }
	public PolyType EnumType() { 
		if ("polygon".equalsIgnoreCase(type)) { return PolyType.POLYGON; }
		if ("polyline".equalsIgnoreCase(type)) { return PolyType.POLYLINE; }
		if ("marker".equalsIgnoreCase(type)) { return PolyType.MARKER; }
		if ("circle".equalsIgnoreCase(type)) { return PolyType.CIRCLE; }
		return PolyType.UNKNOWN;
	}
}

class Polygon extends PolyObject {
	public ArrayList<PolyPoint> latLngs;
		
	public Polygon () { super ("polygon"); latLngs = new ArrayList<PolyPoint>(); }
	public Polygon(ArrayList<PolyPoint> pp) { super("polygon"); latLngs = pp; }
	public Polygon(ArrayList<PolyPoint> pp, String c) { super("polygon"); latLngs = pp; setColour(c); }
	public void addPoint(PolyPoint pp) { latLngs.add(pp); }
	public ArrayList<PolyPoint> getLatLngs() { return latLngs; }
	@Override
	public boolean equals (Object o)
	{
		if (o == this) return true;
		if (!(o instanceof Polygon)) return false;
		Polygon l = (Polygon) o;
		if (l.latLngs.size() != this.latLngs.size()) return false;
		
		// can only work this out for 3 points
		// only cyclic order is important
		// direction is not important
		// starting point is not important
		
		return ((this.latLngs.get(0).equals(l.latLngs.get(0)) &&
			this.latLngs.get(1).equals(l.latLngs.get(1)) &&
			this.latLngs.get(2).equals(l.latLngs.get(2)) ) ||
			(this.latLngs.get(0).equals(l.latLngs.get(1)) &&
			 this.latLngs.get(1).equals(l.latLngs.get(2)) &&
			 this.latLngs.get(2).equals(l.latLngs.get(0)) ) ||
			(this.latLngs.get(0).equals(l.latLngs.get(2)) &&
			 this.latLngs.get(1).equals(l.latLngs.get(0)) &&
			 this.latLngs.get(2).equals(l.latLngs.get(1)) ) ||
			(this.latLngs.get(0).equals(l.latLngs.get(0)) &&
			 this.latLngs.get(1).equals(l.latLngs.get(2)) &&
			 this.latLngs.get(2).equals(l.latLngs.get(1)) ) ||
			(this.latLngs.get(0).equals(l.latLngs.get(2)) &&
			 this.latLngs.get(1).equals(l.latLngs.get(1)) &&
			 this.latLngs.get(2).equals(l.latLngs.get(0)) ) ||
			(this.latLngs.get(0).equals(l.latLngs.get(1)) &&
			 this.latLngs.get(1).equals(l.latLngs.get(0)) &&
			 this.latLngs.get(2).equals(l.latLngs.get(2)) ));
		
		
	}
	@Override
	public int hashCode()
	{
		// the cyclic order is important
		// but can't work it out.
		int hc=0;
		for (int i =0; i < this.latLngs.size(); i++)
			hc |= this.latLngs.get(i).hashCode();
		return hc;
	}
}

class Polyline extends PolyObject {
	public ArrayList<PolyPoint> latLngs;

	public Polyline () { super("polyline"); latLngs = new ArrayList<PolyPoint>(); }
	public Polyline(ArrayList<PolyPoint> pp, String c) { super("polyline"); latLngs = pp; setColour(c); }
	public Line asLine() {  return new Line(latLngs.get(0).asPoint(),latLngs.get(1).asPoint()); }
	public void addPoint(PolyPoint pp) { latLngs.add(pp); }
	@Override
	public boolean equals (Object o)
	{
		if (o == this) return true;
		if (!(o instanceof Polyline)) return false;
		Polyline l = (Polyline) o;
		if (l.latLngs.size() != this.latLngs.size()) return false;
		boolean equal = true;
		for (int i =0; i < this.latLngs.size(); i++)
			if ((!this.latLngs.get(i).equals(l.latLngs.get(i))) &&
			(!this.latLngs.get(i).equals(l.latLngs.get(this.latLngs.size()-i-1))))
				return false;
		
		return true;
	}
	@Override
	public int hashCode()
	{
		// honestly, who uses more than 2 points?
		if (this.latLngs.size()==2)
			return this.latLngs.get(0).hashCode() | this.latLngs.get(1).hashCode();
		int hc=0;
		for (int i =0; i < (this.latLngs.size()/2); i++)
		{
			hc *= 31;
			hc += this.latLngs.get(i).hashCode() | this.latLngs.get(this.latLngs.size()-i-1).hashCode();
		}
		return hc;
	}
	// only works with 1 line segment.  toLines generates this anyway.
	public String toString() { return latLngs.get(0) + "," +latLngs.get(1); }
	
}

class Marker extends PolyObject {
	public PolyPoint latLng;

	public Marker(PolyPoint pp) { super("marker"); setPoint(pp); }
	public Marker(PolyPoint pp,String c) { super("marker"); setPoint(pp); setColour(c); }
	public void setPoint (PolyPoint pp) { latLng = pp; }
}

class Circle extends PolyObject {
	public PolyPoint latLng;
	public String radius;

	public Circle (PolyPoint p, String r) { super ("circle"); setPoint(p); setRadius(r); }
	public Circle (PolyPoint p, String r, String c) { super ("circle"); setPoint(p); setRadius(r); setColour(c); }
	public void setPoint (PolyPoint pp) { latLng = pp; }
	public void setRadius (String r) { radius = r; }
}

class PolyPoint {
	public final String lat;
	public final String lng;
	public PolyPoint() { lat = "0.0"; lng = "0.0"; }
	public PolyPoint(String a, String o) { lat = a; lng = o; }
	public PolyPoint(Double a, Double o) { this(String.valueOf(a),String.valueOf(o)); }
	public PolyPoint(JsonNode jPoint) { this(jPoint.path("lat").asText(),jPoint.path("lng").asText()); }
	public Point asPoint() { return new Point(lat,lng); }

	@Override
	public boolean equals (Object o)
	{
		if (o == this) return true;
		if (!(o instanceof PolyPoint)) return false;
		PolyPoint l = (PolyPoint) o;
	
		return (this.lat.equals(l.lat) && this.lng.equals(l.lng));
	}
	@Override 
	public int hashCode ()
	{
		return this.lat.hashCode() * 2 + (this.lng.hashCode() * 2 + 1);
	}
	
	public String toString () { return (lat + "," + lng); }
}


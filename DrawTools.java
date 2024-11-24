
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;


public class DrawTools {

	private ArrayList<PolyObject> entities;
	private String colour;
	private int outputType = 0;
	private final double zoomView = 0.075;

	public DrawTools() {
		entities = new ArrayList<PolyObject>();	
		colour = "#ffffff";
	}

	public DrawTools(DrawTools dt)
	{
		entities = new ArrayList<PolyObject>();	
		for (int i =0; i < dt.size(); i++)
			entities.add(dt.get(i));
		colour = new String(dt.getColour());
	}

	public DrawTools(String clusterDescription) throws IOException
	{
		this();
		//ObjectMapper mapper = new ObjectMapper();
                
		//JsonNode dtObj = mapper.readTree(clusterDescription);
		JSONArray dtObj = new JSONArray(clusterDescription);
		for (Object o: dtObj)
		{
			JSONObject node = (JSONObject)o;

			String type = node.getString("type");
			String colour = node.getString("color");
			if ("polygon".equalsIgnoreCase(type)) 
			{ 
				ArrayList<PolyPoint> pp = makeLatLngs(node.getJSONArray("latLngs"));
				entities.add(new Polygon(pp,colour));
			}
			if ("polyline".equalsIgnoreCase(type)) { 
				ArrayList<PolyPoint> pp = makeLatLngs(node.getJSONArray("latLngs"));
				entities.add(new Polyline(pp,colour));
			}
			if ("marker".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.getJSONObject("latLng"));
				entities.add(new Marker(pp,colour));
			}
			if ("circle".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.getJSONObject("latLng"));
				entities.add(new Circle(pp,node.getString("radius"),colour));
			}
		
		}

                
		// should do some validity checking.
                
	
	}

    // Method to get unique points from all entities across all polyObjects
    public PolyPoint[] getUniquePoints() {
        Set<PolyPoint> uniquePoints = new HashSet<>();

        for (PolyObject object : entities) {
            uniquePoints.addAll(Arrays.asList(object.getPoints()));
        }

        return uniquePoints.toArray(new PolyPoint[uniquePoints.size()]);
    }

	public void jsonNodeParser(String jsonDrawtools) throws IOException
	{

		//ObjectMapper mapper = new ObjectMapper();
		//JsonNode dtObj = mapper.readTree(jsonDrawtools);

        JSONArray dtObj = new JSONArray(jsonDrawtools);

		for (Object o: dtObj)
		{
			JSONObject node = (JSONObject)o;

			String type = node.getString("type");
			String colour = node.getString("color");
			if ("polygon".equalsIgnoreCase(type)) 
			{ 
				//ArrayList<PolyPoint> pp = makeLatLngs(node.path("latLngs"));
				ArrayList<PolyPoint> pp = makeLatLngs(node.getJSONArray("latLngs"));

				entities.add(new Polygon(pp,colour));
			}
			if ("polyline".equalsIgnoreCase(type)) { 
				ArrayList<PolyPoint> pp = makeLatLngs(node.getJSONArray("latLngs"));
				entities.add(new Polyline(pp,colour));
			}
			if ("marker".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.getJSONObject("latLng"));
				entities.add(new Marker(pp,colour));
			}
			if ("circle".equalsIgnoreCase(type)) { 
				PolyPoint pp = new PolyPoint(node.getJSONObject("latLng"));
				entities.add(new Circle(pp,node.getString("radius"),colour));
			}
		
		}

	}

	public void erase() {entities = new ArrayList<PolyObject>(); }
	public void setOutputAsPolyline() { outputType = 1; }  // I know I should ENUM this
	public void setOutputAsPolygon() { outputType = 2; }
	public void setOutputAsIntel() { outputType = 3; }
	public void setOutputAsIs() { outputType = 0; }

	public void setDefaultColour(String c) { colour = c; }
	public String getColour() { return colour; }

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

	public void addMarker (Point p) { addMarker(p.getLat(),p.getLng()); }

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

				centreLat += pl.latLngs.get(0).lat;
				centreLng += pl.latLngs.get(0).lng;
				centreLat += pl.latLngs.get(1).lat;
				centreLng += pl.latLngs.get(1).lng;
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
			centreLat = Math.round(centreLat*1000000)/1000000.0;
			centreLng = Math.round(centreLng*1000000)/1000000.0;

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
			

// this might be difficult with the different JSON Library.
	protected static String entToString(ArrayList<PolyObject> ent)
	{
		JSONArray jent = new JSONArray();
		for (PolyObject po: ent)
		{
			jent.put(po.getJSONObject());
		}
		return jent.toString();
	}
// thinking about deprecating this.
	public String out () { return this.toString();	}

	public int size ()  { return entities.size(); }
	public PolyObject get(int i) { return entities.get(i); }
	public ArrayList<PolyObject> getEntities() { return entities; }

	public int countPolygons() {
		int count =0;
		for (PolyObject po: entities)
			if (po.EnumType() == PolyType.POLYGON)
				count++;
		return count;
	}
	private ArrayList<PolyPoint> makeLatLngs(JSONArray latLngs)
	{
		ArrayList<PolyPoint> pt = new ArrayList<PolyPoint>();
		for (Object o: latLngs)
		{
			JSONObject jPoint = (JSONObject)o;
			pt.add(new PolyPoint(jPoint));
		}
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
	public JSONObject getJSONObject()
	{
		return new JSONObject();
	}
	public PolyPoint[] getPoints()
	{
		return new PolyPoint[0];
	}
}

class Polygon extends PolyObject {
	public ArrayList<PolyPoint> latLngs;
		
	public Polygon () { super ("polygon"); latLngs = new ArrayList<PolyPoint>(); }
	public Polygon(ArrayList<PolyPoint> pp) { super("polygon"); latLngs = pp; }
	public Polygon(ArrayList<PolyPoint> pp, String c) { super("polygon"); latLngs = pp; setColour(c); }
	public Polygon(PolyPoint p1, PolyPoint p2, PolyPoint p3) {
		super ("polygon");
        this.latLngs = new ArrayList<PolyPoint>();
        this.latLngs.add(p1);
        this.latLngs.add(p2);
        this.latLngs.add(p3);
		ensureClockwise();
    }
	public void ensureClockwise() {
		double sum = 0;
		for(int i=0; i<latLngs.size()-1; i++) {
			PolyPoint p1 = latLngs.get(i);
			PolyPoint p2 = latLngs.get(i+1);
   
			// Subtract the longitude of point1 from that of point2
			double x = (p2.lng - p1.lng) * Math.cos((p1.lat + p2.lat)/2);
   
			// Subtract the latitude of point1 from that of point2
			double y = p2.lat - p1.lat;
   
			sum += x*y;
		}
   
		// If sum is negative, points are counterclockwise. If it's positive, they're clockwise.
		if(sum < 0) {
			Collections.reverse(latLngs);
		}
   }
   
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

	public JSONObject getJSONObject()
	{
		JSONObject jo = new JSONObject();
		JSONArray ll = new JSONArray();
		for (PolyPoint pp: latLngs)
			ll.put(pp.getJSONObject());
		jo.put("latLngs",ll);
		jo.put("color",color);
		jo.put("type",type);
		return jo;
	}

    @Override
    public PolyPoint[] getPoints() {
        return latLngs.toArray(new PolyPoint[latLngs.size()]); // Assuming PolyPoint has an empty constructor
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

	public JSONObject getJSONObject()
	{
		JSONObject jo = new JSONObject();
		JSONArray ll = new JSONArray();
		for (PolyPoint pp: latLngs)
			ll.put(pp.getJSONObject());
		jo.put("latLngs",ll);
		jo.put("color",color);
		jo.put("type",type);
		return jo;
	}
	
	@Override
    public PolyPoint[] getPoints() {
        return latLngs.toArray(new PolyPoint[latLngs.size()]); // Assuming PolyPoint has an empty constructor
    }

}

class Marker extends PolyObject {
	public PolyPoint latLng;

	public Marker(PolyPoint pp) { super("marker"); setPoint(pp); }
	public Marker(PolyPoint pp,String c) { super("marker"); setPoint(pp); setColour(c); }
	public void setPoint (PolyPoint pp) { latLng = pp; }
	@Override
	public JSONObject getJSONObject()
	{
		JSONObject jo = new JSONObject();
		jo.put("latLng",latLng.getJSONObject());
		jo.put("color",color);
		jo.put("type",type);
		return jo;
	}

	@Override
	public PolyPoint[] getPoints()
	{
		return new PolyPoint[]{latLng};
	}
}

class Circle extends PolyObject {
	public PolyPoint latLng;
	public String radius;

	public Circle (PolyPoint p, String r) { super ("circle"); setPoint(p); setRadius(r); }
	public Circle (PolyPoint p, String r, String c) { super ("circle"); setPoint(p); setRadius(r); setColour(c); }
	public void setPoint (PolyPoint pp) { latLng = pp; }
	public void setRadius (String r) { radius = r; }
	@Override
	public JSONObject getJSONObject()
	{
		JSONObject jo = new JSONObject();
		jo.put("latLng",latLng.getJSONObject());
		jo.put("radius",radius);
		jo.put("color",color);
		jo.put("type",type);
		return jo;
	}
	@Override
	public PolyPoint[] getPoints()
	{
		return new PolyPoint[]{latLng};
	}
}

class PolyPoint {
	public Double lat;
	public Double lng;
	public PolyPoint() { lat = 0.0; lng = 0.0; }
	public PolyPoint(Double a, Double o) { lat = a; lng = o; }
	public PolyPoint(String a, String o) { this(Double.valueOf(a),Double.valueOf(o)); }
	//public PolyPoint(JsonNode jPoint) { this(jPoint.path("lat").asText(),jPoint.path("lng").asText()); }
	public PolyPoint(JSONObject jPoint) { this(jPoint.getDouble("lat"),jPoint.getDouble("lng")); }

	public void setLat(Double l) { lat = l; }
	public void setLng(Double l) { lng = l; }

	public JSONObject getJSONObject()
	{
		JSONObject jo = new JSONObject();
		jo.put("lat",lat);
		jo.put("lng",lng);
		//System.out.println(jo.toString());
		return jo;
	}

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


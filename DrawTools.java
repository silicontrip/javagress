import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;  
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.io.IOException;



public class DrawTools {

	private ArrayList<PolyObject> entities;
	private String colour;

	public DrawTools() {
		entities = new ArrayList<PolyObject>();	
		colour = "#ffffff";
	}

	public DrawTools(String clusterDescription) throws IOException
	{
		HashMap<String,HashMap<String,Object>> guidMap;
                ObjectMapper mapper = new ObjectMapper();
                
                ArrayList<PolyObject> tmpObj;
                
                // System.out.println(clusterDescription);
                
                try {
                        tmpObj = mapper.readValue(clusterDescription,new TypeReference<Collection<PolyObject>>() {});
                } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                        throw new IOException("Invalid Drawtools: " + e);
                }
        
                // System.out.println(tmpObj.latLngs);
                
                // there should be only 1 entry
                for (PolyObject entry : tmpObj) {

			System.out.println("Type: " + entry.type);

                }
                
                
                
                
	
	}

	public void setDefaultColour(String c) { colour = c; }

	public void addField (Field f)
	{	
		Polygon pg = new Polygon();
		pg.addPoint(new PolyPoint(f.getLat(0)/1000000.0,f.getLng(0)/1000000.0));
		pg.addPoint(new PolyPoint(f.getLat(1)/1000000.0,f.getLng(1)/1000000.0));
		pg.addPoint(new PolyPoint(f.getLat(2)/1000000.0,f.getLng(2)/1000000.0));
		pg.setColour(colour);
		entities.add(pg);
	}	

	public void addFieldAsLines (Field f)
	{	
		Polyline pg = new Polyline();
		pg.addPoint(new PolyPoint(f.getLat(0)/1000000.0,f.getLng(0)/1000000.0));
		pg.addPoint(new PolyPoint(f.getLat(1)/1000000.0,f.getLng(1)/1000000.0));
		pg.addPoint(new PolyPoint(f.getLat(2)/1000000.0,f.getLng(2)/1000000.0));
		pg.addPoint(new PolyPoint(f.getLat(0)/1000000.0,f.getLng(0)/1000000.0));
		pg.setColour(colour);
		entities.add(pg);
	}	

	public void addMarker (PolyPoint p) { entities.add(new Marker(p,colour)); }
	public void addMarker (String lat, String lng) { addMarker(new PolyPoint(lat,lng)); }
	public void addCircle (PolyPoint p, String r) { entities.add(new Circle(p,r,colour)); }
	public void addCircle (String lat, String lng, String r) { addCircle(new PolyPoint(lat,lng),r); }

	public void addLine (PolyPoint p1, PolyPoint p2) {
		Polyline pg = new Polyline();
		pg.addPoint(p1);
		pg.addPoint(p2);
		entities.add(pg);
	}
		

	public String out () 
	{
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(entities);
		} catch (Exception e) {
			// probably not the best thing to do with the exception.
			return e.getMessage();
		}
	}


}

abstract class PolyObject {
	public String type;
	public String color = "#ffffff";

	public void setColour (String c) { color = c; }
	public PolyType getType() { 
		if ("Polygon".equals(type)) { return PolyType.POLYGON; }
		if ("Polyline".equals(type)) { return PolyType.POLYLINE; }
		if ("Marker".equals(type)) { return PolyType.MARKER; }
		if ("Circle".equals(type)) { return PolyType.CIRCLE; }
		return PolyType.UNKNOWN;
	}
}

class Polygon extends PolyObject {
	public String type = "polygon";
	public ArrayList<PolyPoint> latLngs;
	
	public Polygon () { latLngs = new ArrayList<PolyPoint>(); }
	public Polygon(ArrayList<PolyPoint> pp) { latLngs = pp; }
	public void addPoint(PolyPoint pp) { latLngs.add(pp); }

}

class Polyline extends PolyObject {
	public String type = "polyline";
	public ArrayList<PolyPoint> latLngs;

	public Polyline () { latLngs = new ArrayList<PolyPoint>(); }
	public void addPoint(PolyPoint pp) { latLngs.add(pp); }

}

class Marker extends PolyObject {
	public String type = "marker";
	public PolyPoint latLng;

	public Marker(PolyPoint pp) { setPoint(pp); }
	public Marker(PolyPoint pp,String c) { setPoint(pp); setColour(c); }
	public void setPoint (PolyPoint pp) { latLng = pp; }
}

class Circle extends PolyObject {
	public String type = "circle";
	public PolyPoint latLng;
	public String radius;

	public Circle (PolyPoint p, String r) { setPoint(p); setRadius(r); }
	public Circle (PolyPoint p, String r, String c) { setPoint(p); setRadius(r); setColour(c); }
	public void setPoint (PolyPoint pp) { latLng = pp; }
	public void setRadius (String r) { radius = r; }
}

class PolyPoint {
	public String lat;
	public String lng;
	public PolyPoint() { lat = "0.0"; lng = "0.0"; }
	public PolyPoint(String a, String o) { lat = a; lng = o; }
	public PolyPoint(Double a, Double o) { this(String.valueOf(a),String.valueOf(o)); }

	public String toString () { return (lat + "," + lng); }
}


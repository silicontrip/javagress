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
	private Boolean addFieldAsLine = false;

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
	/*
                for (PolyObject entry : tmpObj) {

			System.out.println("Type: " + entry.type);

                }
	*/
                
                
                
                
	
	}

	public void erase() {entities = new ArrayList<PolyObject>(); }
	public void setFieldsAsPolyline() { addFieldAsLine = true; }
	public void setFieldsAsPolygon() { addFieldAsLine = false; }

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

	public void toLines() {

	//	System.out.println (">>> DrawTools::toLines");

		ArrayList<PolyObject> oldent = entities;
		entities = new ArrayList<PolyObject>();
		HashSet<PolyObject> lines = new HashSet<PolyObject>();

		//for (PolyObject po: entities)
		for (int i=0; i < oldent.size(); i++)
		{
			PolyObject po = oldent.get(i);
	//		System.out.println("Drawtools::toLines entity: " + i + " / " + po.EnumType());
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
	//			System.out.println("Drawtools::toLines not polygon :"+po.type);
				entities.add(po);
			}
		}

	}
		
	public String toString() { return this.out(); }

	public String out () 
	{
		if (addFieldAsLine)
			toLines();
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(entities);
		} catch (Exception e) {
			// probably not the best thing to do with the exception.
			return e.getMessage();
		}
	}

	public int size ()  { return entities.size(); }


}

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
	public void addPoint(PolyPoint pp) { latLngs.add(pp); }
	public ArrayList<PolyPoint> getLatLngs() { return latLngs; }
	@Override
	public equals (Object o)
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
	public HashCode()
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
	public void addPoint(PolyPoint pp) { latLngs.add(pp); }
	@Override
	public equals (Object o)
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
	public HashCode()
	{
		// honestly, who uses more than 2 points?
		if (this.latLngs.size()==2)
			return this.latLngs.get(0).hashCode() | this.latLngs.get(1);
		int hc=0;
		for (int i =0; i < (this.latLngs.size()/2); i++)
		{
			hc *= 31;
			hc += this.latLngs.get(i).hashCode() | this.latLngs.get(this.latLngs.size()-i-1);
		}
		return hc;
	}
	
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

	@Override
	public equals (Object o)
	{
		if (o == this) return true;
		if (!(o instanceof PolyPoint)) return false;
		PolyPoint l = (PolyPoint) o;
	
		return (this.lat.equals(l.lat) && this.lng.equals(l.lng));
	}
	@Override hashCode ()
	{
		return this.lat.hashCode() * 2 + (this.lng.HashCode() * 2 + 1);
	}
	
	public String toString () { return (lat + "," + lng); }
}


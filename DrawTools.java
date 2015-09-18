
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;  
import java.util.Collection;
import java.util.ArrayList;

public class DrawTools {

	private ArrayList<PolyObject> entities;

	public DrawTools() {
		entities = new ArrayList<PolyObject>();	
	}

	

}

abstract class PolyObject {
	public String type;
	public String color = "#ffffff";

	public void setColour (String c) { color = c; }
}

class Polygon extends PolyObject {
	public String type = "polygon";
	public ArrayList<PolyPoint> latLngs;
	
	public Polygon () { latLngs = new ArrayList<PolyPoint>(); }
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

	public void setPoint (PolyPoint pp) { latLng = pp; }
}

class Circle extends PolyObject {
	public String type = "circle";
	public PolyPoint latLng;
	public String radius;

	public void setPoint (PolyPoint pp) { latLng = pp; }
	public void setRadius (String r) { radius = r; }
}

class PolyPoint {
	public String lat;
	public String lng;
	public PolyPoint(String a, String o) {
		lat = a;
		lng = o;
	}

	public String toString () { return (lat + "," + lng); }
}

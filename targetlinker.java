import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class targetlinker {

	private static String drawFields(List<Field> fa)
	{
		
		StringBuilder rs = new StringBuilder(1024);
		
		rs.append("[");
		
		boolean first = true;
		
		for (Field fi: fa)
		{
			if (!first)
			{
				rs.append(",");
			}
			
			rs.append(fi.getDraw());
			first = false;
		}
		rs.append("]");
		
		return rs.toString();
		
	}
	
	private static boolean newFieldIntersect (List<Field> fa,Field f)
	{
		for (Field fi: fa) {
			if (fi.intersects(f)) { return true; }
			if (fi.equals(f)) { return true; }
			
			//System.err.println("no intersect: [" + fi + "," + f + "]");
			
			
		}
		return false;
	}
	
	private static ArrayList<Field> getFieldsOverPoint (Point p, HashMap<String,Portal> portals)
	{

		ArrayList <Field> fields = new ArrayList<Field>();
		String[] portalKeys = portals.keySet().toArray(new String[portals.size()]);

		for (int li = 0; li < portals.size(); li++)
		{
         
		    Portal pki = portals.get(portalKeys[li]);

		    for (int lj = li+1; lj < portals.size(); lj++)
		    {
			Portal pkj = portals.get(portalKeys[lj]);

			    for (int lk = lj+1; lk < portals.size(); lk++)
			    {
				Portal pkk = portals.get(portalKeys[lk]);
                                Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
				// check if field is over point.
				// add field to array
				if (fi.inside(p))
					fields.add(fi);
				}
			}
		}
		return fields;
	}
	
	private static Double sizeFields(List<Field> fa)
	{
		Double area = 0.0;
		
		for (Field fi: fa)
		{
			area += fi.getGeoArea();
		}
		return area;
	}
	
public static void main(String[] args) {

	Point target ;
	ArrayList <Field> fieldList = new ArrayList<Field>();

        Arguments ag = new Arguments(args);

        //System.out.println ("Arguments: " + ag );

        teamCount maxBl = new teamCount(ag.getOptionForKey("E"),ag.getOptionForKey("R"));

        DrawTools dt = new DrawTools();
        if (ag.hasOption("C"))
                dt.setDefaultColour(ag.getOptionForKey("C"));
        else
                dt.setDefaultColour("#a24ac3");

        if (ag.hasOption("L"))
                dt.setFieldsAsPolyline();
        else
                dt.setFieldsAsPolygon();



	Float lat = Float.parseFloat(ag.getArgumentAt(0));
	Float lng = Float.parseFloat(ag.getArgumentAt(1));
    
	target = new Point(lat,lng);
    try {
	// get point
        PortalFactory pf = PortalFactory.getInstance();
        
        System.err.println("== Reading portals ==");
        
        HashMap<String,Portal> portals = pf.portalClusterFromString(ag.getArgumentAt(2));
        System.err.println("== calculating all fields ==");
	ArrayList<Field> validFields = getFieldsOverPoint(target,portals);

	// find smallest that doesn't intersect field list
        
        System.err.println("== searching fields ==");
	Double max = 0.0;
	Field thisField=null;
	while (max != 10000) 
	{
		max = 10000.0;
		for (Field fi: validFields) {
			if (!newFieldIntersect(fieldList,fi))
			{
				if (fi.getGeoPerimeter() < max) 
				{
					max=fi.getGeoPerimeter();
					thisField = fi;
				}
			}

		}
        // System.err.println(thisField.getDraw());
		fieldList.add(thisField);
		dt.addField(thisField);	
	}
	
// print plan

        System.out.println(dt.out());

        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
    
}

}

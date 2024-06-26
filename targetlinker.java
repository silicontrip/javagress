import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.*;


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

	private static ArrayList<Field> removeIntersecting ( List<Field> fa,Field f )
	{
		ArrayList<Field> nf = new ArrayList<Field>();
		for (Field fi: fa) {
			if (!fi.intersects(f) && !fi.equals(f))
				nf.add(fi);

                }
		return nf;	
	}

	private static ArrayList<Field> getFieldsOverPoint (ArrayList<Point> p, HashMap<String,Portal> portals)
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
                                Field fi = new Field (pki,pkj,pkk);
				// check if field is over point.
				// add field to array
				if (fi.inside(p))
					fields.add(fi);
				}
			}
		}
		return fields;
	}

	private static ArrayList<Field> getFieldsOverPoints (ArrayList<Point> p, HashMap<String,Portal> portals)
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
                                Field fi = new Field (pki,pkj,pkk);
				
				boolean inside = true;
				// check if field is over point.
				// add field to array
				for (int pp =0; pp < p.size(); pp++)
					if (!fi.inside(p.get(pp))) {
						inside = false;
						break;
					}
	
				if (inside)
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
	
private static ArrayList<Portal> getCadencePortals(int c, Field f)
{

	if (f == null)
		return null;
	int nc = c % 3;
	ArrayList<Portal> cp = new ArrayList<Portal>();

	cp.add(f.getPortal(nc));
	nc ++;
	nc = nc % 3;
	cp.add(f.getPortal(nc));
	
	return cp;
}

private static boolean matchPortals(ArrayList<Portal> p, Field f)
{
	if  (p==null || f==null)
		return true;

	Portal p0 = p.get(0);
	Portal p1 = p.get(1);

	Portal f0 = f.getPortal(0);
	Portal f1 = f.getPortal(1);
	Portal f2 = f.getPortal(2);

	return( (p0.equals(f0) || p0.equals(f1) || p0.equals(f2)) && (p1.equals(f0) || p1.equals(f1) || p1.equals(f2))); 
}

private static boolean notMatchPortals(ArrayList<Portal> p, Field f)
{
	if  (p==null || f==null)
		return true;

	Portal p0 = p.get(0);
	Portal p1 = p.get(1);

	Portal f0 = f.getPortal(0);
	Portal f1 = f.getPortal(1);
	Portal f2 = f.getPortal(2);

	return !( (p0.equals(f0) || p0.equals(f1) || p0.equals(f2)) && (p1.equals(f0) || p1.equals(f1) || p1.equals(f2))); 
}
	
public static void main(String[] args) {

//	Point target ;
        ArrayList<Point> target=null;

	ArrayList <Field> fieldList = new ArrayList<Field>();
	int cyclone_cadence =-1; // set by command line
	Arguments ag = new Arguments(args);

	System.out.println ("Arguments: " + ag );

	teamCount maxBl = new teamCount(ag.getOptionForKey("E"),ag.getOptionForKey("R"),ag.getOptionForKey("N"));

	DrawTools dt = new DrawTools();
	if (ag.hasOption("C"))
		dt.setDefaultColour(ag.getOptionForKey("C"));
	else
		dt.setDefaultColour("#a24ac3");

                if (ag.hasOption("L"))
                        dt.setOutputAsPolyline();
                if (ag.hasOption("O"))
                        dt.setOutputAsIntel();

    try {
        PortalFactory pf = PortalFactory.getInstance();

	if (ag.hasOption("c"))
		cyclone_cadence = new Integer(ag.getOptionForKey("c")).intValue();

	System.err.println("Cadence pattern: " + cyclone_cadence);


	if (ag.hasOption("T"))
		target = pf.getPointsFromString(ag.getOptionForKey("T"));



	// make sure that there is a target...
    
	// get point
        
        System.err.println("== Reading portals ==");
        
        HashMap<String,Portal> portals = pf.portalClusterFromString(ag.getArgumentAt(0));
        System.err.println("== calculating all fields ==");
		ArrayList<Field> validFields = getFieldsOverPoint(target,portals);

		System.err.println("Found " + validFields.size() + " fields");

	// find smallest that doesn't intersect field list
        
		System.err.println("== searching fields ==");
		Double max = 0.0;
		Field thisField=null;
		ArrayList<Portal> cadencePortals = null;
		while (max != 10000)
		{
			max = 10000.0;
			for (Field fi: validFields) {
				if (!newFieldIntersect(fieldList,fi))
				{
			// and portals match cadence
					if (cyclone_cadence >= 0) {
					// get cadence portals
					// check field matches cadence
						if (matchPortals(cadencePortals,fi)) {
							if (fi.getGeoPerimeter() < max)
							{
								max=fi.getGeoPerimeter();
								thisField = fi;
							}
						}
					} else {
						if (ag.hasOption("r"))
						{
							if (notMatchPortals(cadencePortals,fi)) {
								if (fi.getGeoPerimeter() < max)
								{
									max=fi.getGeoPerimeter();
									thisField = fi;
								}
							}
						} else {
							if (fi.getGeoPerimeter() < max)
							{
								max=fi.getGeoPerimeter();
								thisField = fi;
							}
						}
					}
				}
			}
        // System.err.println(thisField.getDraw());
			if (max != 10000) {
				fieldList.add(thisField);
				dt.addField(thisField);
				validFields = removeIntersecting(validFields,thisField);
	
				if (cyclone_cadence >= 0) {
					if (cadencePortals == null)  {
						cadencePortals = getCadencePortals(cyclone_cadence,thisField);
						System.err.println("Cadence: " + cadencePortals);
					} else {
						System.err.print("Cadence before: " + cadencePortals);
						if (ag.hasOption("r"))
							cadencePortals = thisField.getPrevPortalLink(cadencePortals);
						else
							cadencePortals = thisField.getNextPortalLink(cadencePortals);
						System.err.println("  after: " + cadencePortals);
					}
				}
			}
		}
	
// print plan
	
		System.out.println("Layers: " + dt.size());
		System.out.println(dt.out());

        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
    
}

}

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class maxfields {

	private static String drawFields(List<Field> fa,DrawTools dt)
	{
		
		dt.erase();
		
		for (Field fi: fa)
			dt.addField(fi);
		
		return dt.out();
		
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

	private static int iterSearchFields (DrawTools dt, Object[] fields)
	{

		int mostFields = 0;

		ArrayList<Integer> sStack = new ArrayList<Integer>();
		ArrayList<ArrayList<Field>> fStack = new ArrayList<ArrayList<Field>>();

		while (sStack.size() > 0) {

			System.out.println ("size: " + sStack.size());

			ArrayList<Field> list = fStack.remove(fStack.size()-1);
			Integer start = sStack.remove(sStack.size()-1);
			if (list.size() > 0) {
				int thisSize = list.size();
				if (thisSize > mostFields)
				{
					mostFields = thisSize;
					System.out.println(thisSize + " : " + drawFields(list,dt));
					System.out.println("");
				}
			}

			for (int i =start; i<fields.length; i++)
			{
				Field thisField = (Field)fields[i];
				if (!thisField.intersects(list))
//				if (!newFieldIntersect(list,thisField))
				{
					ArrayList<Field> newlist = new ArrayList<Field>(list);
					newlist.add((Field)fields[i]);
					

					sStack.add (new Integer(i+1));
					fStack.add (newlist);

				}
			}

		}

		return mostFields;
		

	}
	
	private static Double searchFields (DrawTools dt, ArrayList<Field> list, Object[] fields, int start, Double maxArea,int depth)
	{
			if (list.size() > 0) {
				
			//	Double thisArea = sizeFields(list);
				// we want to maximise number of fields
				Double thisArea = new Double(list.size());
				
				if (thisArea > maxArea) {
					System.out.println(thisArea + " : " + drawFields(list,dt));
					System.out.println("");
					maxArea = thisArea;
				}
			}
			
			for (int i =start; i<fields.length; i++)
			{
				Field thisField = (Field)fields[i];
				
				//	System.err.println(" - new Field Intersect - ");
				if (!thisField.intersects(list))
				// if (!newFieldIntersect(list,thisField))
				{
					//		System.err.println(" = END new Field Intersect = ");
					
					
					ArrayList<Field> newlist = new ArrayList<Field>(list);
					newlist.add((Field)fields[i]);
					
					/* this was an integrity check and is redundant.
					if (fieldIntersect(newlist)) {
						throw new RuntimeException("Field Collision : " + drawFields(list,dt) + " / " + thisField );
						
					}
					*/
					
					maxArea = searchFields(dt,newlist,fields,i+1,maxArea,depth+1);
				}
				
			}
		
		return maxArea;
	}

public static void main(String[] args) {

	ArrayList<Point> target=null;
	
	
	Arguments ag = new Arguments(args);

	
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
		
		
		if (ag.hasOption("T"))
			target = pf.getPointsFromString(ag.getOptionForKey("T"));

        System.err.println("== Reading portals ==");
        
        HashMap<String,Portal> portals = pf.portalClusterFromString(ag.getArgumentAt(0));
        ArrayList<Link> links = pf.getPurgedLinks(portals.values());
		ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
		// check that maxBl is set
		ArrayList<Line> l2 = pf.filterLinks(li,links,maxBl);
		ArrayList<Field> allfields = pf.makeFieldsFromSingleLinks(l2);

		ArrayList<Field> fiList = new ArrayList<Field>();
		
		for (Field fi: allfields) {
			if (target==null || fi.inside(target))
				fiList.add(fi);
		}
		
		System.out.println("Total Fields: " + fiList.size());

			// sort through colliding fields.
		
		// iterSearchFields(dt,  fiList.toArray());
		searchFields(dt, new ArrayList<Field>() , fiList.toArray(),0,0.0,0);
		
      //  System.out.println("]");

        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
	
}

}

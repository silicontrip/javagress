import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class maxlinks {

	private static String drawFields(List<Line> fa,DrawTools dt)
	{
		
		dt.erase();
		
		for (Line fi: fa)
			dt.addLine(fi);
		
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


    private static Double calculateBalanceScore(List<Line> links) {
        HashMap<Point, Integer> linkCounts = new HashMap<>();

        for (Line line : links) {
            Point[] portals = line.getPoints();
            for (Point portal : portals) {
                linkCounts.put(portal, linkCounts.getOrDefault(portal, 0) + 1);
            }
        }

        int totalLinks = 0;
        int totalPortals = linkCounts.size();
        for (int count : linkCounts.values()) {
            totalLinks += count;
        }

        double mean = (double) totalLinks / totalPortals;
        double variance = 0.0;

        for (int count : linkCounts.values()) {
            variance += Math.pow(count - mean, 2);
        }

        variance /= totalPortals;
        return Math.sqrt(variance); // return the standard deviation as the balance score
    }

	private static Object[] searchFields (DrawTools dt, ArrayList<Line> list, Object[] links, int start, int maxArea,int depth, boolean sameSize, Double balance)
	{
			if (list.size() > 0) {
				
			//	Double thisArea = sizeFields(list);
				// we want to maximise number of fields
				// Double thisArea = new Double(list.size());
				int thisArea = list.size();


				if ((thisArea > maxArea) || (sameSize && thisArea == maxArea))
				{
					Double bal = calculateBalanceScore(list);

					if (thisArea > maxArea || bal < balance) {
						System.out.println("" + bal + " : " + thisArea + " : " + drawFields(list,dt));
						System.out.println("");
						balance = bal;
					}

					maxArea = thisArea;


				}
			}
			
			for (int i =start; i<links.length; i++)
			{
				Line thisLink = (Line)links[i];
				
				//	System.err.println(" - new Field Intersect - ");
				if (!thisLink.intersects(list))
				// if (!newFieldIntersect(list,thisField))
				{
					//		System.err.println(" = END new Field Intersect = ");
					
					
					ArrayList<Line> newlist = new ArrayList<Line>(list);
					newlist.add((Line)links[i]);
					
					/* this was an integrity check and is redundant.
					if (fieldIntersect(newlist)) {
						throw new RuntimeException("Field Collision : " + drawFields(list,dt) + " / " + thisField );
						
					}
					*/
					
		            Object[] result = searchFields(dt, newlist, links, i + 1, maxArea, depth + 1, sameSize, balance);

        		    maxArea = (int) result[0];
            		balance = (double) result[1];
				}
				
			}
		
		    return new Object[]{maxArea, balance};
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

		boolean sameSize = ag.hasOption("S");


        System.err.println("== Reading portals ==");
        
        HashMap<String,Portal> portals = pf.portalClusterFromString(ag.getArgumentAt(0));
        ArrayList<Link> links = pf.getPurgedLinks(portals.values());
		ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
		// check that maxBl is set
		ArrayList<Line> liList = pf.filterLinks(li,links,maxBl);
		
		System.out.println("Total Links: " + liList.size());

			// sort through colliding fields.
		
		// iterSearchFields(dt,  fiList.toArray());
		searchFields(dt, new ArrayList<Line>() , liList.toArray(),0,0,0,sameSize,0.0);
		
      //  System.out.println("]");

        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
	
}

}

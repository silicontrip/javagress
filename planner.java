
import org.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class planner {

	static double geoCost(ArrayList<PolyPoint>pp)
	{
		double distance = 0;

		for (int i=1; i<pp.size(); i++)
		{
			Point o = pp.get(i-1).asPoint();
			Point d = pp.get(i).asPoint();
			distance += o.getGeoDistance(d);
		}
		return distance;
	}

	static int countUnvisitedPoints(PolyPoint pp, ArrayList<PolyPoint>visited, ArrayList<PolyObject>links)
	{
		int cost = 0;
		for (PolyObject poly : links)
		{
			PolyPoint[] linkPoints = poly.getPoints();
			boolean hasVisited = false;

			if (pp.equals(linkPoints[0]) || pp.equals(linkPoints[1]))
			{
				for (PolyPoint visit : visited) 
				{
					if (visit.equals(linkPoints[0]) || visit.equals(linkPoints[1]))
					{
						hasVisited = true;
						break;
					}
				}
				if (!hasVisited)
					cost ++;
			}
		}
		return cost;
	}

	static int keyCost(ArrayList<PolyPoint>planOrder, ArrayList<PolyObject>links)
	{
		ArrayList<PolyPoint>visited = new ArrayList<PolyPoint>();
		int maxCost = 0;
		for (int i=0; i<planOrder.size(); i++)
		{
			int pointCost = countUnvisitedPoints (planOrder.get(i),visited,links);
			if (pointCost > maxCost)
				maxCost = pointCost;
			visited.add(planOrder.get(i));
		}
		return maxCost;
	}

	static DrawTools linkOrder(DrawTools dt, ArrayList<PolyPoint> combination,ArrayList<PolyObject> polyLines)
	{
		dt.erase();
		ArrayList<PolyPoint>visited = new ArrayList<PolyPoint>();
		visited.add(combination.get(0));
		for (int i = 1; i < combination.size(); i++) {
			PolyPoint thisPoint = combination.get(i);
			for (PolyObject po : polyLines)
			{
				for (PolyPoint visitPoint : visited)
				{
					if ((thisPoint.equals(po.getPoints()[0]) && visitPoint.equals(po.getPoints()[1])) ||
						(thisPoint.equals(po.getPoints()[1]) && visitPoint.equals(po.getPoints()[0])) )
						{
							dt.addLine(thisPoint,visitPoint);
						}
				}
			}
			visited.add(thisPoint);
		}
		return dt;
	}

	static double search (DrawTools dt, ArrayList<PolyPoint> points,  ArrayList<PolyPoint> combination,ArrayList<PolyObject> polyLines, double cost, int keyPercent, HashMap<String,Portal>portalsLoc)
	{
		if (points.size() == 0) {
			int kcost = keyCost (combination,polyLines);
			double dist = geoCost (combination);

			double totalCost = (kcost * keyPercent / 100.0 ) + ( dist * 1 - keyPercent / 100.0);
 
 			if (totalCost < cost)
 			{
				cost = totalCost;
				System.out.print("" + kcost + " " + dist + " : ");
				//for (PolyPoint point : combination) {
				//	System.out.print(portalsLoc.get(point.toString()) + " ; ");
				//}
				//System.out.println("");
				dt = linkOrder(dt,combination,polyLines);
				System.out.println(dt);
			}
		}
		for (int i =0; i<points.size(); i++) {
			ArrayList<PolyPoint>newCombination = new ArrayList<PolyPoint>(combination);
			newCombination.add(points.get(i));
			ArrayList<PolyPoint>pointsCopy = new ArrayList<PolyPoint>(points);
			pointsCopy.remove(i);
			cost = search (dt,pointsCopy,newCombination,polyLines,cost,keyPercent,portalsLoc);
		}
		return cost;
	}

    public static void main(String[] args) throws IOException {

        Arguments ag = new Arguments(args);

		DrawTools dt = new DrawTools();
		if (ag.hasOption("C"))
			dt.setDefaultColour(ag.getOptionForKey("C"));
		else
			dt.setDefaultColour("#a24ac3");

		try{ 

			PortalFactory pf = PortalFactory.getInstance();

			Integer costPercentage = 50;
			if (ag.hasOption("k"))
				costPercentage = Integer.valueOf(ag.getOptionForKey("k"));

			if (ag.getArgumentCount() != 1) {
				System.out.println("Please provide a JSON string as an argument.");
				return;
			}

			DrawTools dtp = new DrawTools(ag.getArgumentAt(0));
			PolyPoint[] uniquePoints = dtp.getUniquePoints();

			// Print out the unique points
			ArrayList<String> stringLoc = new ArrayList<String>();
			for (PolyPoint point : uniquePoints) {
				stringLoc.add(point.toString());
				//System.out.println("LL: " + point);
			}

			HashMap<String,Portal> portalsGuid = pf.getPortals(stringLoc.toArray(new String[stringLoc.size()]));
			HashMap<String,Portal> portalsLoc = new HashMap<String,Portal>();

			for (Portal pn : portalsGuid.values())
			{
				System.out.println(pn);
				String locKey = "" + pn.getLat() + "," + pn.getLng();
				portalsLoc.put(locKey,pn);
			}

			ArrayList<PolyObject> polyLines = dtp.getAsLines();
/*
			for (PolyObject po : polyLines)
			{
				//PolyLine pl = (PolyLine)po;
				System.out.println("poly: " + po);
			}
*/
			ArrayList<PolyPoint> combination = new ArrayList<PolyPoint>(Arrays.asList(uniquePoints));
        	search(dt,combination, new ArrayList<PolyPoint>(),polyLines,1000,costPercentage,portalsLoc);


			} catch (Exception e) {
				System.out.print ("Exception: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}

		}

}

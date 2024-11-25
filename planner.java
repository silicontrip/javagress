
import org.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class planner {

	private int keyPercent;
	private int sbulAvailable;
	private DrawTools dt;
	private int sbulLimit;
	private ArrayList<PolyObject> polyLines;
	
	public planner (int k, int s, DrawTools d,ArrayList<PolyObject>p)
	{
		keyPercent = k;
		sbulAvailable = s;
		dt = d;
		polyLines = p;
		sbulLimit = 8 + sbulAvailable * 8;
	}

	public static boolean containsPoint(Polygon polygon, PolyPoint point) 
	{

		for (PolyPoint vertex : polygon.latLngs) {
			if (point.lat == vertex.lat && point.lng == vertex.lng) {
				return false; // Point is exactly on a vertex, return true (inside)
			}
		}
	

		int crossCount = 0;
		ArrayList<PolyPoint> points = polygon.getLatLngs();
		for (int i = 1; i < points.size(); ++i) {
			PolyPoint p1 = points.get(i - 1);
			PolyPoint p2 = points.get(i);

			if ((p1.lat > point.lat) != (p2.lat > point.lat) && point.lng < (p2.lng - p1.lng) * (point.lat - p1.lat) / (p2.lat - p1.lat) + p1.lng) 
			{
				crossCount++;
			}
		}
	 
		// Check the segment from the last point to the first
		PolyPoint p1 = points.get(points.size() - 1);
		PolyPoint p2 = points.get(0);

		if ((p1.lat > point.lat) != (p2.lat > point.lat) && point.lng < (p2.lng - p1.lng) * (point.lat - p1.lat) / (p2.lat - p1.lat) + p1.lng) 
		{
			crossCount++;
		}
	 
		return (crossCount & 1) == 1;// If odd, point is inside the polygon
		
	}
	public static boolean containsPoint(ArrayList<Polygon> polys, PolyPoint point) 
	{
		for (Polygon polygon: polys)
		{
			if (containsPoint(polygon,point))
				return true;
		}
		return false;
	}

	public static boolean containsPoint(Polygon polygon, ArrayList<PolyPoint> points) 
	{
		for (PolyPoint point: points)
		{
			if (containsPoint(polygon,point))
				return true;
		}
		return false;
	}

	static ArrayList<Polygon> completeField(ArrayList<Polyline> order, Polyline pl) {
		ArrayList<Polygon> completedFields = new ArrayList<>();
	
		// Iterate through all possible pairs of polylines from the order list and the new polyline
		for (int i = 0; i < order.size(); i++) {
			Polyline p1 = order.get(i);

			for (int j = i + 1; j < order.size(); j++) {
				Polyline p2 = order.get(j);
	
				PolyPoint p1o = p1.latLngs.get(0);
				PolyPoint p1d = p1.latLngs.get(1);
				PolyPoint p2o = p2.latLngs.get(0);
				PolyPoint p2d = p2.latLngs.get(1);				
				PolyPoint plo = pl.latLngs.get(0);
				PolyPoint pld = pl.latLngs.get(1);

				if (p1o.equals(p2o) &&
					((plo.equals(p1d) && pld.equals(p2d)) ||
					(plo.equals(p2d) && pld.equals(p1d)))
				) {
					Polygon potentialPolygon = new Polygon(p1o,plo,pld);
					completedFields.add(potentialPolygon);
				} else if (p1d.equals(p2o) &&
					((plo.equals(p1o) && pld.equals(p2d)) ||
					(plo.equals(p2d) && pld.equals(p1o)))
				) {
					Polygon potentialPolygon = new Polygon(p1d,plo,pld);
					completedFields.add(potentialPolygon);
				} else if (p1o.equals(p2d) &&
					((plo.equals(p1d) && pld.equals(p2o)) ||
					(plo.equals(p2o) && pld.equals(p1d)))
				) {
					Polygon potentialPolygon = new Polygon(p1o,plo,pld);
					completedFields.add(potentialPolygon);
				} else if (p1d.equals(p2d) &&
					((plo.equals(p1o) && pld.equals(p2o)) ||
					(plo.equals(p2o) && pld.equals(p1o)))
				) {
					Polygon potentialPolygon = new Polygon(p1d,plo,pld);
					completedFields.add(potentialPolygon);
				} 

			}
		}
	
		return completedFields;
	}
	
	static boolean checkPlan (DrawTools dts)
	{
		ArrayList<Polyline> order = new ArrayList<Polyline>();
		ArrayList<Polygon> completed = new ArrayList<Polygon>();

		for (int i=0; i < dts.size(); i++)
		{
			PolyObject po = dts.get(i);
			if (po.EnumType() == PolyType.POLYLINE)
			{
				Polyline pl = (Polyline)po;
				// check if link is less than 2000m
				if (containsPoint(completed,pl.latLngs.get(0)))
					return false;

				ArrayList<Polygon> completeFields = completeField(order,pl);
				// can't check for 2 or more fields on one side.
				for (Polygon pg: completeFields)
					completed.add(pg);

				order.add(pl);
			}
		}
		return true;
	}
	
	static boolean checkSame (DrawTools dts)
	{
		ArrayList<Polyline> order = new ArrayList<Polyline>();

		for (int i=0; i < dts.size(); i++)
		{
			PolyObject po = dts.get(i);
			if (po.EnumType() == PolyType.POLYLINE)
			{
				Polyline pl = (Polyline)po;

				ArrayList<Polygon> completeFields = completeField(order,pl);
				//  check for 2 or more fields on one side.
				if (completeFields.size()>2) {

					/* 
					System.out.println("Fields created: " + completeFields.size());
					boolean first = true;

					System.out.println("[");
					for (Polygon fi: completeFields)
					{
						if (!first)
						
							System.out.println(",");
						first = false;
						System.out.println(fi.getJSONObject());
					}
					System.out.println("]");
					*/

					return false;
				}
				if (completeFields.size() == 2)
				{
					if (containsPoint(completeFields.get(0),completeFields.get(1).getLatLngs()))
					{
						/* 
						System.out.println("[");
						System.out.println(completeFields.get(0).getJSONObject());
						System.out.println(",");
						System.out.println(completeFields.get(1).getJSONObject());
						System.out.println("]");
						*/
						return false;
					}
					if (containsPoint(completeFields.get(1),completeFields.get(0).getLatLngs()))
					{
						 /*
						System.out.println("[");
						System.out.println(completeFields.get(0).getJSONObject());
						System.out.println(",");
						System.out.println(completeFields.get(1).getJSONObject());
						System.out.println("]");
						*/
						return false;
					}
				}
				order.add(pl);
			}
		}
		return true;
	}

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

	int countVisitedPoints(PolyPoint pp, ArrayList<PolyPoint>visited)
	{
		int cost = 0;
		for (PolyObject poly : polyLines)
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
				if (hasVisited)
					cost ++;
			}
		}
		return cost;
	}

	int countUnvisitedPoints(PolyPoint pp, ArrayList<PolyPoint>visited)
	{
		int cost = 0;
		for (PolyObject poly : polyLines)
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

	int keyCost(ArrayList<PolyPoint>planOrder)
	{
		ArrayList<PolyPoint>visited = new ArrayList<PolyPoint>();
		int maxCost = 0;
		for (int i=0; i<planOrder.size(); i++)
		{
			int linkLimit = countVisitedPoints(planOrder.get(i), visited);
			if (linkLimit>sbulLimit)
				return 1000;
			int pointCost = countUnvisitedPoints (planOrder.get(i),visited);
			if (pointCost > maxCost)
				maxCost = pointCost;
			visited.add(planOrder.get(i));
		}
		return maxCost;
	}

	double getTotalCost (ArrayList<PolyPoint>combination)
	{
		DrawTools dts = new DrawTools();
		int kcost = keyCost (combination);
		double dist = geoCost (combination);

		if (kcost < 1000)
		{
			dts = linkOrder(dts,combination);
			if (!checkPlan(dts))
				kcost = 10000;
		}
		return (kcost * keyPercent / 100.0 ) + ( dist * 1 - keyPercent / 100.0);
 
	}

	static int factorial(int n) {
		int result = 1;
		for (int i = 2; i <= n; i++) {
			result *= i;
		}
		return result;
	}

    // Method to generate the next permutation based on the counter
    static boolean nextPermutation(List<PolyPoint> visited, int step) {
        int n = visited.size();
        
		if (n==1)
			return true;
        // If step exceeds the total number of permutations, return false (end of permutations)
        if (step >= factorial(n)) {
            return false;
        }

        int[] p = new int[n];

        for (int i=0; i < n; i++)
            p[i] = 0;

        int j = 1;
        int i = 1;
        if (step > 0)
            while (i < n)
            {
                if (p[i] < i)
                {
                    j = i % 2 * p[i];
                    step --;
                    if (step == 0)
                        break;
                    p[i]++;
                    i = 1;
                } else {
                    p[i] = 0;
                    i++;
                }
            }
        // Perform the swap with only one element per iteration based on swapIndex
        //System.err.println("swap: " + j + " <-> " + i);
        Collections.swap(visited, j, i);

        return true;
    }

	DrawTools linkOrderSame(DrawTools dts, ArrayList<PolyPoint> combination)
	{
		dts.erase();
		ArrayList<PolyPoint>visited = new ArrayList<PolyPoint>();
		visited.add(combination.get(0));
		dts.addMarker(combination.get(0));

		for (int i = 1; i < combination.size(); i++) 
		{
			PolyPoint thisPoint = combination.get(i);
			ArrayList<PolyPoint> outLinks = new ArrayList<PolyPoint>();

			for (PolyPoint visitPoint : visited)
			{
				for (PolyObject po : polyLines)
				{
					if ((thisPoint.equals(po.getPoints()[0]) && visitPoint.equals(po.getPoints()[1])) ||
						(thisPoint.equals(po.getPoints()[1]) && visitPoint.equals(po.getPoints()[0])) )
					{
						outLinks.add(visitPoint);
					}
				}
			}
			if (outLinks.size() > 0)
			{
				boolean validPlan = false;
				int counter = 0;
				DrawTools ndt = new DrawTools(dts);

				int countLimit = factorial(outLinks.size());

				while (!validPlan && counter < countLimit) {
					ndt = new DrawTools(dts);
					nextPermutation(outLinks, counter++);
					for (PolyPoint pp: outLinks)
					{
						ndt.addLine(thisPoint, pp);
					}

					validPlan = checkSame(ndt);
				}
				dts = ndt;
			} else {
				dts.addMarker(thisPoint);
			}
			visited.add(thisPoint);
		}
		return dts;
	}

	DrawTools linkOrder(DrawTools dts, ArrayList<PolyPoint> combination)
	{
		dts.erase();
		ArrayList<PolyPoint>visited = new ArrayList<PolyPoint>();
		visited.add(combination.get(0));
		dts.addMarker(combination.get(0));

		for (int i = 1; i < combination.size(); i++) {
			boolean linked = false;
			PolyPoint thisPoint = combination.get(i);

			for (PolyPoint visitPoint : visited)
			{
				for (PolyObject po : polyLines)
				{
					if ((thisPoint.equals(po.getPoints()[0]) && visitPoint.equals(po.getPoints()[1])) ||
						(thisPoint.equals(po.getPoints()[1]) && visitPoint.equals(po.getPoints()[0])) )
					{
						dts.addLine(thisPoint,visitPoint);
						linked = true;
					}
				}

			}
			
			if (!linked)
			{
				dts.addMarker(thisPoint);
			}
			visited.add(thisPoint);
		}
		return dts;
	}

	static ArrayList<PolyPoint> generateRandom (ArrayList<PolyPoint> points)
	{
		ArrayList<PolyPoint>newCombination = new ArrayList<>(points);
		Collections.shuffle(newCombination);
		return newCombination;
	}

	static ArrayList<PolyPoint> perturbSolution(ArrayList<PolyPoint> combination)
	{
		int n = combination.size();

		int i = new Random().nextInt(n);
		int j = new Random().nextInt(n-1);
		if (j >= i) 
			j++;

		ArrayList<PolyPoint> newCombination = new ArrayList<>(combination);
		Collections.swap(newCombination,i,j);
		return newCombination;
	}

	void simulatedAnnealing (ArrayList<PolyPoint> combination, double initialTemperature, double coolingRate, int iterations)
	{
		double bestCost = 1000;

		int n = combination.size();
		ArrayList<PolyPoint> bestCombination = new ArrayList<>();

		for (double temperature = initialTemperature; temperature > 1e-6; temperature *= coolingRate)
		{
			ArrayList<PolyPoint> currentCombination = generateRandom(combination);

			double currentCost = getTotalCost(currentCombination);

			for (int iter =0; iter < iterations; iter++)
			{
				
				ArrayList<PolyPoint> newCombination = perturbSolution(currentCombination);

				double newCost = getTotalCost(newCombination);
				if (newCost < currentCost || Math.exp((currentCost - newCost) / temperature) > Math.random())
				{
					currentCombination = newCombination;
					currentCost = newCost;

					if (newCost < bestCost)
					{
						bestCost = newCost;
						bestCombination = new ArrayList<>(newCombination);

						int kcost = keyCost(bestCombination);
						double dist = geoCost(bestCombination);
						System.out.print("" + kcost + " " + dist + " : ");
						//dt = linkOrder(dt,bestCombination);
						//System.out.println(dt);
						System.out.println("");
						if (kcost < 1000)
						{
							dt = linkOrderSame(dt,bestCombination);
							System.out.println(dt);
							System.out.println("");
						}
					}
				}
			}
		}
	}
	double search (ArrayList<PolyPoint> points,  ArrayList<PolyPoint> combination, double cost)
	{
		if (points.size() == 0) {
			double totalCost = getTotalCost (combination);
 
 			if (totalCost < cost)
 			{
				cost = totalCost;
				int kcost = keyCost(combination);
				double dist = geoCost(combination);
				System.out.print("" + kcost + " " + dist + " : ");
				//for (PolyPoint point : combination) {
				//	System.out.print(portalsLoc.get(point.toString()) + " ; ");
				//}
				//System.out.println("");
				dt = linkOrder(dt,combination);
				System.out.println(dt);
				System.out.println("");

			}
		}
		for (int i =0; i<points.size(); i++) {
			ArrayList<PolyPoint>newCombination = new ArrayList<PolyPoint>(combination);
			newCombination.add(points.get(i));
			ArrayList<PolyPoint>pointsCopy = new ArrayList<PolyPoint>(points);
			pointsCopy.remove(i);
			cost = search (pointsCopy,newCombination,cost);
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

			//PortalFactory pf = PortalFactory.getInstance();

			Integer costPercentage = 50;
			if (ag.hasOption("k"))
				costPercentage = Integer.valueOf(ag.getOptionForKey("k"));

			// don't blame me if you specify more than 2 for a single agent or more than 4
			Integer sbulCount = 0;
			if (ag.hasOption("s"))
				sbulCount = Integer.valueOf(ag.getOptionForKey("s"));
			

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

			//HashMap<String,Portal> portalsGuid = pf.getPortals(stringLoc.toArray(new String[stringLoc.size()]));
		//	HashMap<String,Portal> portalsLoc = new HashMap<String,Portal>();

			//for (Portal pn : portalsGuid.values())
			//{
			//	System.out.println(pn);
		//		String locKey = "" + pn.lat + "," + pn.lng;
		//		portalsLoc.put(locKey,pn);
		//	}

			ArrayList<PolyObject> polyLines = dtp.getAsLines();
/*
			for (PolyObject po : polyLines)
			{
				//PolyLine pl = (PolyLine)po;
				System.out.println("poly: " + po);
			}
*/
			ArrayList<PolyPoint> combination = new ArrayList<PolyPoint>(Arrays.asList(uniquePoints));
        	//search(dt,combination, new ArrayList<PolyPoint>(),polyLines,1000,costPercentage);

			planner p = new planner(costPercentage, sbulCount, dt, polyLines);

			p.simulatedAnnealing(combination, 1000.0, 0.95, 10000);

		} catch (Exception e) {
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

}

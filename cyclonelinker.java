import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;
import javax.xml.parsers.*;
import java.io.*;


public class cyclonelinker {

	public static <K, V> void printMap(Map<K, V> map) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
		} 
	} 

	private static void initSearch(Object[] fields, DrawTools dt) {
		int max = 2;
		// Scan through all fields
		for (int i = 0; i < fields.length; i++) {
			Field thisField = (Field) fields[i];
			ArrayList<Line> edges = thisField.getAllLines();
			// Try each edge of the field
			for (Line edge : edges) {
				ArrayList<Field> cadFields = new ArrayList<Field>();
				// Find fields with matching edge
				for (int j = i + 1; j < fields.length; j++) {
					Field testField = (Field) fields[j];
					if (testField.hasLine(edge) && !thisField.intersects(testField) && !thisField.equals(testField)) {
						cadFields.add(testField);
					}
				}

				// Search the current fields with the 2 non-matching edges
				for (Field cfi : cadFields) {
					ArrayList<Line> medges = cfi.getAllLines();
					for (Line medge : medges) {
						if (!thisField.hasLine(medge)) {
							// Find the third point of thisField that does not share the medge
							Point thirdPoint = thisField.getOtherPoint(medge);

							if (thirdPoint != null) {
								if (cfi.inside(thirdPoint)) {
									ArrayList<Field> fieldsList = new ArrayList<Field>();
									fieldsList.add(thisField);
									fieldsList.add(cfi);
									max = cycloneIterate(i + 1, medge, fields, fieldsList, max, dt, thirdPoint);
								}
							}
						}
					}
				}
			}
		}
	}


	private static String drawFields(List<Field> fa,DrawTools dt)
        {

                dt.erase();

                for (Field fi: fa)
                        dt.addField(fi);

                return dt.out();

        }
	
	private static int countLinks (Point p, ArrayList<Field> flist)
	{

		int count = 0;
		for (Field fi: flist)
			for (Line li: fi.getAllLines())
				if (li.hasPoint(p))
					count++;
				
		return count;
	}

	

	private static int cycloneIterate(int start, Line medge, Object[] fields, ArrayList<Field> fieldsList, int max, DrawTools dt, Point thirdPoint) {
		// If we have a better plan, print it.
		if (fieldsList.size() > max) {
			max = fieldsList.size();
			// Draw tools
			System.out.println("" + max + " : " + drawFields(fieldsList, dt));
		}

		ArrayList<Field> cadFields = new ArrayList<Field>();
		for (int j = start; j < fields.length; j++) {
			Field testField = (Field) fields[j];
			if (testField.hasLine(medge)) {
				boolean intersect = false;
				for (Field thisField : fieldsList) {
					if (thisField.intersects(testField) || thisField.equals(testField)) {
						intersect = true;
						break;
					}
				}
				if (!intersect) {
					// Ensure the new field covers the third point of the thisField
					if (thirdPoint != null && !testField.inside(thirdPoint)) {
						continue; // Skip this field if it does not cover the necessary point
					}
					cadFields.add(testField);
				}
			}
		}

		for (Field fl : cadFields) {
			int[] pointCount = new int[3];
			for (int i = 0; i < 3; i++) {
				pointCount[i] = countLinks(fl.getPoint(i), fieldsList);
			}
			// Pick points with fewest links.
			int maxCount = pointCount[0];
			int p1 = 1;
			int p2 = 2;
			int p3 = 0;
			if (pointCount[1] > maxCount) { p1 = 0; p2 = 2; p3 = 1;}
			if (pointCount[2] > maxCount) { p1 = 0; p2 = 1; p3 = 2;}
			Line selEdge = new Line(fl.getPoint(p1), fl.getPoint(p2));
			thirdPoint = fl.getPoint(p3);
			ArrayList<Field> newList = new ArrayList<Field>(fieldsList);
			newList.add(fl);

			max = cycloneIterate(start, selEdge, fields, newList, max, dt, thirdPoint);
		}
		return max;
	}


	public static void main(String[] args) {
		
		int calc=0;
		Double threshold;
		Double percentile = null;
		ArrayList<Point> target=null;
		
		RunTimer rt;
		Arguments ag = new Arguments(args);

		//System.out.println ("Arguments: " + ag );

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

		// mu calculation
		if (ag.hasOption("M"))
			calc=1;
		
		
		if (ag.hasOption("t"))
			threshold = new Double(ag.getOptionForKey("t"));
		else
			threshold = new Double(0.3);

		if (ag.hasOption("p"))
			percentile = new Double(ag.getOptionForKey("p"));


		if (ag.hasOption("h"))
		{
			System.out.println("Options");
			System.out.println(" -E <number>       Limit number of Enlightened Blockers");
			System.out.println(" -R <number>       Limit number of Resistance Blockers");
			System.out.println(" -C <#colour>      Set Drawtools output colour");
			System.out.println(" -L                Set Drawtools to output as polylines");
			System.out.println(" -O                Output as Intel Link");
			System.out.println(" -t <number>       Threshold for similar fields (larger less similar)");
			System.out.println(" -p <percentile>   Use longest percentile links");
			System.out.println(" -T <lat,lng,...>  Use only fields  covering target points");
		}

			
		
		try {
			PortalFactory pf = PortalFactory.getInstance();

			if (ag.hasOption("T"))
				target = pf.getPointsFromString(ag.getOptionForKey("T"));

			
			rt = new RunTimer();
			System.err.println("== Reading links and portals ==");
			rt.start();
			
			
			// System.err.println("== " + args.length + " ==");
			
			// HashMap<String,Link> allLinks = pf.getLinks();
			
			List<Portal> allPortals = new ArrayList<Portal>();
			
			ArrayList<String> areaOut;
			ArrayList<Link> links;
			
		// ag.getArgumentAt
			ArrayList<Field> allfields;
			if (ag.getArguments().size() == 1) {
				
			// get all 3 points from one cluster 
				
				HashMap<String,Portal> portals = new HashMap<String,Portal>();
				
				portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				
				System.err.println("== " + portals.size() + " portals read " + rt.split()+ " ==");
				if (ag.hasOption("P")) {
					portals = pf.reducePortals(portals,new Double(ag.getOptionForKey("P")));
					System.err.println("== " + portals.size() + " portals reduced " + rt.split()+ " ==");
				}

				System.err.println("== getting links ==");
				
				links = pf.getPurgedLinks(portals.values());
				
				System.err.println("== "+links.size()+" links read " + rt.split()+ " ==");
				
				
				System.err.println("== generating potential links ==");

				ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
				System.err.println("all links: " + li.size());
				ArrayList<Line> l1 = pf.filterLinks(li,links,maxBl);
				ArrayList<Line> l2;
				if (percentile != null)
					l2 = pf.percentileLinks(l1,percentile);
				else 
					l2 = l1;
				
				System.err.println("purged links: " + l2.size());

				System.err.println("==  links generated " + rt.split()+ " ==");
				System.err.println("== Generating fields ==");
				
				ArrayList<Field> af = pf.makeFieldsFromSingleLinks(l2);
				allfields = pf.filterFields(af,links,maxBl);
				System.err.println("fields: " + allfields.size());

				// allfields = singleCluster(portals);
				
			} else if (ag.getArguments().size() == 2) { 
				// one point from one cluster
				// and the other two points from the other cluster
				
				HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals2 = new HashMap<String,Portal>();

				
				portals1 = pf.portalClusterFromString(ag.getArgumentAt(0));
				portals2 = pf.portalClusterFromString(ag.getArgumentAt(1));
				
				
				allPortals = new ArrayList<Portal>();
								
				allPortals.addAll(portals1.values());
				allPortals.addAll(portals2.values());
				
				
				System.err.println("== " + allPortals.size() + " portals read " + rt.split()+ " ==");
				System.err.println("== Reading links ==");
				
				
				
				links = pf.getPurgedLinks(new ArrayList<Portal>(allPortals));
				
				
				System.err.println("==  links read " +rt.split()  + " ==");
				System.err.println("== Generating fields ==");
				
				ArrayList<Line> li1 = pf.makeLinksFromSingleCluster(portals1.values());
				ArrayList<Line> lf1 = pf.filterLinks(li1,links,maxBl);
				System.err.println("== cluster 1 links:  " + lf1.size() + " ==");

				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals1.values(),portals2.values());
				ArrayList<Line> lf2 = pf.filterLinks(li2,links,maxBl);
				System.err.println("== cluster 2 links:  " + lf2.size() + " ==");

				ArrayList<Field> af = pf.makeFieldsFromDoubleLinks(lf2,lf1);
				allfields = pf.filterFields(af,links,maxBl);
				System.err.println("== Fields:  " + allfields.size() + " ==");

				// portals1 and and portals2 is crucial ordering.
				// two portals from portals1 and 1 from portals2
				//allfields = doubleCluster(portals1,portals2);
				

			} else if (ag.getArguments().size() == 3) { 
				
				// one point from each cluster
				
				HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals2 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals3 = new HashMap<String,Portal>();

				
				portals1 = pf.portalClusterFromString(ag.getArgumentAt(0));
				portals2 = pf.portalClusterFromString(ag.getArgumentAt(1));
				portals3 = pf.portalClusterFromString(ag.getArgumentAt(2));

				 allPortals = new ArrayList<Portal>();
				
				allPortals.addAll(portals1.values());
				allPortals.addAll(portals2.values());
				allPortals.addAll(portals3.values());

				System.err.println("== " + allPortals.size() + " portals read " + rt.split()+ " ==");
				System.err.println("== get links ==");
				
				
				links = pf.getPurgedLinks(new ArrayList<Portal>(allPortals));
				// create link blockers 1-2, 2-3 and 3-1 

				// HashMap<String,teamCount> bpl = new HashMap<String,teamCount>();
				
				
				System.err.println("==  links read " + rt.split()+ " ==");
				System.err.println("== Generating possible fields ==");
				
				ArrayList<Line> li1 = pf.makeLinksFromDoubleCluster(portals1.values(),portals2.values());
				
				System.err.println("== "+li1.size()+" links generated " + rt.split()+ " ==");

				
				ArrayList<Line> lf1 = pf.filterLinks(li1,links,maxBl);
				System.err.println("== "+lf1.size()+" links filtered " + rt.split()+ " ==");
				
				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals2.values(),portals3.values());
				System.err.println("== "+li2.size()+" links generated " + rt.split()+ " ==");

				ArrayList<Line> lf2 = pf.filterLinks(li2,links,maxBl);
				System.err.println("== "+lf2.size()+" links filtered " + rt.split()+ " ==");

				
				ArrayList<Line> li3 = pf.makeLinksFromDoubleCluster(portals3.values(),portals1.values());
				System.err.println("== "+li3.size()+" links generated " + rt.split()+ " ==");

				ArrayList<Line> lf3 = pf.filterLinks(li3,links,maxBl);
				System.err.println("== "+lf3.size()+" links filtered " + rt.split()+ " ==");

				ArrayList<Field> af = pf.makeFieldsFromTripleLinks(lf1,lf2,lf3);
				allfields = pf.filterFields(af,links,maxBl);

			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
			System.err.println("== fields generated " + rt.split() + " ==");
			System.err.println("== purge fields ==");
			
			// start searching for fields.

			//Map<Double,Field> blockField = new TreeMap<Double,Field>(Collections.reverseOrder());
			Map<Double,Field> blockField = new TreeMap<Double,Field>();
			for (Field fi: allfields) {
				if (target==null || fi.inside(target))
					blockField.put(fi.getGeoPerimeter(),fi);
			}
			System.err.println("==  fields filtered " + rt.split() + " ==");
			System.err.println("== show matches ==");

		// 	Map<Double,Field> simField = new TreeMap<Double,Field>(Collections.reverseOrder());

			Object[] bf = blockField.values().toArray();

			initSearch(bf,dt);

			System.err.println("==  plans searched " + rt.split() + " ==");
			System.err.println("== show all plans ==");


			System.err.println("== Finished. " + rt.split() + " elapsed time. " + rt.stop() + " total time.");
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

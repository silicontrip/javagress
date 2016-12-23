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


public class layerlinker {

	public static <K, V> void printMap(Map<K, V> map) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
		} 
	} 

	private static ArrayList<Field> matchingFields(ArrayList<Field> fa, Field fi, Double threshold)
	{
		ArrayList<Field> ff = new ArrayList<Field>();
		for (Field tf: fa)
			if(tf.difference(fi) <= threshold)
				ff.add(tf);

		return ff;
	}	
        private static Double searchFields (DrawTools dt, ArrayList<Field> list, Object[] fields, int start, Double maxArea) throws javax.xml.parsers.ParserConfigurationException, java.io.IOException
        {
                        if (list.size() > 0) {

				// how to pick which field sizing algorithm
                        //      Double thisArea = sizeFields(list);
                                // we want to maximise number of fields
                               // Double thisArea = new Double(list.size());
				Double thisArea = 0.0;
				for (Field fi: list)
					thisArea += fi.getEstMu();
				

                                if (thisArea > maxArea) {
                                        System.out.print(thisArea + "(" + list.size() + ") : ");
					dt.erase();
					for (Field fi: list)
						dt.addField(fi);
					System.out.println(dt);
						
                                        System.out.println("");
                                        maxArea = thisArea;
                                }
                        }

                        for (int i =start; i<fields.length; i++)
                        {
                                Field thisField = (Field)fields[i];

                                if (!thisField.intersects(list))
                                {

                                        ArrayList<Field> newlist = new ArrayList<Field>(list);
                                        newlist.add((Field)fields[i]);
                                        maxArea = searchFields(dt,newlist,fields,i+1,maxArea);
                                }

                        }

                return maxArea;
        }
	

	// would like to change this to use a field search
	// return all similar fields.  Search all similar fields for maximum layers or mu.

	private static int findField(Object[] fields, int start, Field current,ArrayList<Field> exist, Double threshold)
	{
		int best=-1;
		Double closest = 9999.0;
		for (int n =start; n < fields.length; n++)
		{
			if (!((Field)fields[n]).intersects(exist))
			{
				Double diff = current.difference((Field)fields[n]);
			// want to make a field selection strategy, for different fielding plans.
			// need to make configurable threshold
				if (diff < threshold  && diff < closest)
					{
						closest = diff;
						best = n;
					}

			}
		}
		return best;
	}
	
	public static void main(String[] args) {
		
		int calc=0;
		Double threshold;
		ArrayList<Point> target=null;
		
		RunTimer rt;
		Arguments ag = new Arguments(args);

		//System.out.println ("Arguments: " + ag );

		teamCount maxBl = new teamCount(ag.getOptionForKey("E"),ag.getOptionForKey("R"));
        
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
				ArrayList<Line> l2 = pf.filterLinks(li,links,maxBl);
				
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
/*
				dt.erase();
				dt.setDefaultColour("#f0f000");
				for (Line l: lf1)
					dt.addLine(l);
*/
				
				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals2.values(),portals3.values());
				System.err.println("== "+li2.size()+" links generated " + rt.split()+ " ==");

				ArrayList<Line> lf2 = pf.filterLinks(li2,links,maxBl);
				System.err.println("== "+lf2.size()+" links filtered " + rt.split()+ " ==");

/*
				dt.setDefaultColour("#f000f0");
				for (Line l: lf2)
					dt.addLine(l);
*/
				
				ArrayList<Line> li3 = pf.makeLinksFromDoubleCluster(portals3.values(),portals1.values());
				System.err.println("== "+li3.size()+" links generated " + rt.split()+ " ==");

				ArrayList<Line> lf3 = pf.filterLinks(li3,links,maxBl);
				System.err.println("== "+lf3.size()+" links filtered " + rt.split()+ " ==");

/*
				dt.setDefaultColour("#00f0f0");
				for (Line l: lf3)
					dt.addLine(l);

				System.out.println(dt.out());
*/
				ArrayList<Field> af = pf.makeFieldsFromTripleLinks(lf1,lf2,lf3);
				allfields = pf.filterFields(af,links,maxBl);

			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
			System.err.println("==  fields generated " + rt.split() + " ==");
			System.err.println("== purge fields ==");
			
			// start searching for fields.

			Map<Double,Field> blockField = new TreeMap<Double,Field>(Collections.reverseOrder());
			for (Field fi: allfields) {
				if (target==null || fi.inside(target))
					if (calc==0)
						blockField.put(fi.getGeoArea(),fi);
					else
						blockField.put(fi.getEstMu(),fi);
			}
			System.err.println("==  fields filtered " + rt.split() + " ==");
			System.err.println("== show matches ==");

		// 	Map<Double,Field> simField = new TreeMap<Double,Field>(Collections.reverseOrder());

			Object[] bf = blockField.values().toArray();

			Map<Double,String> plan = new TreeMap<Double,String>();

			Double bestbest = 0.0;
				
			for (int i =0; i< bf.length;i++ )  {
				Field tfi = (Field)bf[i];
				Double at = 0.0;
				if (calc==0)
					at  += tfi.getGeoArea();
				else
					at  += tfi.getEstMu();

				ArrayList<Field> fc = new ArrayList<Field>();
				dt.erase();
				fc.add(tfi);
				dt.addField(tfi);
				int best = findField(bf,i+1,tfi,fc,threshold); // make threshold configurable
				while (best != -1) {

					tfi = (Field)bf[best];

					if (calc==0)
						at  += tfi.getGeoArea();
					else
						at  += tfi.getEstMu();
					dt.addField(tfi);
					fc.add(tfi);
					best = findField(bf,best+1,tfi,fc,threshold); // make threshold configurable
				}
				// calc area, layers 
				// print
				plan.put(at," ("+ fc.size()+") / " + dt.out());
				if (at>bestbest) {
					bestbest = at;
					System.out.println("" + at + " ("+fc.size()+ ") / " + dt.out());
				}
			}
			System.err.println("==  plans searched " + rt.split() + " ==");
			System.err.println("== show all plans ==");


			for (Map.Entry<Double, String> entry : plan.entrySet()) 
			{
				System.out.println(""  + entry.getKey() +  entry.getValue());
				System.out.println("");
			}


			System.err.println("== Finished. " + rt.split() + " elapsed time. " + rt.stop() + " total time.");
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

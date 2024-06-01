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
		int maxLayers = 0;
		Double threshold;
		Double percentile = null;
		Double fpercentile = null;
		Double notOver = 8000000000.0; // populaton of earth
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
		
		if (ag.hasOption("m"))
			notOver = new Double (ag.getOptionForKey("m"));

		
		if (ag.hasOption("t"))
			threshold = new Double(ag.getOptionForKey("t"));
		else
			threshold = new Double(0.3);

		if (ag.hasOption("p"))
			percentile = new Double(ag.getOptionForKey("p"));
		if (ag.hasOption("f"))
			fpercentile = new Double(ag.getOptionForKey("f"));

		if (ag.hasOption("l"))
			maxLayers = Integer.valueOf(ag.getOptionForKey("l"));

		if (ag.hasOption("h"))
		{
			System.out.println("Options");
			System.out.println(" -E <number>       Limit number of Enlightened Blockers");
			System.out.println(" -R <number>       Limit number of Resistance Blockers");
			System.out.println(" -C <#colour>      Set Drawtools output colour");
			System.out.println(" -L                Set Drawtools to output as polylines");
			System.out.println(" -O                Output as Intel Link");
			System.out.println(" -M                Use MU calculation");
			System.out.println(" -t <number>       Threshold for similar fields (larger less similar)");
			System.out.println(" -l <number>       Maximum number of layers in plan");
			System.out.println(" -p <percentile>   Use longest percentile links");
			System.out.println(" -f <percentile>   Use largest percentile fields");
			System.out.println(" -T <lat,lng,...>  Use only fields covering target points");
		}

		System.err.println("== MaxLayers: " + maxLayers + " ==");
	
		
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
				li = pf.filterLinks(li,links,maxBl);
				if (percentile != null)
					li = pf.percentileLinks(li,percentile);
				
				System.err.println("purged links: " + li.size());

				System.err.println("==  links generated " + rt.split()+ " ==");
				System.err.println("== Generating fields ==");
				
				ArrayList<Field> af = pf.makeFieldsFromSingleLinks(li);
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
				li1 = pf.filterLinks(li1,links,maxBl);
				System.err.println("== cluster 1 links:  " + li1.size() + " ==");

				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals1.values(),portals2.values());
				li2 = pf.filterLinks(li2,links,maxBl);
				System.err.println("== cluster 2 links:  " + li2.size() + " ==");

				ArrayList<Field> af = pf.makeFieldsFromDoubleLinks(li2,li1);
				allfields = pf.filterFields(af,links,maxBl);
				System.err.println("== Fields:  " + allfields.size() + " ==");


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

				
				li1 = pf.filterLinks(li1,links,maxBl);
				System.err.println("== "+li1.size()+" links filtered " + rt.split()+ " ==");
				
				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals2.values(),portals3.values());
				System.err.println("== "+li2.size()+" links generated " + rt.split()+ " ==");

				li2 = pf.filterLinks(li2,links,maxBl);
				System.err.println("== "+li2.size()+" links filtered " + rt.split()+ " ==");

				ArrayList<Line> li3 = pf.makeLinksFromDoubleCluster(portals3.values(),portals1.values());
				System.err.println("== "+li3.size()+" links generated " + rt.split()+ " ==");

				li3 = pf.filterLinks(li3,links,maxBl);
				System.err.println("== "+li3.size()+" links filtered " + rt.split()+ " ==");

				ArrayList<Field> af = pf.makeFieldsFromTripleLinks(li1,li2,li3);
				allfields = pf.filterFields(af,links,maxBl);

			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
			System.err.println("==  fields generated " + rt.split() + " ==");
			System.err.println("== purge fields ==");
			
			// start searching for fields.
			if (target!=null)
				allfields = pf.fieldsOverTarget(allfields,target);

			if (fpercentile != null)
				allfields = pf.percentileFields(allfields,fpercentile,false);

			System.err.println("==  fields filtered " + rt.split() + " ==");
			System.err.println("== sorting fields ==");

		Map<Double,Field> blockField = new TreeMap<Double,Field>(Collections.reverseOrder());
			for (Field fi: allfields) {
				if (calc==0)
					blockField.put(fi.getGeoArea(),fi);
				else
					blockField.put(fi.getEstMu(),fi);
			}
			System.err.println("==  fields sortered " + rt.split() + " ==");
			System.err.println("== show matches ==");

		// 	Map<Double,Field> simField = new TreeMap<Double,Field>(Collections.reverseOrder());

			Object[] bf = blockField.values().toArray();

			Map<Double,String> plan = new TreeMap<Double,String>();

			Double bestbest = 0.0;
				
			for (int i =0; i< bf.length;i++ ) {
				Field tfi = (Field)bf[i];
				Double at = 0.0;
				if (calc==0)
					at += tfi.getGeoArea();
				else
					at += tfi.getEstMu();

				ArrayList<Field> fc = new ArrayList<Field>();
				dt.erase();
				fc.add(tfi);
				dt.addField(tfi);
				int best = findField(bf,i+1,tfi,fc,threshold); // make threshold configurable
				while (best != -1 && (maxLayers==0 || fc.size() < maxLayers)) {

					tfi = (Field)bf[best];

					if (calc==0)
					{
						at += tfi.getGeoArea();
						dt.addField(tfi);
						fc.add(tfi);
					}
					else
					{
						//System.out.println ("at: " + at  + " est: " + tfi.getEstMu());
						if (at + tfi.getEstMu() < notOver)
						{
							at += tfi.getEstMu();
							dt.addField(tfi);
							fc.add(tfi);
						}
					}
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

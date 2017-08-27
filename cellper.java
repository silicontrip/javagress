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


public class cellper {

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
		
		ArrayList<Point> target=null;
		double lower=0.0;
		double upper=0.0;
		
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

		if (ag.hasOption("u"))
			upper = new Double(ag.getOptionForKey("u")).doubleValue();
		if (ag.hasOption("l"))
			lower = new Double(ag.getOptionForKey("l")).doubleValue();


		if (ag.hasOption("h"))
		{
			System.out.println("Options");
			System.out.println(" -E <number>       Limit number of Enlightened Blockers");
			System.out.println(" -R <number>       Limit number of Resistance Blockers");
			System.out.println(" -C <#colour>      Set Drawtools output colour");
			System.out.println(" -L                Set Drawtools to output as polylines");
			System.out.println(" -O                Output as Intel Link");
			System.out.println(" -u                Upper MU");
			System.out.println(" -l                Lower MU");
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
				
				System.err.println("==  links generated " + rt.split()+ " ==");
				System.err.println("== Generating fields ==");
				
				ArrayList<Field> af = pf.makeFieldsFromSingleLinks(li);
				allfields = pf.filterFields(af,links,maxBl);
				System.err.println("fields: " + allfields.size());

				// allfields = singleCluster(portals);
				
			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
			System.err.println("==  fields generated " + rt.split() + " ==");
			System.err.println("== sorting fields ==");

		Map<Double,Field> blockField = new TreeMap<Double,Field>(Collections.reverseOrder());
			for (Field fi: allfields) {
				blockField.put(fi.getGeoArea(),fi);
			}
			System.err.println("==  fields sortered " + rt.split() + " ==");
			System.err.println("== show matches ==");

		// 	Map<Double,Field> simField = new TreeMap<Double,Field>(Collections.reverseOrder());

			Object[] bf = blockField.values().toArray();

			double at;
				
			for (int i =0; i< bf.length;i++ )  {
				Field tfi = (Field)bf[i];
				at  = tfi.getGeoArea();

				dt.erase();
				dt.addField(tfi);

				double minmu = lower*at;
				double maxmu = upper*at;

				if (maxmu - minmu > 1.0) 
					System.out.println("[" + lower*at +"," + upper*at +"] " + at + " / " + dt.out());
			}

			System.err.println("== Finished. " + rt.split() + " elapsed time. " + rt.stop() + " total time.");
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

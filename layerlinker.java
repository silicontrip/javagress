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
	
	private static final Double nanoPerSec = 1000000000.0;

    public static <K, V> void printMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey()
+ " Value : " + entry.getValue());
        }
    }


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

	/*
// create all fields from 3 portal clusters
	private static ArrayList<Field> tripleCluster(HashMap<String,Portal> p1, HashMap<String,Portal> p2,HashMap<String,Portal> p3)
	{
		
		ArrayList<Field> fa = new ArrayList<Field>();

		for (Portal pki: p1.values())
		{
			for (Portal pkj: p2.values())
			{

				for (Portal pkk: p3.values())
				{
					
					Field fi = new Field (pki,pkj,pkk);
					fa.add(fi);		
				}
			}
		}
		return fa;
	}

	
// make all fields from 2 portals from the first cluster and 1 portal from the second
	private static ArrayList<Field> doubleCluster(HashMap<String,Portal> p1, HashMap<String,Portal> p2)
	{
		ArrayList<Field> fa = new ArrayList<Field>();
		Object[] portalKeys = p1.values().toArray();

		for (Portal pki: p2.values())
		{
			for (int j=0; j<portalKeys.length; j++)
			{
				Portal pkj = (Portal)portalKeys[j];
				for (int k=j+1; k<portalKeys.length; k++)
				{
					Portal pkk = (Portal)portalKeys[k];
					Field fi = new Field (pki,pkj,pkk);
					fa.add(fi);
						
				}
			}
			
		}
		return fa;

		
	}
	
	
	private static HashMap<String,teamCount> getLinkBlockersDouble(Object[] portalKeys, Object[] portal2Keys , Collection<Link> links)
	{
		
		HashMap<String,teamCount> blocksPerLink =  new HashMap<String,teamCount>();
		
		// Object[] portalKeys = portals.values().toArray();
		
		for (int i=0; i < portalKeys.length; i++) {
			
			for (int j = 0; j < portal2Keys.length; j++) {
				
				Portal pi = (Portal)portalKeys[i];
				Portal pj = (Portal)portal2Keys[j];
				
				//	String guidKey = new String (pi.getGuid()+pj.getGuid());
				
				//	System.out.println(guidKey);
				
				Line l =  new Line (pi, pj);
				
				teamCount bb = new teamCount();
				
				for (Link link: links) {
					
					if (l.intersects(link)) {
						bb.incTeamEnum(link.getTeamEnum());  // change to enum
					}
				}
				
				blocksPerLink.put( pi.getGuid()+pj.getGuid(), bb);
				
			}
		}
		return blocksPerLink;
	}
	*/
	
// this should move into the PortalFactory class	
	private static ArrayList<Link> purgeLinks (Collection<Portal> portals, Collection<Link> links) {
		
		boolean first = true;
		Long minLng=0L;
		Long minLat=0L;
		Long maxLng=0L;
		Long maxLat=0L;
		
		ArrayList<Link> purgeList = new ArrayList<Link>();
		
		// determine bounds
		for (Portal portal : portals) {
			
			Long lat = portal.getLatE6();
			Long lng = portal.getLngE6();
			
			if (first) {
				minLng = lng;
				maxLng = lng;
				minLat = lat;
				maxLat = lat;
				first = false;
			}
			
			if (lat > maxLat) { maxLat = lat; }
			if (lng > maxLng) { maxLng = lng; }
			if (lat < minLat) { minLat = lat; }
			if (lng < minLng) { minLng = lng; }
			
		}
		
		// create bounding box
		
		Line line0 = new Line(minLat,minLng, minLat, maxLng);
		Line line1 = new Line(minLat, maxLng,maxLat,maxLng);		
		Line line2 = new Line(maxLat,maxLng, maxLat, minLng);
		Line line3 = new Line(maxLat, minLng,minLat,minLng);
		
		// System.err.println("Bounds: Lat: " + minLat + " - " + maxLat + " Lng: " + minLng + " - " + maxLng);
		
		for (Link link: links) {
			
			// Line linkLine = link.getLine();
			
			// if link intesects or is contained in bounding box
			if ((line0.intersects(link) ||
				 line1.intersects(link) ||
				 line2.intersects(link) ||
				 line3.intersects(link) ) ||
				(link.getoLat() >= minLat && link.getoLat() <= maxLat &&
				 link.getoLng() >= minLng && link.getoLng() <= maxLng)
				)
			{
				purgeList.add(link);
			}
		}
		
		return purgeList;
		
	}
	
	
	public static void main(String[] args) {
		
		long startTime;
		double elapsedTime;
		long runTime;
		double totalTime;
		long endTime;
		int calc=0;
		
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

		// mu calculation
		if (ag.hasOption("M"))
			calc=1;


		try {
			PortalFactory pf = PortalFactory.getInstance();
			
			System.err.println("== Reading links and portals ==");
			
			startTime = System.nanoTime();
			runTime = startTime;
			
			// System.err.println("== " + args.length + " ==");
			
			HashMap<String,Link> allLinks = pf.getLinks();
			
			List<Portal> allPortals = new ArrayList<Portal>();
			
			ArrayList<String> areaOut;
			ArrayList<Link> links;
			
		// ag.getArgumentAt
			ArrayList<Field> allfields;
			if (ag.getArguments().size() == 1) {
				
			// get all 3 points from one cluster 
				
				HashMap<String,Portal> portals = new HashMap<String,Portal>();
				
				portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  portals read " + elapsedTime+ " ==");
				System.err.println("== purging links ==");
				startTime = System.nanoTime();
				
				links = purgeLinks(portals.values(),allLinks.values());
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links read " + elapsedTime+ " ==");
				System.err.println("== test generating links ==");
				startTime = System.nanoTime();
				ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
				System.err.println("all links: " + li.size());
				ArrayList<Line> l2 = pf.filterLinks(li,links,maxBl);
				
				System.err.println("purged links: " + l2.size());

				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links generated " + elapsedTime+ " ==");

				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				allfields = pf.makeFieldsFromSingleLinks(l2);
				System.err.println("fields: " + allfields.size());

				// allfields = singleCluster(portals);
				
			} else if (ag.getArguments().size() == 2) { 
				// one point from one cluster
				// and the other two points from the other cluster
				
				HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals2 = new HashMap<String,Portal>();

				
				portals1 = pf.portalClusterFromString(ag.getArgumentAt(0));
				portals2 = pf.portalClusterFromString(ag.getArgumentAt(1));
				
				
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  portals read " + elapsedTime+ " ==");
				System.err.println("== Reading links ==");
				startTime = System.nanoTime();
				
				
				allPortals = new ArrayList<Portal>();
								
				allPortals.addAll(portals1.values());
				allPortals.addAll(portals2.values());
				
				
				links = purgeLinks(new ArrayList<Portal>(allPortals),allLinks.values());

				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links read " + elapsedTime+ " ==");
				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				ArrayList<Line> li1 = pf.makeLinksFromSingleCluster(portals1.values());
				ArrayList<Line> lf1 = pf.filterLinks(li1,links,maxBl);

				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals1.values(),portals2.values());
				ArrayList<Line> lf2 = pf.filterLinks(li2,links,maxBl);

				allfields = pf.makeFieldsFromDoubleLinks(lf1,lf2);
				
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

				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  portals read " + elapsedTime+ " ==");
				System.err.println("== Reading links ==");
				startTime = System.nanoTime();
				
				
				links = purgeLinks(new ArrayList<Portal>(allPortals),allLinks.values());
				
				// create link blockers 1-2, 2-3 and 3-1 

				// HashMap<String,teamCount> bpl = new HashMap<String,teamCount>();
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links read " + elapsedTime+ " ==");
				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				ArrayList<Line> li1 = pf.makeLinksFromDoubleCluster(portals1.values(),portals2.values());
				ArrayList<Line> lf1 = pf.filterLinks(li1,links,maxBl);

				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals2.values(),portals3.values());
				ArrayList<Line> lf2 = pf.filterLinks(li2,links,maxBl);

				ArrayList<Line> li3 = pf.makeLinksFromDoubleCluster(portals3.values(),portals1.values());
				ArrayList<Line> lf3 = pf.filterLinks(li3,links,maxBl);

				allfields = pf.makeFieldsFromTripleLinks(lf1,lf2,lf3);

				
				
//				allfields = tripleCluster(portals1,portals2,portals3);
				
			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			totalTime = (endTime - runTime)/nanoPerSec;
			System.err.println("==  fields generated " + elapsedTime+ " ==");
			System.err.println("== purge fields ==");
			startTime = System.nanoTime();
			
			// start searching for fields.

			Map<Double,Field> blockField = new TreeMap<Double,Field>(Collections.reverseOrder());
			for (Field fi: allfields) { blockField.put(fi.getGeoArea(),fi); }
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			totalTime = (endTime - runTime)/nanoPerSec;
			System.err.println("==  fields filtered " + elapsedTime+ " ==");
			System.err.println("== show matches ==");
			startTime = System.nanoTime();

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
				int best = findField(bf,i+1,tfi,fc,0.3); // make threshold configurable
				while (best != -1) {

					tfi = (Field)bf[best];

					if (calc==0)
						at  += tfi.getGeoArea();
					else
						at  += tfi.getEstMu();
					dt.addField(tfi);
					fc.add(tfi);
					best = findField(bf,best+1,tfi,fc,0.3); // make threshold configurable
				}
				// calc area, layers 
				// print
				plan.put(at,dt.out());
				if (at>bestbest) {
					bestbest = at;
					System.out.println("" + at + " ("+fc.size()+ ") / " + dt.out());
				}
			}
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			totalTime = (endTime - runTime)/nanoPerSec;
			System.err.println("==  plans searched " + elapsedTime + " ==");
			System.err.println("== show all plans ==");
			startTime = System.nanoTime();


			for (Map.Entry<Double, String> entry : plan.entrySet()) 
			{
				System.out.println(""  + entry.getKey() + " / " + entry.getValue());
				System.out.println("");
			}


			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			totalTime = (endTime - runTime)/nanoPerSec;
			System.err.println("== Finished. " + elapsedTime + " elapsed time. " + totalTime + " total time.");
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

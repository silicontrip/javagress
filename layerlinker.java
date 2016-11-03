
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;
import javax.xml.parsers.*;
import java.io.*;


public class layerlinker {
	
	private static final Double nanoPerSec = 1000000000.0;

public static void printMap(Map mp) {
    Iterator it = mp.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        System.out.println(pair.getKey() + " = " + pair.getValue());
        it.remove(); // avoids a ConcurrentModificationException
    }
}
	
	private static teamCount getBlocks (Portal pki, Portal pkj, Portal pkk,HashMap<String,teamCount> blocksPerLink)
	{
		teamCount block = new teamCount();

		teamCount ij = blocksPerLink.get(pki.getGuid() + pkj.getGuid());
		teamCount jk = blocksPerLink.get(pkj.getGuid() + pkk.getGuid());
		teamCount ik = blocksPerLink.get(pki.getGuid() + pkk.getGuid());
		
		block.setResistance (ij.getResistanceAsInt() + jk.getResistanceAsInt() + ik.getResistanceAsInt());
		block.setEnlightened (ij.getEnlightenedAsInt() + jk.getEnlightenedAsInt() + ik.getEnlightenedAsInt());
		
		return block;
	}
	
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
					
					Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
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
					Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
					fa.add(fi);
						
				}
			}
			
		}
		return fa;

		
	}
	
// method to return all fields from a single cluster of portals.	
// this should become a factory class
	private static  ArrayList<Field> singleCluster(HashMap<String,Portal> portals) {
		
		ArrayList<Field> fa = new ArrayList<Field>();
		Object[] portalKeys = portals.values().toArray();
		
		for (int i =0; i<portalKeys.length; i++) 
		{
			Portal pki = (Portal)portalKeys[i];
			
			for (int j=i+1; j<portalKeys.length; j++)
			{
				Portal pkj = (Portal)portalKeys[j];
				
				
					for (int k=j+1; k<portalKeys.length; k++)
					{
						Portal pkk = (Portal)portalKeys[k];
						Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
						fa.add(fi);	
					}
					
			}
		}
		return fa;
	}
	
	
	private static HashMap<String,teamCount> getLinkBlockersSingle(Object[] portalKeys, Collection<Link> links)
	{
		
		HashMap<String,teamCount> blocksPerLink =  new HashMap<String,teamCount>();
		
		
		// Object[] portalKeys = portals.values().toArray();
		
		for (int i=0; i < portalKeys.length; i++) {
			
			for (int j = i+1; j < portalKeys.length; j++) {
				
				Portal pi = (Portal)portalKeys[i];
				Portal pj = (Portal)portalKeys[j];
				
				//	String guidKey = new String (pi.getGuid()+pj.getGuid());
				
				//	System.out.println(guidKey);
				
				Line l =  new Line (pi.getPoint(), pj.getPoint());
				
				teamCount bb = new teamCount();
				
				for (Link link: links) {
					
					if (l.intersects(link.getLine())) {
						bb.incTeamEnum(link.getTeamEnum());  // change to enum
					}
				}
				
				blocksPerLink.put( pi.getGuid()+pj.getGuid(), bb);
				
			}
		}
		return blocksPerLink;
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
				
				Line l =  new Line (pi.getPoint(), pj.getPoint());
				
				teamCount bb = new teamCount();
				
				for (Link link: links) {
					
					if (l.intersects(link.getLine())) {
						bb.incTeamEnum(link.getTeamEnum());  // change to enum
					}
				}
				
				blocksPerLink.put( pi.getGuid()+pj.getGuid(), bb);
				
			}
		}
		return blocksPerLink;
	}
	
	
	
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
			
			Line linkLine = link.getLine();
			
			// if link intesects or is contained in bounding box
			if ((line0.intersects(linkLine) ||
				 line1.intersects(linkLine) ||
				 line2.intersects(linkLine) ||
				 line3.intersects(linkLine) ) ||
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
			
		System.err.println("== Reading portals ==");
			
		startTime = System.nanoTime();
		runTime = startTime;
			
		// System.err.println("== " + args.length + " ==");
			
		HashMap<String,Link> allLinks = pf.getLinks();
			
		List<Portal> allPortals = new ArrayList<Portal>();
			
		ArrayList<String> areaOut;
			
	// ag.getArgumentAt
		ArrayList<Field> allfields;
		if (ag.getArguments().size() == 1) {
				
		// get all 3 points from one cluster 
				
			HashMap<String,Portal> portals = new HashMap<String,Portal>();
				
			portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			System.err.println("==  portals read " + elapsedTime+ " ==");
			System.err.println("== Reading links ==");
			startTime = System.nanoTime();

				
			ArrayList<Link> links = purgeLinks(portals.values(),allLinks.values());
				
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			System.err.println("==  links read " + elapsedTime+ " ==");
			System.err.println("== Generating fields ==");
			startTime = System.nanoTime();
				
				
			allfields = singleCluster(portals);
				
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
				
				
				
				ArrayList<Link> links = purgeLinks(new ArrayList<Portal>(allPortals),allLinks.values());

				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links read " + elapsedTime+ " ==");
				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				
				// portals1 and and portals2 is crucial ordering.
				// two portals from portals1 and 1 from portals2
				allfields = doubleCluster(portals1,portals2);
				

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
				
				
				ArrayList<Link> links = purgeLinks(new ArrayList<Portal>(allPortals),allLinks.values());
				
				// create link blockers 1-2, 2-3 and 3-1 

				HashMap<String,teamCount> bpl = new HashMap<String,teamCount>();
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links read " + elapsedTime+ " ==");
				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				
				allfields = tripleCluster(portals1,portals2,portals3);
				
			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			totalTime = (endTime - runTime)/nanoPerSec;
			System.err.println("== Finished. " + elapsedTime + " elapsed time. " + totalTime + " total time.");
			
			// start searching for fields.

			for (Field fi: allfields) 
			{
				System.out.println(fi);
			}
			
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

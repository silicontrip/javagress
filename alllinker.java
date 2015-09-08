
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;

public class alllinker {
	
	private static final Double nanoPerSec = 1000000000.0;
	
	private static teamCount getBlocks (Portal pki, Portal pkj, Portal pkk,HashMap<String,teamCount> blocksPerLink)
	{
		teamCount block = new teamCount();
		
		block.setResistance (blocksPerLink.get(pki.getGuid() + pkj.getGuid()).getResistance()+
							 blocksPerLink.get(pkj.getGuid() + pkk.getGuid()).getResistance()+
							 blocksPerLink.get(pki.getGuid() + pkk.getGuid()).getResistance());
		
		block.setEnlightened (blocksPerLink.get(pki.getGuid() + pkj.getGuid()).getEnlightened()+
							  blocksPerLink.get(pkj.getGuid() + pkk.getGuid()).getEnlightened()+
							  blocksPerLink.get(pki.getGuid() + pkk.getGuid()).getEnlightened());
		
		return block;
	}
	
 //   private static setArea(Double area, Integer res, Integer enl)
  //  {
        
        
  //  }
	
	private static ArrayList<String> tripleCluster(HashMap<String,Portal> p1, HashMap<String,Portal> p2,HashMap<String,Portal> p3, HashMap<String,teamCount> blocksPerLink,teamCount max) 
	{
		
		ArrayList<Double> maxArea = new ArrayList<Double>();
		ArrayList<String> areaOut = new ArrayList<String>();
		
		for(int i =0; i<1024; i++)
		{
			maxArea.add(i,0.0);
			areaOut.add(i,null);
		}
		
		for (Portal pki: p1.values())
		{
			for (Portal pkj: p2.values())
			{
				if (!blocksPerLink.get(pki.getGuid() + pkj.getGuid()).moreThan(max)) {

				for (Portal pkk: p3.values())
				{
					
					Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
					
					Double area = fi.getGeoArea();
					
					teamCount block = getBlocks(pki,pkj,pkk,blocksPerLink);
										
					if (!block.moreThan(max)) {
						
						for(int ii =block.getResistance(); ii<1024; ii++) 
						{
							if (area > maxArea.get(ii)) 
							{
								maxArea.set(ii,area);
								areaOut.set(ii,null);
							}
						}
						
						if (area >= maxArea.get(block.getResistance())) {
							maxArea.set(block.getResistance(),area);
							areaOut.set(block.getResistance(), area + ", " + block + ", " + pki + ", " + pkj + ", " +pkk + "  [" + fi.getDraw() + "]");
						}
					}
					
					
				}
				}
			}
		}
		return areaOut;
	}

	
	private static ArrayList<String> doubleCluster(HashMap<String,Portal> p1, HashMap<String,Portal> p2, HashMap<String,teamCount> blocksPerLink,teamCount max) 
	{
		Object[] portalKeys = p1.values().toArray();

		ArrayList<Double> maxArea = new ArrayList<Double>();
		ArrayList<String> areaOut = new ArrayList<String>();
		
		for(int i =0; i<1024; i++) 
		{
			maxArea.add(i,0.0);
			areaOut.add(i,null);
		}

		for (Portal pki: p2.values())
		{
			for (int j=0; j<portalKeys.length; j++)
			{
				Portal pkj = (Portal)portalKeys[j];
				if (!blocksPerLink.get(pki.getGuid() + pkj.getGuid()).moreThan(max)) {
					for (int k=j+1; k<portalKeys.length; k++)
					{
						Portal pkk = (Portal)portalKeys[k];
						Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
						
						Double area = fi.getGeoArea();
						
						teamCount block = getBlocks(pki,pkj,pkk,blocksPerLink);
						
						//	if (area >= maxArea) {
						
						
						if (!block.moreThan(max)) {
							
							for(int ii =block.getResistance(); ii<1024; ii++) 
							{
								if (area > maxArea.get(ii)) 
								{
									maxArea.set(ii,area);
									areaOut.set(ii,null);
								}
							}
							
							if (area >= maxArea.get(block.getResistance())) {
								maxArea.set(block.getResistance(),area);
								areaOut.set(block.getResistance(), area + ", " + block + ", " + pki + ", " + pkj + ", " +pkk + "  [" + fi.getDraw() + "]");
							}
						}
						
					}
				}
			}
			
		}
		return areaOut;

		
	}
	
	
	private static  ArrayList<String> singleCluster(HashMap<String,Portal> portals, HashMap<String,teamCount> blocksPerLink,teamCount max) {
		
		Object[] portalKeys = portals.values().toArray();
		
		ArrayList<Double> maxArea = new ArrayList<Double>();
		ArrayList<String> areaOut = new ArrayList<String>();
		
		for(int i =0; i<1024; i++) 
		{
			maxArea.add(i,0.0);
			areaOut.add(i,null);
		}
		
		for (int i =0; i<portalKeys.length; i++) 
		{
			Portal pki = (Portal)portalKeys[i];
			
			for (int j=i+1; j<portalKeys.length; j++)
			{
				Portal pkj = (Portal)portalKeys[j];
				
				
				if (!blocksPerLink.get(pki.getGuid() + pkj.getGuid()).moreThan(max)) {
					for (int k=j+1; k<portalKeys.length; k++)
					{
						Portal pkk = (Portal)portalKeys[k];
						Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
						
						Double area = fi.getGeoArea();
						
						teamCount block = getBlocks(pki,pkj,pkk,blocksPerLink);
						
						if (!block.moreThan(max)) {
							
							for(int ii =block.getResistance(); ii<1024; ii++) 
							{
								if (area > maxArea.get(ii)) 
								{
									maxArea.set(ii,area);
									areaOut.set(ii,null);
								}
							}
							
							if (area >= maxArea.get(block.getResistance())) {
								maxArea.set(block.getResistance(),area);
								areaOut.set(block.getResistance(), area + ", " + block + ", " + pki + ", " + pkj + ", " +pkk + "  [" + fi.getDraw() + "]");
							}
						}
						
					}
					
				}
			}
		}
		return areaOut;
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
		
//		ArrayList<String> alArgs = new ArrayList<String>(Arrays.asList(args));
		
	//	for (String a: args) { System.out.println(a); }

		
		teamCount maxBl = new teamCount(args);
		
		
		// ugly hack to modify args array.s
		int newLength =args.length ;
		for (int c=0; c<args.length; c++) 
		{
			if (args[c] == null) {
				newLength = c;
				c = args.length;
			}
		}
		
		
		String[] newArgs = new String[newLength];
		System.arraycopy (args,0,newArgs,0,newLength);
		args = newArgs;
		
	//	 for (String a: args) { System.out.println(a); }

		
		try {
			PortalFactory pf = PortalFactory.getInstance();
			
			System.err.println("== Reading portals ==");
			
			startTime = System.nanoTime();
			runTime = startTime;
			
			// System.err.println("== " + args.length + " ==");
			
			HashMap<String,Link> allLinks = pf.getLinks();
			
			List<Portal> allPortals = new ArrayList<Portal>();
			
			ArrayList<String> areaOut;
			
			if (args.length == 1) {
				
				// get all 3 points from one cluster 
				
				HashMap<String,Portal> portals = new HashMap<String,Portal>();
				
				portals = pf.portalClusterFromString(args[0]);
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  portals read " + elapsedTime+ " ==");
				System.err.println("== Reading links ==");
				startTime = System.nanoTime();

				
				ArrayList<Link> links = purgeLinks(portals.values(),allLinks.values());
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links read " + elapsedTime+ " ==");
				System.err.println("== Creating link cache ==");
				startTime = System.nanoTime();
				

				HashMap<String,teamCount> bpl = getLinkBlockersSingle(portals.values().toArray(), links);

				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  Cache created " + elapsedTime+ " ==");
				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				
				areaOut = singleCluster(portals, bpl,maxBl);
				
			}
			else if (args.length == 2) {
				
				// one point from one cluster
				// and the other two points from the other cluster
				
				HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals2 = new HashMap<String,Portal>();

				
				portals1 = pf.portalClusterFromString(args[0]);
				portals2 = pf.portalClusterFromString(args[1]);
				
				
				
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
				System.err.println("== Creating link cache ==");
				startTime = System.nanoTime();
				
				
				// create link blockers 1 and 1-2 
				HashMap<String,teamCount> bpl = new HashMap<String,teamCount>();
				
				HashMap<String,teamCount> tblocks;
				
				tblocks = getLinkBlockersSingle(portals1.values().toArray(), links);
				bpl.putAll(tblocks);
				tblocks = getLinkBlockersDouble(portals2.values().toArray(),portals1.values().toArray(), links);
				bpl.putAll(tblocks);

				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  Cache created " + elapsedTime+ " ==");
				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				
				// portals1 and and portals2 is crucial ordering.
				areaOut = doubleCluster(portals1,portals2,bpl,maxBl);
				

			}
			else if (args.length == 3) {
				
				// one point from each cluster
				
				HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals2 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals3 = new HashMap<String,Portal>();

				
				portals1 = pf.portalClusterFromString(args[0]);
				portals2 = pf.portalClusterFromString(args[1]);
				portals3 = pf.portalClusterFromString(args[2]);

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
				System.err.println("== Creating link cache ==");
				startTime = System.nanoTime();
				
				
				HashMap<String,teamCount> tblocks;
				tblocks = getLinkBlockersDouble(portals1.values().toArray(),portals2.values().toArray(), links);
				bpl.putAll(tblocks);
				tblocks = getLinkBlockersDouble(portals2.values().toArray(),portals3.values().toArray(), links);
				bpl.putAll(tblocks);
				tblocks = getLinkBlockersDouble(portals1.values().toArray(),portals3.values().toArray(), links);
				bpl.putAll(tblocks);

				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  Cache created " + elapsedTime+ " ==");
				System.err.println("== Generating fields ==");
				startTime = System.nanoTime();
				
				
				areaOut = tripleCluster(portals1,portals2,portals3,bpl,maxBl);
				
			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			/*
			elasedTime = (System.nanoTime() - startTime)/nanoPerSec;
			System.err.println("== Finished reading. " + elasedTime + " elapsed time.");
			
			System.err.println("== " + portals.size() + " Portals. " + allLinks.size() + " Links."); 
			
			startTime = System.nanoTime();
			
			
			elasedTime = (System.nanoTime() - startTime)/nanoPerSec;
			System.err.println("== Finished purging. " + elasedTime + " elapsed time.");
			
			
			System.err.println("== " +links.size() + " links remaining in Bounds");
			System.err.println("== creating block cache ==");
			startTime = System.nanoTime();
			
			// need different methods depending on the clusters.
			HashMap<String,teamCount> blocksPerLink = getLinkBlockersSingle(portals.values().toArray(), links);
			
			elasedTime = (System.nanoTime() - startTime)/nanoPerSec;
			System.err.println("== Finished blocking. " + elasedTime + " elapsed time.");
			
			
			System.err.println("== Created " + blocksPerLink.size() + " blocked links");
			*/
			
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			totalTime = (endTime - runTime)/nanoPerSec;
			System.err.println("== Finished. " + elapsedTime + " elapsed time. " + totalTime + " total time.");
			
			
			
			for(int i =0; i<1024; i++) 
			{
				if (areaOut.get(i) != null ) {
					System.out.println(areaOut.get(i));
				}
			}
			
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

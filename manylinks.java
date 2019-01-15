
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;

public class manylinks {
	
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
			
			//Line linkLine = link.getLine();
			
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
	
	public static teamCount linkBlocks(Portal pki, Portal pkj, ArrayList<Link> links)
	{
		Line l =  new Line (pki, pkj);
		teamCount bb = new teamCount();
		
		for (Link link: links) {
			if (l.intersects(link)) {
				bb.incTeamEnum(link.getTeamEnum());
			}
		}
		return bb;
	}
	
	public static boolean linkExists(Portal pki, Portal pkj, ArrayList<Link> links)
	{
		Line l =  new Line (pki, pkj);
		
		
		for (Link link: links) {
			if (l.equals(link)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static void main(String[] args) {
		
		long startTime;
		double elapsedTime;
		long runTime;
		double totalTime;
		long endTime;
		
		
		
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
			
			HashMap<Portal,Boolean> source = new HashMap<Portal,Boolean>();
			HashMap<Portal,Boolean> destination = new HashMap<Portal,Boolean>(); 
			
			HashMap<String,Boolean> dual = new HashMap<String,Boolean>();
			HashMap<String,Boolean> single = new HashMap<String,Boolean>();
			HashMap<String,Boolean> onlylink = new HashMap<String,Boolean>();
			
			
			if (args.length == 2) {
				
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
				
				Link[] linksA = links.toArray(new Link[links.size()]);
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  links read " + elapsedTime+ " ==");
				System.err.println("== finding linked portals ==");
				startTime = System.nanoTime();
				
				
				// HashMap<String,teamCount> blocksPerLink;
				
				
				// blocksPerLink = getLinkBlockersDouble(portals1.values().toArray(),portals2.values().toArray(), links);
				
				Object[] portal2Keys = portals2.values().toArray();
				
				
				
				// fields requiring 2 links. 
				
				//	System.out.println("== 2 link fields ==");
				
				for (int i=0; i < portal2Keys.length; i++) {
					
					Portal pki = (Portal)portal2Keys[i];
					if (pki.isEnlightened() && pki.getResCount() == 8) {
						for (int j = i+1; j < portal2Keys.length; j++) {
							
							Portal pkj = (Portal)portal2Keys[j];
							if (pkj.getResCount() == 8) {
								Line l =  new Line (pki, pkj);
								
								
								if (l.equals(linksA)) {								
									for (Portal pkk: portals1.values())
									{
										if (!pkk.equals(pkj) && !pkk.equals(pki)) {
											
											Line l1 = new Line (pkk, pki);
											Line l2 = new Line (pkk, pkj);
											
											if (!l1.intersectsOrEquals(linksA) && !l2.intersectsOrEquals(linksA)) {
												source.put(pkk,Boolean.TRUE);
												destination.put(pki,Boolean.TRUE);
												destination.put(pkj,Boolean.TRUE);
												dual.put(pkk.getGuid() + pki.getGuid(),Boolean.TRUE);
												dual.put(pkk.getGuid() + pkj.getGuid(),Boolean.TRUE);
												
									//			System.out.println("" + pkk + " => " + pki + " & " + pkj);
											}
										}
									}
								}
							}
						}
					}
				}
				// fields requiring 1 link
				// 	System.out.println("== 1 link fields ==");
				
				for (Portal pki : portals2.values()) {
					
					if (pki.isEnlightened() && pki.getResCount() == 8 ) {
						//System.out.println ("From: " + pki);
						
						// get all connected portals
						Portal[] portalsConnected = pki.getConnectedPortals(linksA,portals1);
						for (Portal pkj: portalsConnected) {
							//	 System.out.println ("To: " + pkj);
							
							// get all connected portals
							Portal[] portalsConnected2 = pkj.getConnectedPortals(linksA,portals1);
							for (Portal pkk: portalsConnected2) {
								
								if (!pkk.equals(pki)) {
									//		 	System.out.println ("Then: " + pkk);
									
									Line l =  new Line (pki, pkk);
									if (!l.intersectsOrEquals(linksA)) {
										source.put(pkk,Boolean.TRUE);
										destination.put(pki,Boolean.TRUE);
										single.put(pkk.getGuid() + pki.getGuid(),Boolean.TRUE);
										
									//	System.out.println("" + pkk + " -> " + pki + " via " + pkj);
									}
									
								}
								
								
							}
							
							
						}
					}	
				}
				
				
				endTime = System.nanoTime();
				elapsedTime = (endTime - startTime)/nanoPerSec;
				System.err.println("==  Cache created " + elapsedTime+ " ==");
				System.err.println("== Counting links ==");
				startTime = System.nanoTime();
				
				
				
				for (Portal pki: portals1.values())
				{
					int count = 0;
					ArrayList<Portal> connected = new ArrayList<Portal>();
					for (Portal pkj: portals2.values())
					{
						
						// System.out.print(pki + " -> " + pkj + " : ");
						
						if (!pki.equals(pkj)) {
							Line l =  new Line (pki, pkj);
							
							if (!l.intersectsOrEquals(links.toArray(new Link[links.size()])) && pkj.getResCount() == 8 && pkj.isEnlightened()) {
								connected.add(pkj);
								
								source.put(pki,Boolean.TRUE);
								destination.put(pkj,Boolean.TRUE);
								onlylink.put(pki.getGuid() + pkj.getGuid(),Boolean.TRUE);
								
								count ++;
							}
						}
					}
				/*	
					if (count > 0) {
						//int dlinks = pki.countdLinks(links.toArray(new Link[links.size()]));
						int olinks = pki.countoLinks(links.toArray(new Link[links.size()]));
						
						int max = 8 - olinks;
						if (pki.isEnlightened()) { max = 8; }
						
						if (count > max) { count = max; }
						
						// System.out.print(count + " : " + pki.toExtendedString());
                        System.out.println(pki.toExtendedString());
						
						for (Portal p : connected) 
						{
							
							System.out.println("" + pki + " -- " + p);
							
							//			System.out.print (", " + p);
						}
						//		System.out.println("");
					}
				 */
				}
				
				
				
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
			
			
			System.out.println("<table border=\"1\">");
			System.out.println("<tr><th>origin</th>");
			for (Portal pkj: destination.keySet()) {
				System.out.println("<th>" + pkj.getTitle() + "</th>");
			}
			System.out.println("</tr>");
			int bkcolour=0;
			for (Portal pki: source.keySet()) {

				if (bkcolour == 1) {
					System.out.println("<tr bgcolor=\"#dddddd\">");
				} else {
					System.out.println("<tr>");
				}
				bkcolour = 1 - bkcolour;
				
				System.out.println("<td><a href=\"" + pki.getUrl() + "\">"+ pki.getTitle() + "</a></td>");  
				
				
				for (Portal pkj: destination.keySet()) {
					
					if (single.containsKey(pki.getGuid() + pkj.getGuid()))
					{
						System.out.println("<td>F1</td>");

					} else if (dual.containsKey(pki.getGuid() + pkj.getGuid()))
					{
						System.out.println("<td>F2</td>");

					} else if (onlylink.containsKey(pki.getGuid() + pkj.getGuid()))
					{
						System.out.println("<td>L</td>");
					} else {
						System.out.println("<td>&nbsp;</td>");
					}
					
					
				}
				System.out.println("</tr>");
				
			}
			System.out.println("</table>");

			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

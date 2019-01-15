
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;

import java.lang.StringBuilder;
import java.util.Arrays;

public class multilinker {
	
	private static final Double nanoPerSec = 1000000000.0;
	
	
	private static Double  searchFields (ArrayList<Field> list, Object[] fields, int start, Double maxArea,int depth, int maxFields) 
	{
		if (depth <= maxFields) {
			if (list.size() > 0) {
				
				Double thisArea = sizeFields(list);
				if (thisArea > maxArea) {
					System.out.println(thisArea + " : " + drawFields(list));
					System.out.println("");
					maxArea = thisArea;
				}
			}
			
			for (int i =start; i<fields.length; i++) 
			{
				Field thisField = (Field)fields[i];
				
				//	System.err.println(" - new Field Intersect - ");
				
				if (!newFieldIntersect(list,thisField)) 
				{
					//		System.err.println(" = END new Field Intersect = ");
					
					
					ArrayList<Field> newlist = new ArrayList<Field>(list);
					newlist.add((Field)fields[i]);
					
					if (fieldIntersect(newlist)) {
						throw new RuntimeException("Field Collision : " + drawFields(list) + " / " + thisField );
						
					}
					
					
					maxArea = searchFields(newlist,fields,i+1,maxArea,depth+1,maxFields);
				}
				
			}
		}
		return maxArea;
	}
	
	private static ArrayList<Field> findFields (Collection<Portal>portals, 
												HashMap<String,teamCount> blocksPerLink,
												Portal anchor1,Portal anchor2) 
	{
		
		ArrayList<Field> fields = new ArrayList<Field>();
		
		for (Portal anchor3: portals)
		{
			
			if (!(anchor3.getGuid().equals(anchor1.getGuid()) || anchor3.getGuid().equals(anchor2.getGuid()))) {
				
				
				//				Portal pkk = (Portal)portalKeys[k];
				Field fi = new Field (anchor1,anchor2,anchor3);
				
				
				Double area = fi.getGeoArea();
				
				teamCount block = new teamCount();
				
				//	System.out.println("Portal: " + pkk.getGuid());
				
				teamCount l1 = blocksPerLink.get(anchor1.getGuid() + anchor2.getGuid());
				teamCount l2 = blocksPerLink.get(anchor2.getGuid() + anchor3.getGuid());
				teamCount l3 = blocksPerLink.get(anchor3.getGuid() + anchor1.getGuid());
				
				//	System.out.println ("link 1 " + l1 + ". link 2 " + l2 + ". link 3 " + l3 + ".");
				
				// block.setResistance (l1.getResistance() + l2.getResistance() + l3.getResistance());
				
				block.setEnlightened (l1.getEnlightened() + l2.getEnlightened() + l3.getEnlightened());
				
				
				if (!block.anyEnlightenedBlockers()) {
					//				System.out.println ( area + ", " + block + ", " + pki + ", " + pkj + ", " +pkk + "  " + fi.getDraw());
					fields.add(fi);
					
				}
			}
		}
		return fields;
		
	}
	
	
	private static Double sizeFields(List<Field> fa) 
	{
		Double area = 0.0;
		
		for (Field fi: fa) 
		{
			area += fi.getGeoArea();
		}
		return area;
	}
	
	private static String drawFields(List<Field> fa) 
	{
		
		StringBuilder rs = new StringBuilder(1024);
		
		rs.append("[");
		
		boolean first = true;
		
		for (Field fi: fa) 
		{
			if (!first)
			{
				rs.append(",");
			}
			
			rs.append(fi.getDraw());
			first = false;
		}
		rs.append("]");
		
		return rs.toString();
		
	}
	
	private static boolean newFieldIntersect (List<Field> fa,Field f) 
	{
		for (Field fi: fa) {
			if (fi.intersects(f)) { return true; }
			if (fi.equals(f)) { return true; }
			
			//System.err.println("no intersect: [" + fi + "," + f + "]");
			
			
		}
		return false;
	}
	
	
	private static boolean fieldIntersect (List<Field> fa) 
	{
		
		for (Field fi: fa) {
			for (Field fj: fa) {
				
				if (!fi.equals(fj)) {
					
					if (fi.intersects(fj)) {
						
						
						/*	
						 System.out.print("[");
						 
						 System.out.print(fi.getDraw());
						 System.out.print(",");
						 System.out.print(fj.getDraw());
						 System.out.println("]");
						 */
						return true;
					}
				}
				
			}
		}
		return false;
	}
	
	private static HashMap<String,teamCount> getLinkBlockers(Object[] portalKeys, Collection<Link> links)
	{
		
		HashMap<String,teamCount> blocksPerLink =  new HashMap<String,teamCount>();
		
		
		// Object[] portalKeys = portals.values().toArray();
		
		for (int i=0; i < portalKeys.length; i++) {
			
			for (int j = i+1; j < portalKeys.length; j++) {
				
				Portal pi = (Portal)portalKeys[i];
				Portal pj = (Portal)portalKeys[j];
				
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
				blocksPerLink.put( pj.getGuid()+pi.getGuid(), bb);
				
				
			}
		}
		return blocksPerLink;
	}
	
	
	
	
	public static void main(String[] args) {
		
		long startTime;
		double elasedTime;
		long runTime;
		double totalTime;
		long endTime;
		
		
		try {
			PortalFactory pf = PortalFactory.getInstance();
			
			System.err.println("== Reading portals and links ==");
			
			startTime = System.nanoTime();
			runTime = startTime;
			
			// System.err.println("== " + args.length + " ==");
			
			
			HashMap<String,Portal> portals = new HashMap<String,Portal>();
			
			int maxLinks = Integer.parseInt(args[3]);
			
			portals = pf.getPortalsInTri(args[0],args[1],args[2]);
			Collection<Portal> portalArray = portals.values();
			
			ArrayList<Link> allLinks = pf.getPurgedLinks(portals.values());
			
			Portal pki = pf.getPortal(args[0]);
			Portal pkj = pf.getPortal(args[1]);
			Portal pkk = pf.getPortal(args[2]);			
			
			elasedTime = (System.nanoTime() - startTime)/nanoPerSec;
			System.err.println("== Finished reading. " + elasedTime + " elapsed time.");
			
			System.err.println("== " + portals.size() + " Portals. " + allLinks.size() + " Links."); 
			System.err.println("== creating block cache ==");
			startTime = System.nanoTime();
			
			HashMap<String,teamCount> blocksPerLink = getLinkBlockers(portals.values().toArray(), allLinks);
			
			elasedTime = (System.nanoTime() - startTime)/nanoPerSec;
			System.err.println("== Finished blocking. " + elasedTime + " elapsed time.");
			
			
			System.err.println("== Created " + blocksPerLink.size() + " blocked links");
			
			//			Object[] portalKeys = portals.values().toArray();
			
			/*
			 for (int i =0; i<portalKeys.length; i++) 
			 {
			 System.out.println ( (Portal)portalKeys[i] );
			 }
			 */
			
			startTime = System.nanoTime();
			ArrayList <Field> fields;
			
			System.out.println (" == " + pki.getTitle() + " -- " + pkj.getTitle() + " ==");
			fields = findFields(portalArray,blocksPerLink,pki,pkj);
			System.out.println(fields.size() + " possible fields");
			if (fields.size()>0) {
				searchFields(new ArrayList<Field>() , fields.toArray(),0,0.0,0,maxLinks);
			}
			
			
			
			
			System.out.println (" == " + pkj.getTitle() + " -- " + pkk.getTitle() + " ==");
			
			fields = findFields(portalArray,blocksPerLink,pkj,pkk);
			System.out.println(fields.size() + " possible fields");
			if (fields.size()>0) {
				searchFields(new ArrayList<Field>() , fields.toArray(),0,0.0,0,maxLinks);
			}
			
			
			System.out.println (" == " + pkk.getTitle() + " -- " + pki.getTitle() + " ==");
			
			fields = findFields(portalArray,blocksPerLink,pkk,pki);
			System.out.println(fields.size() + " possible fields");
			if (fields.size()>0) {
				searchFields(new ArrayList<Field>() , fields.toArray(),0,0.0,0,maxLinks);
			}
			
			
			endTime = System.nanoTime();
			elasedTime = (endTime - startTime)/nanoPerSec;
			totalTime = (endTime - runTime)/nanoPerSec;
			System.err.println("== Finished. " + elasedTime + " elapsed time. " + totalTime + " total time.");
			
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

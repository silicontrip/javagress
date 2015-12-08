
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

public class multilinker2 {
	
	private static final Double nanoPerSec = 1000000000.0;
	
	
	private static Double  searchFields (ArrayList<Field> list, Object[] fields, int start, Double maxArea,int depth, int maxFields, DrawTools dt) 
	{
		if (depth <= maxFields) {
			if (list.size() > 0) {
				
				Double thisArea = sizeFields(list);
				if (thisArea > maxArea) {
					System.out.println(thisArea + " : " + drawFields(list,dt));
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
						throw new RuntimeException("Field Collision : " + drawFields(list,dt) + " / " + thisField );
						
					}
					
					
					maxArea = searchFields(newlist,fields,i+1,maxArea,depth+1,maxFields,dt);
				}
				
			}
		}
		return maxArea;
	}
	
	private static Field getLargest(ArrayList<Field> fs) 
	{
	
		Field large=null;
		boolean first = true;
		double max=0.0;
		
		for (Field f: fs)
		{
			double gg =  f.getGeoArea();
			if (gg> max || first)
			{
				max = f.getGeoArea();
				first = false;
				large = f;
			}
			
		}
		
		return large;
		
	}
	
	private static ArrayList<Field> findFields (Collection<Portal>portals, 
												HashMap<String,teamCount> blocksPerLink,
												Field bounds, int exclude, teamCount max) 
	{
		
		ArrayList<Field> fields = new ArrayList<Field>();
		Portal anchor1 = null;
		Portal anchor2 = null;
		Portal anchor3 = null;
		
		
		if (exclude == 0) {
			 anchor1 = bounds.getPortal(1);
			 anchor2 = bounds.getPortal(2);
			 anchor3 = bounds.getPortal(0);
		}
		if (exclude == 1) {
			 anchor1 = bounds.getPortal(2);
			 anchor2 = bounds.getPortal(0);
			 anchor3 = bounds.getPortal(1);
		}
		if (exclude == 2) {
			 anchor1 = bounds.getPortal(0);
			 anchor2 = bounds.getPortal(1);
			 anchor3 = bounds.getPortal(2);
		}
		
		
		for (Portal anchorSearch: portals)
		{
			
			if (!(anchorSearch.getGuid().equals(anchor1.getGuid()) || 
				  anchorSearch.getGuid().equals(anchor2.getGuid()) || 
				  anchorSearch.getGuid().equals(anchor3.getGuid())) &&
				bounds.inside(anchorSearch.getPoint())) {
				
				
				//				Portal pkk = (Portal)portalKeys[k];
				Field fi = new Field (anchor1,anchor2,anchorSearch);
				
				teamCount block = new teamCount();
				
				//	System.out.println("Portal: " + pkk.getGuid());
				
                if (!max.dontCare()) {
			teamCount l1 = blocksPerLink.get(anchor1.getGuid() + anchor2.getGuid());
			teamCount l2 = blocksPerLink.get(anchor2.getGuid() + anchorSearch.getGuid());
			teamCount l3 = blocksPerLink.get(anchorSearch.getGuid() + anchor1.getGuid());
				
			//System.out.println ("link 1 " + l1 + ". link 2 " + l2 + ". link 3 " + l3 + ".");
				
			block.setResistance (l1.getResistance());
			block.addResistance (l2.getResistance());
			block.addResistance (l3.getResistance());
				
			block.setEnlightened (l1.getEnlightened());
			block.addEnlightened (l2.getEnlightened());
			block.addEnlightened (l3.getEnlightened());
                }
				
				if (!block.moreThan(max)) {
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
	
        private static String drawFields(List<Field> fa, DrawTools dt)
        {
        

                dt.erase();
                
                for (Field fi: fa)
                        dt.addField(fi);
                
                return dt.out();
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
				
				Line l =  new Line (pi.getPoint(), pj.getPoint());
				
				teamCount bb = new teamCount();
				
				for (Link link: links) {
					
					if (l.intersects(link.getLine())) {
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

	int maxLinks=0; 
	if (ag.hasOption("N"))
		maxLinks = Integer.parseInt(ag.getOptionForKey("N"));
		
		
        if (maxBl.dontCare())
            System.out.println("limits: " + maxBl);
		
		try {
			PortalFactory pf = PortalFactory.getInstance();
			
			System.err.println("== Reading portals and links ==");
			
			startTime = System.nanoTime();
			runTime = startTime;
			
			// System.err.println("== " + args.length + " ==");
			
			
			HashMap<String,Portal> portals = new HashMap<String,Portal>();
			
			
			portals = pf.portalClusterFromString(ag.getArgumentAt(0));
			
			//portals = pf.getPortalsInTri(args[0],args[1],args[2]);
			
			Collection<Portal> portalArray = portals.values();
			
			ArrayList<Link> allLinks = pf.getPurgedLinks(portals.values());
			
			Portal corner[] ;
			
			
			corner = pf.getCornerPortalsFromString(ag.getArgumentAt(0));

			
			Field outerField = new Field(corner[0],corner[1],corner[2]);
			
			elasedTime = (System.nanoTime() - startTime)/nanoPerSec;
			System.err.println("== Finished reading. " + elasedTime + " elapsed time.");
			
			System.err.println("== " + portals.size() + " Portals. " + allLinks.size() + " Links.");
            
            HashMap<String,teamCount> blocksPerLink = null;
            
            if (!maxBl.dontCare()) {

                System.err.println("== creating block cache ==");
                startTime = System.nanoTime();
			
                blocksPerLink = getLinkBlockers(portals.values().toArray(), allLinks);
			
                elasedTime = (System.nanoTime() - startTime)/nanoPerSec;
			
                System.err.println("== Finished blocking. " + elasedTime + " elapsed time.");
			
			
                System.err.println("== Created " + blocksPerLink.size() + " blocked links");
            }
			//			Object[] portalKeys = portals.values().toArray();
			
			/*
			 for (int i =0; i<portalKeys.length; i++) 
			 {
			 System.out.println ( (Portal)portalKeys[i] );
			 }
			 */
			
			startTime = System.nanoTime();
			
			ArrayList <Field> result = new ArrayList <Field>();
			ArrayList <Field> largest;
			
			do {
				
				result.add(outerField);

				
				ArrayList <Field> fields0 = findFields(portalArray,blocksPerLink,outerField,0,maxBl);
				ArrayList <Field> fields1 = findFields(portalArray,blocksPerLink,outerField,1,maxBl);
				ArrayList <Field> fields2 = findFields(portalArray,blocksPerLink,outerField,2,maxBl);

				largest = new ArrayList <Field>();

			
				if (fields0.size() > 0) { largest.add(getLargest(fields0)); }
				if (fields1.size() > 0) { largest.add(getLargest(fields1)); }
				if (fields2.size() > 0) { largest.add(getLargest(fields2)); }
			
				
				if (largest.size() > 0) {
					Field l = getLargest(largest);
				
				//System.out.println(l.getGeoArea() + " + [" + l + "]");

				
				
					outerField = l;
				}
			
				maxLinks--;
				
			} while (largest.size() > 0 && maxLinks != 0); 
			
			
			Double thisArea = sizeFields(result);
				System.out.println(thisArea + " : " + drawFields(result,dt));
				System.out.println("");
				
			
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

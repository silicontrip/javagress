import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.*;
import java.io.*;


public class megaplan {


	private static String drawFields(List<Field> fa, DrawTools dt)
	{
	

		dt.erase();
		
		for (Field fi: fa)
			dt.addField(fi);
		
		return dt.out();
	}
	
	private static boolean newFieldIntersect (List<Field> fa,Field f,boolean touch)
	{
		for (Field fi: fa) {
			if (fi.intersects(f)) { return true; }
			if (fi.equals(f)) { return true; }
			if (touch && fi.touches(f)) { return true; }
		}
		return false;
	}
	
	
	private static boolean fieldIntersect (List<Field> fa)
	{
		
		for (Field fi: fa) {
			for (Field fj: fa) {
				
				if (!fi.equals(fj)) {
					if (fi.intersects(fj)) {
						return true;
					}
				}
				
			}
		}
		return false;
	}

	
	private static Double sizeFields(List<Field> fa) throws ParserConfigurationException, IOException
	{
		Double area = 0.0;
		
		for (Field fi: fa)
		{
			//area += fi.getGeoArea();
			area += fi.getEstMu();
		}
		return area;
	}

        private static int iterSearchFields (DrawTools dt, Object[] fields,boolean touch) throws ParserConfigurationException, IOException
        {

                int mostFields = 0;
		Double largestArea = 0.0;

                ArrayList<Integer> sStack = new ArrayList<Integer>();
                ArrayList<ArrayList<Field>> fStack = new ArrayList<ArrayList<Field>>();

		sStack.add(new Integer(0));
		fStack.add(new ArrayList<Field>());

                while (sStack.size() > 0) {

                        ArrayList<Field> list = fStack.remove(fStack.size()-1);
                        Integer start = sStack.remove(sStack.size()-1);
                        if (list.size() > 0) {
                                int thisSize = list.size();

                                if (thisSize > mostFields)
					largestArea = 0.0;
                                if (thisSize >= mostFields)
                                {
                                        mostFields = thisSize;
                                        Double thisAreaSize = sizeFields(list);
					if (thisAreaSize > largestArea) {
						System.out.println(thisSize + " : " +thisAreaSize + " : "  + drawFields(list,dt));
						System.out.println("");
						largestArea = thisAreaSize;
					}
                                }
                        }

                        for (int i =start; i<fields.length; i++)
                        {
                                Field thisField = (Field)fields[i];
                                if (!newFieldIntersect(list,thisField,touch))
                                {
                                        ArrayList<Field> newlist = new ArrayList<Field>(list);
                                        newlist.add((Field)fields[i]);


                                        sStack.add (new Integer(i+1));
                                        fStack.add (newlist);

                                }
                        }

                }

                return mostFields;


        }


	
	private static Double searchFields (ArrayList<Field> list, Object[] fields, int start, Double maxArea,int depth,DrawTools dt,boolean touch) throws ParserConfigurationException, IOException
	{
			if (list.size() > 0) {
				
			//	Double thisArea = sizeFields(list);
				// we want to maximise number of fields
				Double thisArea = new Double(list.size()); // not really area but number of fields
				Double maxAreaSmall = Math.floor(maxArea);
				
				if (thisArea >= maxAreaSmall) {
					int retval = Double.compare(thisArea, maxAreaSmall);
					if (retval == 0) { maxArea += 0.0001; } else { maxArea = thisArea; }
					Double thisAreaSize = sizeFields(list);
					System.out.println( String.format("%.4f",maxArea) +  " : " + thisAreaSize + " : " + drawFields(list,dt));
					System.out.println("");
				}
			}
			
			for (int i =start; i<fields.length; i++)
			{
				Field thisField = (Field)fields[i];
				
				//	System.err.println(" - new Field Intersect - ");
				
				if (!newFieldIntersect(list,thisField,touch))
				{
					//		System.err.println(" = END new Field Intersect = ");
					
					
					ArrayList<Field> newlist = new ArrayList<Field>(list);
					newlist.add((Field)fields[i]);
					
					if (fieldIntersect(newlist)) {
						throw new RuntimeException("Field Collision : " + drawFields(list,dt) + " / " + thisField );
						
					}
					
					
					maxArea = searchFields(newlist,fields,i+1,maxArea,depth+1,dt,touch);
				}
				
			}
		
		return maxArea;
	}

	
    private static BlockList getLinkBlockersSingle(Object[] portalKeys, Collection<Link> links)
    {
        
        BlockList blocksPerLink = new BlockList();
        
        
        // Object[] portalKeys = portals.values().toArray();
        
        for (int i=0; i < portalKeys.length; i++) {
            
            for (int j = i+1; j < portalKeys.length; j++) {
                
                Portal pi = (Portal)portalKeys[i];
                Portal pj = (Portal)portalKeys[j];
                
                //	String guidKey = new String (pi.getGuid()+pj.getGuid());
                
                //	System.out.println(guidKey);
                
                Line l =  new Line (pi, pj);
                
                teamCount bb = new teamCount();
                blocksPerLink.put(pi,pj,bb);
                for (Link link: links) {
                    if (l.intersects(link)) {
                        // System.out.println("< " + pi  + ":" + pj +  " link: " + blocksPerLink.get(pi,pj));
                        blocksPerLink.incTeamEnum(pi,pj,link.getTeamEnum());
                    } else if (l.equalLine(link)) {
                        blocksPerLink.setExists(pi,pj,true);
                    }
                }
            }
        }
        return blocksPerLink;
    }


public static void main(String[] args) {

    
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
	
	
    try {
        PortalFactory pf = PortalFactory.getInstance();
        
        System.err.println("== Reading portals ==");
        
		HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
		HashMap<String,Portal> portals2 = new HashMap<String,Portal>();
		HashMap<String,Portal> portals3 = new HashMap<String,Portal>();
		
		
		portals1 = pf.portalClusterFromString(ag.getArgumentAt(0));
		portals2 = pf.portalClusterFromString(ag.getArgumentAt(1));
		portals3 = pf.portalClusterFromString(ag.getArgumentAt(2));
		
		HashMap<String,Portal> allPortals = new HashMap<String,Portal>();
		
		allPortals.putAll(portals1);
		allPortals.putAll(portals2);
		allPortals.putAll(portals3);

        System.err.println("== Reading links ==");
		ArrayList<Link> links = pf.getPurgedLinks(allPortals.values());

        System.err.println("== Getting Blocks ==");
        BlockList bl = getLinkBlockersSingle(allPortals.values().toArray(), links);
        
        // remove proposed links that exceed our maximum blockers
        
        HashSet<String> purgeList = new HashSet<String>();
        
        System.out.println("Keys: " + bl.size());
        
        Iterator it = bl.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (((teamCount)pair.getValue()).moreThan(maxBl)) {
                //System.out.println(pair.getKey() + " = " + pair.getValue());
                purgeList.add((String)pair.getKey());
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        it = purgeList.iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            bl.remove(key);
        }
        
        System.out.println("Keys: " + bl.size());

        
        String[] portalKeys1 = portals1.keySet().toArray(new String[portals1.size()]);
		String[] portalKeys2 = portals2.keySet().toArray(new String[portals2.size()]);
		String[] portalKeys3 = portals3.keySet().toArray(new String[portals3.size()]);

        boolean first = true;
		
	ArrayList<Field> fiList = new ArrayList<Field>();
		
        for (int li = 0; li < portals1.size(); li++)
        {
         
            Portal pki = portals1.get(portalKeys1[li]);

            for (int lj = 0; lj < portals2.size(); lj++)
            {
                Portal pkj = portals2.get(portalKeys2[lj]);

                if (bl.containsKey(pki,pkj)) {
                
                
                    for (int lk = 0; lk < portals3.size(); lk++)
                    {
                   
                        Portal pkk = portals3.get(portalKeys3[lk]);

                        
                        if (bl.containsKey(pkk,pkj) &&
                            bl.containsKey(pkk,pki)) {

                            // found a winner.
                            // remove fields that already exist.
                            
                            if (!(bl.getExists(pki,pkj) && bl.getExists(pkk,pkj) && bl.getExists(pkk,pki))) {
                            
                            
                                Field fi = new Field (pki,pkj,pkk);
								
								fiList.add(fi);
                            }
                            
                        }
                    }
                }
                
            }
        }
		
		System.out.println("Total Fields: " + fiList.size());

			// sort through colliding fields.
		
	        if (ag.hasOption("T"))
			iterSearchFields(dt , fiList.toArray(),true);
		else
			iterSearchFields(dt , fiList.toArray(),false);
		//searchFields(new ArrayList<Field>() , fiList.toArray(),0,0.0,0,dt);
		
      //  System.out.println("]");

        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
    
}

}

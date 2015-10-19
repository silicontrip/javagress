import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class megaplan {


	private static String drawFields(List<Field> fa, DrawTools dt)
	{
	

		dt.erase();
		
		for (Field fi: fa)
			dt.addFieldAsLines(fi);
		
		return dt.out();
	}
	
	private static boolean newFieldIntersect (List<Field> fa,Field f)
	{
		for (Field fi: fa) {
			if (fi.intersects(f)) { return true; }
			if (fi.equals(f)) { return true; }
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

	
	private static Double sizeFields(List<Field> fa)
	{
		Double area = 0.0;
		
		for (Field fi: fa)
		{
			area += fi.getGeoArea();
		}
		return area;
	}

	
	private static Double searchFields (ArrayList<Field> list, Object[] fields, int start, Double maxArea,int depth,DrawTools dt)
	{
			if (list.size() > 0) {
				
			//	Double thisArea = sizeFields(list);
				// we want to maximise number of fields
				Double thisArea = new Double(list.size());
				Double maxAreaSmall = Math.floor(maxArea);
				
				if (thisArea >= maxAreaSmall) {
					int retval = Double.compare(thisArea, maxAreaSmall);
					if (retval == 0) { maxArea += 0.01; } else { maxArea = thisArea; }
					System.out.println( String.format("%.2f",maxArea) + " : " + drawFields(list,dt));
					System.out.println("");
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
					
					
					maxArea = searchFields(newlist,fields,i+1,maxArea,depth+1,dt);
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
                
                Line l =  new Line (pi.getPoint(), pj.getPoint());
                
                teamCount bb = new teamCount();
                blocksPerLink.put(pi,pj,bb);
                for (Link link: links) {
                    if (l.intersects(link.getLine())) {
                      //  System.out.println("< " + pi  + ":" + pj +  " link: " + blocksPerLink.get(pi,pj));
                        blocksPerLink.incTeamEnum(pi,pj,link.getTeamEnum());
                    } else if (l.equalLine(link.getLine())) {
                        blocksPerLink.setExists(pi,pj,true);
                    }
                }
            }
        }
        return blocksPerLink;
    }


public static void main(String[] args) {

    
	Arguments ag = new Arguments(args);

	teamCount maxBl = new teamCount(ag.getOptionForKey("E"),ag.getOptionForKey("R"));
	
	DrawTools dt = new DrawTools();	
	if (ag.hasOption("C"))
		dt.setDefaultColour(ag.getOptionForKey("C"));
	else
		dt.setDefaultColour("#a24ac3");
	
	
	// ugly hack to modify args array.s
/*
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
*/	
    try {
        PortalFactory pf = PortalFactory.getInstance();
        
        System.err.println("== Reading portals ==");
        
		HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
		HashMap<String,Portal> portals2 = new HashMap<String,Portal>();
		HashMap<String,Portal> portals3 = new HashMap<String,Portal>();
		
		
		portals1 = pf.portalClusterFromString(ag.getArguments().get(0));
		portals2 = pf.portalClusterFromString(ag.getArguments().get(1));
		portals3 = pf.portalClusterFromString(ag.getArguments().get(2));
		
		HashMap<String,Portal> allPortals = new HashMap<String,Portal>();
		
		allPortals.putAll(portals1);
		allPortals.putAll(portals2);
		allPortals.putAll(portals3);

		ArrayList<Link> links = pf.getPurgedLinks(allPortals.values());

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
                            
                            
                                Field fi = new Field (pki.getPoint(),pkj.getPoint(),pkk.getPoint());
								
								fiList.add(fi);
                            }
                            
                        }
                    }
                }
                
            }
        }
		
		System.out.println("Total Fields: " + fiList.size());

			// sort through colliding fields.
		
		searchFields(new ArrayList<Field>() , fiList.toArray(),0,0.0,0);
		
      //  System.out.println("]");

        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
    
}

}

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class maxfields {

	private static String drawFields(List<Field> fa,DrawTools dt)
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

	private static int iterSearchFields (DrawTools dt, Object[] fields)
	{

		int mostFields = 0;

		ArrayList<Integer> sStack = new ArrayList<Integer>();
		ArrayList<ArrayList<Field>> fStack = new ArrayList<ArrayList<Field>>();

		while (sStack.size() > 0) {

			System.out.println ("size: " + sStack.size());

			ArrayList<Field> list = fStack.remove(fStack.size()-1);
			Integer start = sStack.remove(sStack.size()-1);
			if (list.size() > 0) {
				int thisSize = list.size();
				if (thisSize > mostFields)
				{
					mostFields = thisSize;
					System.out.println(thisSize + " : " + drawFields(list,dt));
					System.out.println("");
				}
			}

			for (int i =start; i<fields.length; i++)
			{
				Field thisField = (Field)fields[i];
				if (!newFieldIntersect(list,thisField))
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
	
	private static Double searchFields (DrawTools dt, ArrayList<Field> list, Object[] fields, int start, Double maxArea,int depth)
	{
			if (list.size() > 0) {
				
			//	Double thisArea = sizeFields(list);
				// we want to maximise number of fields
				Double thisArea = new Double(list.size());
				
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
					
					
					maxArea = searchFields(dt,newlist,fields,i+1,maxArea,depth+1);
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
	
	if (ag.hasOption("L"))
		dt.setFieldsAsPolyline();
	else
		dt.setFieldsAsPolygon();

	try {
        PortalFactory pf = PortalFactory.getInstance();
		
        System.err.println("== Reading portals ==");
        
        HashMap<String,Portal> portals;
        
        portals = pf.portalClusterFromString(ag.getArgumentAt(0));
        ArrayList<Link> links = pf.getPurgedLinks(portals.values());

        BlockList bl = getLinkBlockersSingle(portals.values().toArray(), links);
        
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

        
        String[] portalKeys = portals.keySet().toArray(new String[portals.size()]);
        boolean first = true;
     //   System.out.println("[");

		
		ArrayList<Field> fiList = new ArrayList<Field>();
		
        for (int li = 0; li < portals.size(); li++)
        {
         
            Portal pki = portals.get(portalKeys[li]);

            for (int lj = li+1; lj < portals.size(); lj++)
            {
                Portal pkj = portals.get(portalKeys[lj]);

                if (bl.containsKey(pki,pkj)) {
                
                
                    for (int lk = lj+1; lk < portals.size(); lk++)
                    {
                   
                        Portal pkk = portals.get(portalKeys[lk]);

                        
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
		
		// iterSearchFields(dt,  fiList.toArray());
		searchFields(dt, new ArrayList<Field>() , fiList.toArray(),0,0.0,0);
		
      //  System.out.println("]");

        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
    
}

}

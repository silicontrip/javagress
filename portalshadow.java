
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;

public class portalshadow {

        private static final Double nanoPerSec = 1000000000.0;

	
	public static void main(String[] args) {
		
		long startTime;
		double elapsedTime;
		long runTime;
		double totalTime;
		long endTime;
		
        Arguments ag = new Arguments(args);

        DrawTools dt = new DrawTools(); 
        if (ag.hasOption("C"))
                dt.setDefaultColour(ag.getOptionForKey("C"));
        else
                dt.setDefaultColour("#a24ac3");


	try {
		PortalFactory pf = PortalFactory.getInstance();
			
		System.err.println("== Reading portals ==");
			
		startTime = System.nanoTime();
		runTime = startTime;
			
		// System.err.println("== " + args.length + " ==");
			
			
		List<Portal> allPortals = new ArrayList<Portal>();
			
			
		if (ag.getArguments().size() >= 1) {
				
				
			HashMap<String,Portal> portals;
			HashMap<String,Link> links ;
				
			portals = pf.portalClusterFromString(ag.getArgumentAt(0));
			links = pf.getLinks();
				
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			System.err.println("==  portals read " + elapsedTime+ " ==");

			Iterator it = portals.entrySet().iterator();
			// get all links
			// loop {
			// find closest link from portal
			// add link to shadow array.
			// perform obscure test on remaining links.
			// loop {
			// if not obscured by shadow array add link to new array.
			// } end loop
			// } end loop 
			ArrayList<Link> shadowList = new ArrayList<Link>();
			if (it.hasNext()) { 
				Map.Entry pair = (Map.Entry)it.next();
				Portal pt = (Portal)pair.getValue();
				System.out.println(pair.getKey() + ":" + pair.getValue() + ((Portal)pair.getValue()).getUrl());

				HashMap<String,Link> remLinks = links ;
				dt.addMarker(pt.getLat(), pt.getLng());
				it.remove(); // avoids a ConcurrentModificationException
					// why ?
				Iterator lit = links.entrySet().iterator();

				while (remLinks.size() >0) 
				{
					double minDist = 6882.0; // approx max link distance
					Link closeLink = null;
					while (lit.hasNext())
					{
						Map.Entry lpair = (Map.Entry)lit.next();
						Link li = (Link)lpair.getValue();
						if (!li.hasPoint(pt)) {
							if (li.getGeoDistance(pt) < minDist)
							{
								minDist = li.getGeoDistance(pt);
								closeLink = li;
							}
						}
						//System.out.println("" +  li.getGeoDistance(pt) + " : [" + li + "] "  + pt.getBearingTo(li.getO()) + " - " + pt.getBearingTo(li.getD() ));
						//lit.remove();
					}
	/*
					System.out.println("" +  closeLink.getGeoDistance(pt) + " : [" + closeLink + "] "  + pt.getBearingTo(closeLink.getO()) + " - " + pt.getBearingTo(closeLink.getD()));

					for (Link li: shadowList)
					{
						System.out.println("[" + closeLink + "," + li + "]");
						closeLink.shadow(pt,li);
					}
	*/

					shadowList.add(closeLink);

					lit = remLinks.entrySet().iterator();
					remLinks = new HashMap<String,Link>();
					while (lit.hasNext())
					{
						Map.Entry lpair = (Map.Entry)lit.next();
                        			Link li = (Link)lpair.getValue();
						int obs = 0;
						for (Link sli: shadowList) {
							if (sli.equals(li))
								obs = 7;
							if (li.hasPoint(pt))
								obs = 7;
							obs = obs | sli.obscuredFromBy(pt,li);
						}
						if (!(obs==3 || obs==5 || obs==6 || obs==7))
						{
							remLinks.put(li.getGuid(),li);
						}
					}
					lit = remLinks.entrySet().iterator();

				}
				// so now we have a list of links that the portal can get to at least part of
				ArrayList<Line> resultShadowList = new ArrayList<Line>();
				for (Link l1 : shadowList)
				{
					ArrayList<Line> sl= new ArrayList<Line>();
					sl.add(l1);
					for (Link l2: shadowList)
					{
						ArrayList<Line> nsl = new ArrayList<Line>();
						for (Line l3: sl)
						{
							nsl.addAll( l2.shadow(pt,l3));
						}
						sl = nsl;
					}
					resultShadowList.addAll(sl);
				}	

				dt.setDefaultColour("#f0f0f0");
				for (Line li: resultShadowList)
					dt.addField(new Field(pt,li.getO(),li.getD()));	
			}

			System.out.println(dt.out());
				
		} else {
			throw new RuntimeException("Invalid command line arguments");
		}
			
			
			
	} catch (Exception e) {
			
		System.out.print ("Exception: ");
		System.out.println(e.getMessage());
		e.printStackTrace();
	}
	
	}
	
}

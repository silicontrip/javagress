
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;

public class portalquery {

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
				
				
			HashMap<String,Portal> portals = new HashMap<String,Portal>();
				
			portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime)/nanoPerSec;
			System.err.println("==  portals read " + elapsedTime+ " ==");

			Iterator it = portals.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				Portal pt = (Portal)pair.getValue();
				System.out.println(pair.getKey() + ":" + pair.getValue() + ((Portal)pair.getValue()).getUrl());

				dt.addMarker(pt.getLat(), (pt.getLng()));
				it.remove(); // avoids a ConcurrentModificationException
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

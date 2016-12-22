
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;

public class guardlink {

        private static final Double nanoPerSec = 1000000000.0;

	
	public static void main(String[] args) {
		
		RunTimer rt = new RunTimer();
		Arguments ag = new Arguments(args);
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
			
			rt.start();
			
			List<Portal> allPortals = new ArrayList<Portal>();
			
			
			if (ag.getArguments().size() >= 1) {
				
				
				HashMap<String,Portal> portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				Collection<Portal> cp1 = pf.portalClusterFromString(ag.getArgumentAt(1)).values();
				Collection<Portal> cp2 = pf.portalClusterFromString(ag.getArgumentAt(2)).values();
				
				System.err.println("==  portals read " + rt.split() + " ==");

				Portal p1=null;
				Portal p2=null;	

				for (Portal l1: cp1) { p1 = l1; }
				for (Portal l2: cp2) { p2 = l2; }

				Line guard = new Line(p1,p2);

				System.out.println("Link: " + guard);

				
				for (Portal l1: portals.values())
					for (Portal l2: portals.values())
					{
						Line test = new Line(l1,l2);
						double dist1 = Math.abs(guard.getGeoDistance(l1));
						double dist2 = Math.abs(guard.getGeoDistance(l2));
						if (dist1 < 0.1 && dist2 < 0.1)
							if (!guard.intersects(test)) 	
								System.out.println("" + l1.getGeoDistance(l2) + " " + guard.getGeoDistance(l1) + " " + guard.getGeoDistance(l2) + " " + test);
						
					}
				

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


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
			dt.setOutputAsPolyline();
		if (ag.hasOption("O"))
			dt.setOutputAsIntel();

		try {
			PortalFactory pf = PortalFactory.getInstance();
			
			System.err.println("== Reading portals ==");
			
			rt.start();
			
			List<Portal> allPortals = new ArrayList<Portal>();
		
			
			if (ag.getArguments().size() >= 1) {
				
				DrawTools plan = new DrawTools(ag.getArgumentAt(0));
				
				HashMap<String,Portal> portals = pf.portalClusterFromString(ag.getArgumentAt(1));
				
				System.err.println("==  portals read " + rt.split() + " ==");


				
				for (Portal l1: portals.values())
					for (Portal l2: portals.values())
					{
						double min1dist = 20037;
						double min2dist = 20037;
						boolean intersects = false;
						
						

						Line test = new Line(l1,l2);
						for (PolyObject po: plan.getAsLines()) {
							Line guard = ((Polyline)po).asLine();
							double dist1 = Math.abs(guard.getGeoDistance(l1));
							double dist2 = Math.abs(guard.getGeoDistance(l2));
							
							if (guard.intersects(test))
							{
								intersects = true;
								break;
							}
							
							if (dist1 < min1dist && dist2 < min2dist)
							{
								min1dist = dist1;
								min2dist = dist2;
							}
						}
						if (!intersects && min1dist < 0.1 && min1dist > 0 && min2dist < 0.1 && min2dist > 0  && test.getGeoDistance() > 0.1)
							dt.addLine(test);
						//	System.out.println("" + min1dist + " " + min2dist + " " + test.getGeoDistance() + " " + test);
					}
				
				System.out.println(dt);

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

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import java.util.List;

import java.util.Arrays;
import javax.xml.parsers.*;
import java.io.*;


public class spiner {
	

	public static <K, V> void printMap(Map<K, V> map) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
		} 
	} 

	public static void main(String[] args) {
		
		long startTime;
		double elapsedTime;
		long runTime;
		double totalTime;
		long endTime;
		int calc=0;
		Double threshold;
		Point target=null;
		RunTimer rt;
		
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

		// mu calculation
		if (ag.hasOption("M"))
			calc=1;
		
		
		if (ag.hasOption("t"))
			threshold = new Double(ag.getOptionForKey("t"));
		else
			threshold = new Double(0.3);
		
		if (ag.hasOption("T"))
			target = new Point(ag.getOptionForKey("T"));

		try {
			PortalFactory pf = PortalFactory.getInstance();
			
			rt = new RunTimer();
			System.err.println("== Reading portals ==");
			rt.start();

			List<Portal> allPortals = new ArrayList<Portal>();
			
			ArrayList<Link> links;
			
			if (ag.getArguments().size() == 1) {
				HashMap<String,Portal> portals = new HashMap<String,Portal>();
				portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				System.err.println("==  portals read " + rt.split()+ " ==");

				// combinate through portals. (most first)
			

				ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
				System.err.println("== potential links generated " + li.size() + " " + rt.split()+ "s ==");

				// determine bearing for each link.

				
			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
		// should handle known exceptions more gracefully.	
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

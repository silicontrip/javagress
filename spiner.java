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
	
	ArrayList<Double> bestAng;
	ArrayList<ArrayList<Portal>> bestList;

	public static <K, V> void printMap(Map<K, V> map) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
		} 
	} 
	public static void printAngle(ArrayList<Line> la) 
	{
		
		Object[] lineKeys = la.toArray();

                for (int i =0; i<lineKeys.length; i++)
                {
                        Line pki = (Line)lineKeys[i];

			
			System.out.println("" + pki + " : ["  + pki.getBearing() + ", " + pki.getReverseBearing() + "]");
		}

	}

	public static double getMaxAngle(ArrayList<Line> la) 
	{

	
		double max = 0 ;
		Object[] lineKeys = la.toArray();
		DrawTools dt = new DrawTools();

		if (lineKeys.length < 2)
			return 0; // or some other form of undefined sentinel.

                for (int i =0; i<lineKeys.length; i++)
                {
                        Line pki = (Line)lineKeys[i];

                        for (int j=i+1; j<lineKeys.length; j++)
                        {
                                Line pkj = (Line)lineKeys[j];
				double b1 = pki.getBearing();
				double b2 = pkj.getBearing();

				if (b2 > 90) { b2 -= 180; }
				if (b2 < -90) { b2 += 180; }
				if (b1 > 90) { b1 -= 180; }
				if (b1 < -90) { b1 += 180; }


				// determine angle difference between lines.
				double diff = b1 - b2;
				//if ( diff > 180) { diff -= 180; }
				//if ( diff < -180) { diff += 180; }
				//if ( diff > 90) { diff -= 180; }
				//if ( diff < -90) { diff += 180; }
				if ( diff < 0 )  { diff = - diff; }
				if ( diff > 90) { diff = 180 - diff; }

				if (diff > max) 
				{
					dt.erase();
					dt.addLine(pkj);
					dt.addLine(pki);
					System.out.println("" + b1 + " - " + b2 + " = " + diff);	
					System.out.println ("" + diff  + " " + dt.out());
					max = diff;
				}
			
			}
		}	
		return max;
	}

	public static void main(String[] args) {
		
		RunTimer rt = new RunTimer();
		Arguments ag = new Arguments(args);

		//System.out.println ("Arguments: " + ag );

		teamCount maxBl = new teamCount(ag.getOptionForKey("E"),ag.getOptionForKey("R"));
        
		DrawTools dt = new DrawTools(); 
		if (ag.hasOption("C"))
			dt.setDefaultColour(ag.getOptionForKey("C"));
		else
			dt.setDefaultColour("#a24ac3");

		try {
			PortalFactory pf = PortalFactory.getInstance();
			
			System.err.println("== Reading portals ==");
			rt.start();

			List<Portal> allPortals = new ArrayList<Portal>();
			
			ArrayList<Link> links;
			
			if (ag.getArguments().size() == 1) {
				HashMap<String,Portal> portals = new HashMap<String,Portal>();
				portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				System.err.println("==  portals read " + rt.split()+ " ==");

				// combinate through portals. (most first)
				// for combinations
				// gen links
				// determine max angle
				// if maxAngle < bestAngle[portals.size] 
				//  bestAngle[portals.size] = maxAngle
				//  bestList = portals;
			

				ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
				System.err.println("== potential links generated " + li.size() + " " + rt.split()+ "s ==");
				//printAngle(li);
				double ma = getMaxAngle(li);
				System.out.println("Max: " + ma);

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

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
			System.out.println("hash: " + entry.getKey().hashCode() + " Key : " + entry.getKey() + " Value : " + entry.getValue());
		} 
	}
	public static void printList (double bestAng, ArrayList<Portal> bestList) throws IOException
	{
		
		DrawTools dt = new DrawTools();
		
		ArrayList<Line> la = PortalFactory.getInstance().makeLinksFromSingleCluster(bestList);
		
		for (Line li: la) {
			dt.addLine(li);
		}
		System.out.println ("" + bestAng + " ("+ bestList.size() + ") : " + dt.out());
		System.out.println("");

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
	
	public static void searchSpine(Object[] pt, HashMap<Line,Double>  allBearing, int start, ArrayList<Portal> current,ArrayList<Double> bestAng, ArrayList<ArrayList<Portal>> bestList) throws IOException
	{
		int cls =current.size();
		
		
		if (cls > 0 ) {
			
			ArrayList<Line> cline = PortalFactory.getInstance().makeLinksFromSingleCluster(current);
		
			double mm = getMaxAngle(cline,allBearing);

			if (mm < bestAng.get(cls) )
			{
				
				System.out.println("size: " + cls + " / " + mm + " / " + start);

				
				bestAng.set(cls,mm);
				
				bestList.set(cls,new ArrayList<Portal>(current));
				
				// printList(mm,current);
				
			}
		}
		for (int i =start; i<pt.length; i++)
		{
			Portal thisPortal = (Portal)pt[i];
			ArrayList<Portal> newList = new ArrayList<Portal>(current);
			newList.add(thisPortal);
			
			searchSpine(pt, allBearing, i+1,  newList,bestAng,bestList);
		}
		
		
	}

	public static double getMaxAngle(ArrayList<Line> la,HashMap<Line,Double>  allBearing)
	{

	
		double max = 0 ;
		Object[] lineKeys = la.toArray();
		DrawTools dt = new DrawTools();
		double[] dbearing = new double[lineKeys.length];
		
		if (lineKeys.length < 2)
			return 0; // or some other form of undefined sentinel.

		// System.out.println("all bearing size: " + allBearing.size());
		
		// printMap(allBearing);
		
		
		for (int j=0; j<lineKeys.length; j++)
		{
			
			Line l1 = (Line)lineKeys[j];
			
			dbearing[j] = allBearing.get(l1);;

		}
		
		for (int i =0; i<lineKeys.length; i++)
		{
			// Line pki = (Line)lineKeys[i];

			for (int j=i+1; j<lineKeys.length; j++)
			{
				// Line pkj = (Line)lineKeys[j];
				// double b1 = pki.getBearing();
				// double b2 = pkj.getBearing();

				double b1 = dbearing[i];
				double b2 = dbearing[j];
				
				if (b2 > 90) { b2 -= 180; }
				if (b2 < -90) { b2 += 180; }
				if (b1 > 90) { b1 -= 180; }
				if (b1 < -90) { b1 += 180; }


				// determine angle difference between lines.
				double diff = b1 - b2;

				if ( diff < 0 )  { diff = - diff; }
				if ( diff > 90) { diff = 180 - diff; }

				if (diff > max) 
				{
					max = diff;
				}
			
			}
		}	
		return max;
	}

	public static void main(String[] args) {
		
		RunTimer rt = new RunTimer();
		Arguments ag = new Arguments(args);

		ArrayList<Double> bestAng = new ArrayList<Double>();
		ArrayList<ArrayList<Portal>> bestList = new ArrayList<ArrayList<Portal>>();

		
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
				System.err.println("== " + portals.size()+ " portals read " + rt.split()+ " ==");

				ArrayList<Portal> allPortal = new ArrayList<Portal>(portals.values());
				
				// combinate through portals. (most first)
				// for combinations
				// gen links
				// determine max angle
				// if maxAngle < bestAngle[portals.size] 
				//  bestAngle[portals.size] = maxAngle
				//  bestList = portals;
			
				for (int i = 0; i <= portals.size(); i++)
				{
					bestAng.add(new Double(360.0));
					bestList.add(new ArrayList<Portal>());
				}
				
				HashMap<Line,Double>  allBearing = new HashMap<Line,Double>();

				ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
				System.err.println("== potential links generated " + li.size() + " " + rt.split()+ "s ==");
				for (Line l: li)
				{
				for (Portal p: allPortal)
				{
					System.out.print("Link: " + l);
					System.out.println(" Portal: "  + p);
					double sign = l.sign(p);
					System.out.println("sign :" + sign);

				}
				}
				
				for (int i = 0; i < portals.size()-1; i++) {
					Portal pki = allPortal.get(i);

					for (int j = i+1; j < portals.size(); j++) {
						Portal pkj = allPortal.get(j);

						Line l1 = new Line(pki,pkj);
						allBearing.put(l1,new Double(l1.getBearing()));
						
						/*
						Line l2 = new Line(pki,pkj);

						System.out.println("ref: " + allBearing.get(l1) + " get: " + allBearing.get(l2) + " eq: " + l1.equals(l2) + " hasheq: "  + (l1.hashCode()==l2.hashCode()));
						*/
						/*  for some reason the reverse hashCode is the same
						System.out.println("" + l2.hashCode() + " : " + l2.getBearing());

						allBearing.put(l2,l2.getBearing());
						*/
					}
					
				}
				
				System.out.println("all bearings: " + allBearing.size());
				// printMap(allBearing);
				/*
				for (Line pki: li)
				{
					System.out.println("" + pki + "(" + pki.hashCode()  + ") : ["  + pki.getBearing() + "]");
				}
				*/
				
				searchSpine(allPortal.toArray(),allBearing,0,new ArrayList<Portal>(), bestAng,  bestList);

				System.err.println("== search complete " + rt.split()+ " ==");

				
				// printAngle(li);
				// double ma = getMaxAngle(li);
				// System.out.println("Max: " + ma);

				// determine bearing for each link.

				int sz = bestAng.size();
				for (int i = 0; i< sz; i  ++ )
				{
					
					printList(bestAng.get(i),bestList.get(i));

				}
				
				System.err.println("== done " + rt.stop()+ " ==");

				
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

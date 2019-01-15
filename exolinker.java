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


public class exolinker {

	public static <K, V> void printMap(Map<K, V> map) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
		} 
	} 

	private static ArrayList<Line> generateSpine(ArrayList<Field> fa)
	{
		int cs = fa.size();
		ArrayList<Field> faOrdered = new ArrayList<Field>(cs);
		ArrayList<Line> la = new ArrayList<Line>();
		Line shared = fa.get(0).getSharedLine(fa.get(1));
		for (int i=0; i < cs; i++)
			faOrdered.add(i,null);
		//{
			for (Field f1: fa)
			{
				int count = 0;
				for (Field f2: fa)
					if (f2.inside(f1))
						count++;
			//	System.out.println("Count: " + count);
				faOrdered.set(count,f1);
			}
			
		//}	
		Point oldPoint = null;
		for (Field f1: faOrdered)
		{
			Point p = f1.getOtherPoint(shared);
			if (oldPoint!=null)
				la.add(new Line(oldPoint,p));
			oldPoint=p;
		}
			
		return la;

	}

	private static Double exoSpineSearch (Point po, ArrayList<Field> pf, Line shared, ArrayList<Field> spine,Double max,int limit)
	{

		// System.out.println(">>> exoSpineSearch -> " + spine.size());
		// find another portal that works with this spine
		ArrayList<ArrayList<Field>> solution = new ArrayList<ArrayList<Field>>();

		ArrayList<Line> spineLinks = generateSpine(spine);

		ArrayList<Field> fanFields = new ArrayList<Field>();

		for (Line l: spineLinks)
			fanFields.add(new Field(po,l.getO(), l.getD()));

		DrawTools dt = new DrawTools();

		


//		System.out.println("chosen spine: " + dt);


	// generate a spine plan...
		ArrayList<Point> spinePoints = new ArrayList<Point>();
		for (Field f: spine)
			spinePoints.add(f.getOtherPoint(shared));

		ArrayList<Point> anchors = new ArrayList<Point>();
		if (po.equals(shared.getD()))
			anchors.add(shared.getO());
		else 
			anchors.add(shared.getD());

		// go through all fields
		Point anchor=null;
		for (Field fi: pf)
		{
			// find a field which isn't part of this spine.
			// field does not have shared
			if (!fi.hasLine(shared)) {
				boolean ok=false;
				for (Point pfi: spinePoints)
				{
					Line fanLine = new Line(po,pfi);
					// fields are not the same
					// and they share a link
					anchor = fi.getOtherPoint(fanLine);
					boolean used = false;
					for (Point p: anchors)
						if (p.equals(anchor))
						{
							used=true;
							break;
						}
					// don't use a spine portal (not sure why it's picking them up)
					for (Point p: spinePoints)
						if (p.equals(anchor))
						{
							used=true;
							break;
						}
					if ( fi.hasLine(fanLine) && fi.layers(spine) && !fi.intersectsLine(spineLinks) && !used )
					{
						//dt.erase(); dt.addField(fi); System.out.println("Field matches spine: " + dt);
						ok=true;
						break;
					}
				}
				if (ok) {
						
					// dt.erase(); dt.addField(fi); System.out.println("Found field: " + dt);

					// this field works with one of the spine fields
					// does the target portal work with all fields?
					boolean allspine = true;
					ArrayList<Field> newspine = new ArrayList<Field>();
					for (Field spf: spine) 
					{
						boolean fieldok = false;
						for (Field ifi: pf)
							if (ifi.hasPoint(anchor) && !ifi.equals(fi) && ifi.sharesLine(spf) && !ifi.intersects(newspine) && !ifi.isContainedIn(newspine) && !ifi.intersects(fanFields) )
							{
					
/*
								dt.erase(); 
								dt.setDefaultColour("#7f3f1f");
								for(Field f: spine)
									dt.addField(f);
								dt.setDefaultColour("#c0c01f");
								dt.addField(fi);
								dt.setDefaultColour("#c01fc0");
							// dt.addField(ifi); System.out.println("matching field: " + dt);
*/
								newspine.add(ifi);
								fieldok = true;
								break;
							}
						if (!fieldok)
						{
							allspine=false;	
							break;
						}
					}
					if (allspine)
					{
						solution.add(newspine);
						anchors.add(anchor);
					}
				}
			}
		}
		// don't want to overdose on rethrows.
		Double ratio = 1.0;
		if (anchors.size() < limit) {
			Double score = 1.0*(spine.size()-1) + (spine.size()*2-1) * anchors.size();
			if (score > max)
			{


				max = score;
				dt.erase();
				dt.setDefaultColour("#c040c0");
				for(Field f: fanFields)
					dt.addField(f);
		
				dt.setDefaultColour("#c0c000");
				for (Point anc: anchors)
					dt.addMarker(anc);

				dt.setDefaultColour("#c040c0");
				//dt.setDefaultColour("#c0c040");
				for(Line l: spineLinks)
					dt.addLine(l);

				System.out.print("" + max+ " " + ((spine.size()*2-1) * anchors.size()) +  " " + spine.size() + "x" + anchors.size() + " ");
				System.out.println(dt);

				System.out.println("");
			}
		}
		return max;
	}

	private static Double exoSearch (Point po, ArrayList<Field> fa,Double max,int limit)
	{

		DrawTools dt = new DrawTools();	

		//System.out.println(">>> exoSearch");
		ArrayList<Field> portalFields = findFieldsWithPortal(fa,po);

		// iterate through fields with matching links.
		for (int i=0; i< portalFields.size(); i++)
		{
			Field f1 = portalFields.get(i);
			
			for (int j=i+1; j< portalFields.size(); j++)
			{
				Field f2 = portalFields.get(j);
				// do fields share a common link?
				if (f2.sharesLine(f1) && !f2.intersects(f1) && f2.layers(f1))
				{
					ArrayList<Field> spine = new ArrayList<Field>();
					spine.add(f1);
					spine.add(f2);
					// 	
					Line shared = f1.getSharedLine(f2);
					for (int k=j+1; k < portalFields.size(); k++)
					{
						Field f3 = portalFields.get(k);
						if (f3.hasLine(shared) && !f3.intersects(spine) && f3.layers(spine) )
						{
							spine.add(f3);
							
							// find the most portals that can use this spine	
							max= exoSpineSearch(po,portalFields,shared,spine,max,limit);
						}

					}
				}
			}
		}
		return max;
	}


	private static ArrayList<Field> findFieldsWithPortal(ArrayList<Field> fa, Point po)
	{
		ArrayList<Field> nl = new ArrayList<Field>();
		//System.out.println(">>> findFieldsWithPortal: " + po);
		//System.out.println("" + po.getLat() + ", " + po.getLng());
		for (Field fi: fa)
		{
			if (fi.hasPoint(po))
			{
				//System.out.println("found with point: " + fi);
				nl.add(fi);
			}
		}
		return nl;
	}

	private static ArrayList<Field> findFieldsWithLink(ArrayList<Field> fa, Line li)
	{
		ArrayList<Field> nl = new ArrayList<Field>();
		for (Field fi: fa)
			if (fi.hasLine(li))
				nl.add(fi);
		return nl;
	}
	
	public static void main(String[] args) {
		
		int calc=0;
		Double threshold;
		Double percentile = null;
		Double fpercentile = null;
		ArrayList<Point> target=null;
		
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
			dt.setOutputAsPolyline();
		if (ag.hasOption("O"))
			dt.setOutputAsIntel();

		// mu calculation
		if (ag.hasOption("M"))
			calc=1;
		
	Integer limit ;
		if (ag.hasOption("r"))
			limit = new Integer (ag.getOptionForKey("r"));
		else
			limit = new Integer(16);
		
		if (ag.hasOption("t"))
			threshold = new Double(ag.getOptionForKey("t"));
		else
			threshold = new Double(0.3);

		if (ag.hasOption("p"))
			percentile = new Double(ag.getOptionForKey("p"));
		if (ag.hasOption("f"))
			fpercentile = new Double(ag.getOptionForKey("f"));


		if (ag.hasOption("h"))
		{
			System.out.println("Options");
			System.out.println(" -E <number>       Limit number of Enlightened Blockers");
			System.out.println(" -R <number>       Limit number of Resistance Blockers");
			System.out.println(" -C <#colour>      Set Drawtools output colour");
			System.out.println(" -L                Set Drawtools to output as polylines");
			System.out.println(" -O                Output as Intel Link");
			System.out.println(" -M                Use MU calculation");
			System.out.println(" -t <number>       Threshold for similar fields (larger less similar)");
			System.out.println(" -p <percentile>   Use longest percentile links");
			System.out.println(" -f <percentile>   Use largest percentile fields");
			System.out.println(" -T <lat,lng,...>  Use only fields  covering target points");
			System.out.println(" -r <number>       Limit the number of rethrows");
		}

			
		
		try {
			PortalFactory pf = PortalFactory.getInstance();

			if (ag.hasOption("T"))
				target = pf.getPointsFromString(ag.getOptionForKey("T"));

			
			rt = new RunTimer();
			System.err.println("== Reading links and portals ==");
			rt.start();
			
			
			// System.err.println("== " + args.length + " ==");
			
			// HashMap<String,Link> allLinks = pf.getLinks();
			
			List<Portal> allPortals = new ArrayList<Portal>();
			
			ArrayList<String> areaOut;
			ArrayList<Link> links;
			
		// ag.getArgumentAt
			ArrayList<Field> allfields;
			if (ag.getArguments().size() == 1) {
				
			// get all 3 points from one cluster 
				
				HashMap<String,Portal> portals = new HashMap<String,Portal>();
				
				portals = pf.portalClusterFromString(ag.getArgumentAt(0));
				allPortals = new ArrayList<Portal>();
				allPortals.addAll(portals.values());
				
				System.err.println("== " + portals.size() + " portals read " + rt.split()+ " ==");
				if (ag.hasOption("P")) {
					portals = pf.reducePortals(portals,new Double(ag.getOptionForKey("P")));
					System.err.println("== " + portals.size() + " portals reduced " + rt.split()+ " ==");
				}

				System.err.println("== getting links ==");
				
				links = pf.getPurgedLinks(portals.values());
				
				System.err.println("== "+links.size()+" links read " + rt.split()+ " ==");
				
				
				System.err.println("== generating potential links ==");

				ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
				System.err.println("all links: " + li.size());
				li = pf.filterLinks(li,links,maxBl);
				if (percentile != null)
					li = pf.percentileLinks(li,percentile);
				
				System.err.println("purged links: " + li.size());

				System.err.println("==  links generated " + rt.split()+ " ==");
				System.err.println("== Generating fields ==");
				
				ArrayList<Field> af = pf.makeFieldsFromSingleLinks(li);
				allfields = pf.filterFields(af,links,maxBl);
				System.err.println("fields: " + allfields.size());

				// allfields = singleCluster(portals);
				
			} else if (ag.getArguments().size() == 2) { 
				// one point from one cluster
				// and the other two points from the other cluster
				
				HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals2 = new HashMap<String,Portal>();

				
				portals1 = pf.portalClusterFromString(ag.getArgumentAt(0));
				portals2 = pf.portalClusterFromString(ag.getArgumentAt(1));
				
				
				allPortals = new ArrayList<Portal>();
								
				allPortals.addAll(portals1.values());
				allPortals.addAll(portals2.values());
				
				
				System.err.println("== " + allPortals.size() + " portals read " + rt.split()+ " ==");
				System.err.println("== Reading links ==");
				
				
				
				links = pf.getPurgedLinks(new ArrayList<Portal>(allPortals));
				
				
				System.err.println("==  links read " +rt.split()  + " ==");
				System.err.println("== Generating fields ==");
				
				ArrayList<Line> li1 = pf.makeLinksFromSingleCluster(portals1.values());
				li1 = pf.filterLinks(li1,links,maxBl);
				System.err.println("== cluster 1 links:  " + li1.size() + " ==");

				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals1.values(),portals2.values());
				li2 = pf.filterLinks(li2,links,maxBl);
				System.err.println("== cluster 2 links:  " + li2.size() + " ==");

				ArrayList<Field> af = pf.makeFieldsFromDoubleLinks(li2,li1);
				allfields = pf.filterFields(af,links,maxBl);
				System.err.println("== Fields:  " + allfields.size() + " ==");


			} else if (ag.getArguments().size() == 3) { 
				
				// one point from each cluster
				
				HashMap<String,Portal> portals1 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals2 = new HashMap<String,Portal>();
				HashMap<String,Portal> portals3 = new HashMap<String,Portal>();

				
				portals1 = pf.portalClusterFromString(ag.getArgumentAt(0));
				portals2 = pf.portalClusterFromString(ag.getArgumentAt(1));
				portals3 = pf.portalClusterFromString(ag.getArgumentAt(2));

				allPortals = new ArrayList<Portal>();
				
				allPortals.addAll(portals1.values());
				allPortals.addAll(portals2.values());
				allPortals.addAll(portals3.values());

				System.err.println("== " + allPortals.size() + " portals read " + rt.split()+ " ==");
				System.err.println("== get links ==");
				
				
				links = pf.getPurgedLinks(new ArrayList<Portal>(allPortals));
				// create link blockers 1-2, 2-3 and 3-1 

				// HashMap<String,teamCount> bpl = new HashMap<String,teamCount>();
				
				
				System.err.println("==  links read " + rt.split()+ " ==");
				System.err.println("== Generating possible fields ==");
				
				ArrayList<Line> li1 = pf.makeLinksFromDoubleCluster(portals1.values(),portals2.values());
				
				System.err.println("== "+li1.size()+" links generated " + rt.split()+ " ==");

				
				li1 = pf.filterLinks(li1,links,maxBl);
				System.err.println("== "+li1.size()+" links filtered " + rt.split()+ " ==");
				
				ArrayList<Line> li2 = pf.makeLinksFromDoubleCluster(portals2.values(),portals3.values());
				System.err.println("== "+li2.size()+" links generated " + rt.split()+ " ==");

				li2 = pf.filterLinks(li2,links,maxBl);
				System.err.println("== "+li2.size()+" links filtered " + rt.split()+ " ==");

				ArrayList<Line> li3 = pf.makeLinksFromDoubleCluster(portals3.values(),portals1.values());
				System.err.println("== "+li3.size()+" links generated " + rt.split()+ " ==");

				li3 = pf.filterLinks(li3,links,maxBl);
				System.err.println("== "+li3.size()+" links filtered " + rt.split()+ " ==");

				ArrayList<Field> af = pf.makeFieldsFromTripleLinks(li1,li2,li3);
				allfields = pf.filterFields(af,links,maxBl);

			} else {
				throw new RuntimeException("Invalid command line arguments");
			}
			
			System.err.println("==  fields generated " + rt.split() + " ==");
			System.err.println("== purge fields ==");
			
			// start searching for fields.
			if (target!=null)
				allfields = pf.fieldsOverTarget(allfields,target);

			if (fpercentile != null)
				allfields = pf.percentileFields(allfields,fpercentile,false);

			System.err.println("==  fields filtered " + rt.split() + " ==");
			System.err.println("== searching fields ==");


			Double max = 1.0;
			for (Portal po: allPortals)
			{
				max = exoSearch (po,allfields,max,limit.intValue());
				//System.out.println ("max: " + max);
			}
				
			System.err.println("==  plans searched " + rt.split() + " ==");
			System.err.println("== show all plans ==");




			System.err.println("== Finished. " + rt.split() + " elapsed time. " + rt.stop() + " total time.");
			
		} catch (Exception e) {
			
			System.out.print ("Exception: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}

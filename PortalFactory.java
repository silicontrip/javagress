import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;  
import java.util.*;
import com.google.common.geometry.*;

// This is more than just a portal factory.
// includes methods for generating link and field sets
// and filtering link and field sets.

public class PortalFactory {
	
	protected String portalApi;
	protected String linkApi;
	private static PortalFactory instance = null;
	
	public static PortalFactory getInstance() throws java.io.IOException {
		if (instance == null) {
			instance = new PortalFactory();
		}
		return instance;
	}
	// from http://stackoverflow.com/questions/13592236/parse-the-uri-string-into-name-value-collection-in-java	
	protected static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		final String[] pairs = url.getQuery().split("&");
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!query_pairs.containsKey(key)) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
	}
	
	public PortalFactory () throws java.io.IOException {
                        Properties fileProperties = new Properties();

                        FileInputStream fis = new FileInputStream(new File("portalfactory.properties"));
                        fileProperties.load (fis);
                        fis.close();

			// will use file:// for cache
                        linkApi = fileProperties.getProperty("linkurl");
                        portalApi = fileProperties.getProperty("portalurl");

	}
	
	public String[] getCornerPortalsFromJSON(String clusterDescription) throws IOException
	{
		HashMap<String,HashMap<String,Object>> guidMap;
		ObjectMapper mapper = new ObjectMapper();
		
		ArrayList<Polygon> tmpObj;
		
		// System.out.println(clusterDescription);
		
		try {
			tmpObj = mapper.readValue(clusterDescription,new TypeReference<Collection<Polygon>>() {});
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			throw new IOException("Invalid Drawtools: " + e);
		}
	
		// System.out.println(tmpObj.latLngs);
		
		// there should be only 1 entry
		for (Polygon entry : tmpObj) {

			String[] r = new String[3];
			
			PolyPoint[] pp = entry.latLngs.toArray(new PolyPoint[3]);
			r[0] = pp[0].toString();
			r[1] = pp[1].toString();
			r[2] = pp[2].toString();
			return r;
		}
		
		return null;
		
	}
	
	public Portal[] getCornerPortalsFromString(String clusterDescription) throws IOException
	{
		// split on -
		// make that / as some portals have - in their name
		// going for = now found a portal with / in it's name
		String[] parts = clusterDescription.split("=");
		Portal[] portals;
		
		int size =  parts.length;

		if (clusterDescription.startsWith("[{")) {
			String[] portalNames = getCornerPortalsFromJSON(clusterDescription);
			portals = new Portal[3];
			portals[0] = getPortal(portalNames[0]);
			portals[1] = getPortal(portalNames[1]);
			portals[2] = getPortal(portalNames[2]);
			return portals;

			
		} else if  (size == 3) {
			portals = new Portal[3];
			portals[0] = getPortal(parts[0]);
			portals[1] = getPortal(parts[1]);
			portals[2] = getPortal(parts[2]);
			return portals;

		}
		else 
		{
			// throw parsing exception
			throw new RuntimeException("Invalid Portal description");
		}
		
	}
	
	public HashMap<String,Portal> portalClusterFromString (String clusterDescription) throws IOException
	{
		
		// split on -
		String[] parts = clusterDescription.split("=");
		
		int size =  parts.length;
		
		// if begins with ./
		if (clusterDescription.startsWith("./")) {
			return getPortalsFromFile(clusterDescription);
		} 
		
		// description from drawtools
		else if (clusterDescription.startsWith("[{")) {
			String[] portalNames = getCornerPortalsFromJSON(clusterDescription);

			return getPortalsInTri(portalNames[0],portalNames[1],portalNames[2]);

			
		}
		// get clusters in cell
		else if (clusterDescription.startsWith("0x"))
		{
			S2CellId cellid = new S2CellId(Long.decode(clusterDescription) << 32);
			S2Cell cell = new S2Cell(cellid);
			S2LatLng loc = new S2LatLng(cell.getCenter());

			HashMap<String,Portal> portals = getPortals("" + loc.latDegrees() + "," + loc.lngDegrees(),"1");

				HashMap<String,Portal> cellportals = new HashMap<String,Portal>();

	for (String guid : portals.keySet())
	{
		Portal p = portals.get(guid);
		S2LatLng ploc = S2LatLng.fromE6(p.getLatE6().longValue(),p.getLngE6().longValue());
		S2CellId pcell = S2CellId.fromLatLng(ploc).parent(13);
		//System.out.println(p.getTitle() + " : " + pcell.toToken() + " == " + ag.getArgumentAt(0));
		
		if (("0x" +pcell.toToken()).equals(clusterDescription))
			cellportals.put(guid,p);
	}
		return cellportals;
		}

		// single (or range)
		else if (size == 1) {			
			// split on :
			String[] ranges = clusterDescription.split(":");
			int rangeSize = ranges.length;
			
			String range = new String("0");
			if (rangeSize == 2)
			{
				range = ranges[1];
			}
			else if (rangeSize>2)
			{
				// throw parsing error
				throw new RuntimeException("Invalid Portal description");
			}
			// get portals in range
			return getPortals(ranges[0],range);
		}
		
		// 2 for square
		else if (size == 2) {
			// get portals
			// as soon as I write the following method
			return getPortalsInBox(parts[0],parts[1]);
			
		}
		// 3 for triangle
		else if (size == 3) {
			return getPortalsInTri(parts[0],parts[1],parts[2]);
		}
		else 
		{
			// throw parsing exception
			throw new RuntimeException("Invalid Portal description");
		}
		
	}
	
	public HashMap<String,Portal> getPortalsInBox (String loc1, String loc2) throws IOException 
	{
		
		String urlString = portalApi + "?ll=" + URLEncoder.encode(loc1,"UTF-8") +"&l2="+URLEncoder.encode(loc2,"UTF-8") ;
		
		// System.out.println(urlString);
		
		return readPortalsFromUrl(urlString);
		
		
	}
	
	public HashMap<String,Portal> getPortalsInTri (String loc1, String loc2, String loc3) throws IOException
	{
		
		String urlString = portalApi + "?ll=" + URLEncoder.encode(loc1,"UTF-8") +"&l2="+URLEncoder.encode(loc2,"UTF-8") + "&l3=" + URLEncoder.encode(loc3,"UTF-8");
		
		return readPortalsFromUrl(urlString);
		
		
	}
	
	public HashMap<String,Portal> getPortals(String location, String range) throws IOException
	{
		
		String urlString = portalApi + "?ll=" + URLEncoder.encode(location,"UTF-8") +"&rr="+URLEncoder.encode(range,"UTF-8");
		return readPortalsFromUrl(urlString);
		
	}
	
	
	public Portal getPortal(String location) throws IOException
	{
		
		String urlString = portalApi + "?ll=" + URLEncoder.encode(location,"UTF-8") ;
		HashMap <String,Portal> pts = readPortalsFromUrl(urlString);
		
		return (Portal)(pts.values().toArray()[0]);
		
	}
	
	public HashMap<String,Portal> getPortalsFromFile(String file) throws IOException
	{
		
		// new hashmap
		
		HashMap <String,Portal> portals = new HashMap<String,Portal>();
		
		// while every line
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		
		// not sure if it would be better to read all portals and then filter
		// or query each line.
		// portal db is now larger than the java heap can manage
		
		while ((line = br.readLine()) != null) {
			
			// getPortals(line,0)
			// add to hashmap
			
			if (!line.startsWith("#")) {
				portals.putAll(getPortals(line,"0"));
			}
			// end while
			
			
		}
		br.close();
		
		return portals;
		
		// return hashmap
		
	}

	protected HashMap<String,HashMap<String,Object>> portalSearch (PortalSelectionStrategy ps, HashMap<String,HashMap<String,Object>> pm)
	{
		HashMap<String,HashMap<String,Object>> newList = new HashMap<String,HashMap<String,Object>>();
		
		
		for (Map.Entry<String, HashMap<String,Object>> entry : pm.entrySet()) {
			String portalKey = entry.getKey();
			HashMap<String, Object> portalEntry = entry.getValue();
			if (ps.match(portalEntry)) {
				newList.put(portalKey,portalEntry);
			}
		}

		return newList;
		
	}
	
	protected Point getLocation(HashMap<String,HashMap<String,Object>> portalMap,String locationDesc) throws IOException
	{

		// check for lat/lng
		String latlng[] = locationDesc.split(",");
		// check for guid
		boolean guid = locationDesc.matches("^[0-9a-fA-F]{32}.1[16]$");
		// check for title


		String search;
		
		if (latlng.length == 2)
		{
			return new Point(Double.valueOf(latlng[0]),Double.valueOf(latlng[1]));
		} else if (guid) {
			search ="guid";
		} else {
			search = "title";
		}

		System.out.println("File searching on: " + search);
		
		for (Map.Entry<String, HashMap<String,Object>> entry : portalMap.entrySet()) {
			HashMap<String, Object> portalEntry = entry.getValue();
			String portalSearch = (String)portalEntry.get(search);
		// System.out.println(portalSearch + " == " + locationDesc);
			if (locationDesc.equalsIgnoreCase(portalSearch)) {
				return new Point (((Integer)portalEntry.get("lat")).longValue(),((Integer)portalEntry.get("lng")).longValue());
			}
		}

		throw new IOException("No Matching Portals: "+ search + "="+locationDesc);
		
//		return null; // or should throw exception.

	}

	protected HashMap<String,HashMap<String,Object>> purgePortals(HashMap<String,HashMap<String,Object>> portalMap, URL url) throws java.io.UnsupportedEncodingException, IOException
	{
		// determine query type
		//
		// determine portal-location type


		Map<String, List<String>> query = splitQuery(url);
		
		// ll, l2, l3, rr
		// single portal ll=
		// box ll=, l2=
		// tri ll=, l2=, l3=
		// circle ll=, rr=
		// since this string is constructed internally, we will do minimal checking.
		
		//System.out.println(query.get("ll"));
		
		if (query.containsKey("rr")) {

			Point loc = getLocation (portalMap,query.get("ll").get(0));
			Float range = Float.valueOf(query.get("rr").get(0));
			PortalSelectionStrategy  pss = new PortalSelectionRangeStrategy(loc,range);
			
			portalMap = portalSearch(pss,portalMap);
			
		} else if (query.containsKey("l3")) {
			// triangle
			
			Point loc1 = getLocation (portalMap,query.get("ll").get(0));
			Point loc2 = getLocation (portalMap,query.get("l2").get(0));
			Point loc3 = getLocation (portalMap,query.get("l3").get(0));

			
		} else if (query.containsKey("l2")) {
			// box
			Point loc1 = getLocation (portalMap,query.get("ll").get(0));
			Point loc2 = getLocation (portalMap,query.get("l2").get(0));
			PortalSelectionStrategy  pss = new PortalSelectionBoxStrategy(loc1,loc2);

			portalMap = portalSearch(pss,portalMap);
			
			
		} else {
			// single
			Point loc = getLocation (portalMap,query.get("ll").get(0));
			PortalSelectionStrategy  pss = new PortalSelectionRangeStrategy(loc,new Float(0));
			
			portalMap = portalSearch(pss,portalMap);

		}
	
		return portalMap;	

	}
	
	protected HashMap<String,Portal> readPortalsFromUrl (String urlString) throws IOException
	{
		
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";

		
		url = new URL(urlString);
		//System.out.println("urlstring: " + urlString + " query: " +url.getQuery());
		System.out.println("Query: " +url.getQuery());

		rd = new BufferedReader(new InputStreamReader(url.openStream()));
		while ((line = rd.readLine()) != null) { result += line; }
		rd.close();
		
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String,HashMap<String,Object>> guidMap;
		
		try {
			 guidMap = mapper.readValue(result,new TypeReference<HashMap<String,HashMap<String,Object>>>() {});
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			throw new IOException("Cannot decode request: " + urlString);
		}
		if (url.getProtocol().equals("file")) {
			// purge portals		
			guidMap = purgePortals(guidMap,url);
		}
		
		HashMap<String,Portal> resultMap = new HashMap<String,Portal>();
		
		for (Map.Entry<String, HashMap<String,Object>> entry : guidMap.entrySet()) {
			
			String key = entry.getKey();
			
			//	 System.out.println(entry.getValue());
			
			// HashMap<String,String> portalMap = mapper.readValue(entry.getValue(),HashMap.class);
			
			Portal portal = new Portal (entry.getValue());
			
			resultMap.put(key,portal);
		}
		
		return resultMap;
		
	}

	public HashMap<String,Portal> reducePortals (HashMap<String,Portal> portals, Double threshold) {

		//int pcount[] = new int[portals.size()];
		HashMap<String,Integer> pcount = new HashMap<String,Integer>();
		HashMap<String,HashSet<String>> pmatch = new HashMap<String,HashSet<String>>();
		HashMap<String,Portal> portalmap = new HashMap<String,Portal>();
	
		for (String guid: portals.keySet())
		{
			Portal po = portals.get(guid);
			pcount.put(guid, new Integer(0));
			pmatch.put(guid,new HashSet<String>());
			for (Portal p2: portals.values())
				if (po.getGeoDistance(p2) < threshold)
				{
					Integer i = pcount.get(guid);
					i++;
					pcount.put(guid,i);
					HashSet<String> tm = pmatch.get(guid);
					tm.add(p2.getGuid());
				}


		}
		// um now what?	
		
		while (pcount.size() > 0)
		{
			Integer max = new Integer(0);
			String maxguid=null;

			for (String guid: pcount.keySet())
			{
				Integer thisCount = pcount.get(guid);
				if (thisCount > max)
				{
					max = thisCount;
					maxguid = new String(guid);
				}

		// find portal with the highest count
		// add portal to output list.
		// remove matching portals
			}	
			portalmap.put(maxguid,portals.get(maxguid));
			for (String gg: pmatch.get(maxguid))
				pcount.remove(gg);

		}

		return portalmap;

	}
	
	
	public ArrayList<Link> getPurgedLinks (Collection<Portal> portals) throws IOException {
		
		HashMap<String,Link> links = this.getLinks();
		return purgeLinks(portals,links);
		
	}
	
	public static ArrayList<Link> purgeLinks (Collection<Portal> portals, HashMap<String,Link> links ) {
	
		Long minLng=0L;
		Long minLat=0L;
		Long maxLng=0L;
		Long maxLat=0L;
		
		ArrayList<Link> purgeList = new ArrayList<Link>();
		
		// determine bounds
		// must move this to serverside
		boolean first = true;
		for (Portal portal : portals) {
			
			Long lat = portal.getLatE6();
			Long lng = portal.getLngE6();
			
			if (first) {
				minLng = lng;
				maxLng = lng;
				minLat = lat;
				maxLat = lat;
				first = false;
			}
			
			if (lat > maxLat) { maxLat = lat; }
			if (lng > maxLng) { maxLng = lng; }
			if (lat < minLat) { minLat = lat; }
			if (lng < minLng) { minLng = lng; }
			
		}
		
		// create bounding box
		
		Line line0 = new Line(minLat,minLng, minLat, maxLng);
		Line line1 = new Line(minLat, maxLng,maxLat,maxLng);		
		Line line2 = new Line(maxLat,maxLng, maxLat, minLng);
		Line line3 = new Line(maxLat, minLng,minLat,minLng);
		
		//System.err.println("Bounds: Lat: " + minLat + " - " + maxLat + " Lng: " + minLng + " - " + maxLng);
		
		
		
		for (Link link: links.values()) {
			
			// Line linkLine = link.getLine();

		//	System.out.println("link: " + link);
			
			// if link intesects or is contained in bounding box
			if ((line0.intersects(link) ||
				 line1.intersects(link) ||
				 line2.intersects(link) ||
				 line3.intersects(link) ) ||
				(link.getoLat() >= minLat && link.getoLat() <= maxLat &&
				 link.getoLng() >= minLng && link.getoLng() <= maxLng)
				)
			{
				purgeList.add(link);
			}
		}
		
		return purgeList;
		
	}
	
	
	public HashMap<String,Link> getLinks() throws IOException
	{
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		
		String urlString = linkApi ;
		
		// System.out.println(urlString);
		
		url = new URL(urlString);
		
		//conn = (HttpURLConnection) url.openConnection();
		//conn.setRequestMethod("GET");
		rd = new BufferedReader(new InputStreamReader(url.openStream()));
		while ((line = rd.readLine()) != null) {
			result += line;
		}
		rd.close();
		
		ObjectMapper mapper = new ObjectMapper();
		
		ArrayList<HashMap<String,Object>> guidMap = mapper.readValue(result,new TypeReference<ArrayList<HashMap<String,Object>>>(){});
		
		HashMap<String,Link> resultMap = new HashMap<String,Link>();
		
		for (HashMap<String,Object> entry : guidMap ) {
			
			String key = (String)entry.get("guid");
			
			//	 System.out.println(entry.getValue());
			
			// HashMap<String,String> portalMap = mapper.readValue(entry.getValue(),HashMap.class);
			
			Link link  = new Link (entry);
			
			resultMap.put(key,link);
		}
		
		return resultMap;
		
	}
	
	public static ArrayList<Line> makeLinksFromSingleCluster(Collection<Portal> portals)
	{
		ArrayList<Line> la = new ArrayList<Line>();

		Object[] portalKeys = portals.toArray();
		
		for (int i =0; i<portalKeys.length; i++)
		{
			Portal pki = (Portal)portalKeys[i];
			
			for (int j=i+1; j<portalKeys.length; j++)
			{
				Portal pkj = (Portal)portalKeys[j];
				Line li = new Line (pki,pkj);
				la.add(li);
			}
		}
		return la;
	}

	public static ArrayList<Line> makeLinksFromDoubleCluster(Collection<Portal> portals1,Collection<Portal> portals2)
	{
		ArrayList<Line> la = new ArrayList<Line>();
		
		for (Portal pki: portals1)
		{
			
			for (Portal pkj: portals2)
			{
				Line li = new Line (pki,pkj);
				la.add(li);
			}
		}
		return la;
	}

	public static ArrayList<Field> fieldsOverTarget (Collection<Field>allfields, ArrayList<Point>target)
	{
		ArrayList<Field> fa = new ArrayList<Field>();

		for (Field fi: allfields)
			if (fi.inside(target))
				fa.add(fi);

		return fa;
	}
// 
	public static ArrayList<Field> percentileFields(Collection<Field>fields, Double percentile,boolean mu) throws javax.xml.parsers.ParserConfigurationException, java.io.IOException
	{
		ArrayList<Field> la = new ArrayList<Field>();


		Map<Double,Field> fieldSize = new TreeMap<Double,Field>(Collections.reverseOrder());

		for (Field f: fields) 
		if (mu) 
			fieldSize.put(f.getEstMu(),f);
		else
			fieldSize.put(f.getGeoArea(),f);
		
		Object[] fs = fieldSize.values().toArray();
		int end = (int) (fs.length * percentile / 100.0);

		// I recon there's a quicker way to slice an array.
		for (int i =0; i<end; i++)
			la.add( (Field)fs[i]);

		return la;

	}

	public static ArrayList<Line> percentileLinks(Collection<Line>lines, Double percentile)
	{
		ArrayList<Line> la = new ArrayList<Line>();


		Map<Double,Line> linkLength = new TreeMap<Double,Line>(Collections.reverseOrder());

		for (Line l: lines) 
			linkLength.put(l.getGeoDistance(),l);
		
		Object[] ll = linkLength.values().toArray();
		int end = (int) (ll.length * percentile / 100.0);

		// I recon there's a quicker way to slice an array.
		for (int i =0; i<end; i++)
			la.add( (Line)ll[i]);

		return la;

	}
	public static ArrayList<Line> filterLinks(Collection<Line>lines, Collection<Link> links, teamCount max)
	{
		
		ArrayList<Line> la = new ArrayList<Line>();
	
		//System.out.println("" + links.size() + " links in play");
		//System.out.println("" + max + " blocking limits");
		
		for (Line l: lines) {
				
			teamCount bb = new teamCount();
			for (Link link: links) {
				if (l.intersects(link)) {
					//System.out.println("** intersect team: "+ link.getTeamEnum());
					bb.incTeamEnum(link.getTeamEnum());
				}
				if (bb.moreThan(max))
					break;
			}
			if (!bb.moreThan(max))
				la.add(l);
		}
	
		return la;
	}

	public static ArrayList<Field> filterFields(Collection<Field> fields, Collection<Link> links, teamCount max) 
	{
		ArrayList<Field> fa = new ArrayList<Field>();

		for (Field fi: fields) {
			teamCount bb = new teamCount();
			for (Link link: links) {
				if (fi.intersects(link)) {
					//System.out.println("** intersect team: "+ link.getTeamEnum());
					bb.incTeamEnum(link.getTeamEnum());
				}
				if (bb.moreThan(max))
					break;
			}
			if (!bb.moreThan(max))
				fa.add(fi);
			
		
		}
		return fa;
	}

	private static boolean linkExists (Object[] lk, int j, Point p1, Point p2)
	{
		
		for (int k=j+1; k<lk.length; k++)
		{
			Line l3 = (Line)lk[k];
			if (
				(p1.equals(l3.getO()) && p2.equals(l3.getD())) ||
				(p1.equals(l3.getD()) && p2.equals(l3.getO()))
			) { 
				return true;
			}
		}
		return false;
	}

	public static ArrayList<Field> makeFieldsFromSingleLinks(Collection<Line>lines)
	{
		Object[] lk = lines.toArray();
		ArrayList<Field> fa = new ArrayList<Field>();
		for (int i =0; i<lk.length; i++)
		{
			Line l1 = (Line)lk[i];
			for (int j=i+1; j<lk.length; j++)
			{
				Line l2 = (Line)lk[j];
				
				// point l1.o == point l2.o
				if (l1.getO().equals(l2.getO())) {
					if (linkExists(lk,j,l1.getD(),l2.getD()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getD()));
				} else if (l1.getO().equals(l2.getD())) {
					if (linkExists(lk,j,l1.getD(),l2.getO()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getO()));
				} else if (l1.getD().equals(l2.getO())) {
					if (linkExists(lk,j,l1.getO(),l2.getD()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getD()));
				} else if (l1.getD().equals(l2.getD())) {
					if (linkExists(lk,j,l1.getO(),l2.getO()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getO()));
				}
			}
		}
		return fa;
	}

	// the argument order is important.
	// two from lines1 and 1 from lines2
	public static ArrayList<Field> makeFieldsFromDoubleLinks(Collection<Line>lines1, Collection<Line>lines2)
	{
		Object[] lk1 = lines1.toArray();
		Object[] lk2 = lines2.toArray();

		ArrayList<Field> fa = new ArrayList<Field>();
		for (int i =0; i<lk1.length; i++)
		{
			Line l1 = (Line)lk1[i];
			for (int j=i+1; j<lk1.length; j++)
			{
				Line l2 = (Line)lk1[j];
				
				// point l1.o == point l2.o
				if (l1.getO().equals(l2.getO())) {
					if (linkExists(lk2,-1,l1.getD(),l2.getD()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getD()));
				} else if (l1.getO().equals(l2.getD())) {
					if (linkExists(lk2,-1,l1.getD(),l2.getO()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getO()));
				} else if (l1.getD().equals(l2.getO())) {
					if (linkExists(lk2,-1,l1.getO(),l2.getD()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getD()));
				} else if (l1.getD().equals(l2.getD())) {
					if (linkExists(lk2,-1,l1.getO(),l2.getO()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getO()));
				}
			}
		}
		return fa;
	}

	public static ArrayList<Field> makeFieldsFromTripleLinks(Collection<Line>lines1, Collection<Line>lines2,Collection<Line>lines3)
	{
		Object[] lk1 = lines1.toArray();
		Object[] lk2 = lines2.toArray();
		Object[] lk3 = lines3.toArray();

		ArrayList<Field> fa = new ArrayList<Field>();
		for (int i =0; i<lk1.length; i++)
		{
			Line l1 = (Line)lk1[i];
			for (int j=0; j<lk2.length; j++)
			{
				Line l2 = (Line)lk2[j];
				
				// point l1.o == point l2.o
				if (l1.getO().equals(l2.getO())) {
					if (linkExists(lk3,-1,l1.getD(),l2.getD()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getD()));
				} else if (l1.getO().equals(l2.getD())) {
					if (linkExists(lk3,-1,l1.getD(),l2.getO()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getO()));
				} else if (l1.getD().equals(l2.getO())) {
					if (linkExists(lk3,-1,l1.getO(),l2.getD()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getD()));
				} else if (l1.getD().equals(l2.getD())) {
					if (linkExists(lk3,-1,l1.getO(),l2.getO()))
						fa.add(new Field(l1.getO(),l1.getD(),l2.getO()));
				}
			}
		}
		return fa;
	}
	public static ArrayList<Point> getPointsFromString (String pointList)
	{
		
		String[] pointDesc = pointList.split(",");
		if (pointDesc.length % 2 == 1)
			return null; // probably should throw an exception
		
		ArrayList<Point> pa = new ArrayList<Point>();
		
		for (int i =0; i < pointDesc.length; i += 2)
			pa.add(new Point(pointDesc[i],pointDesc[i+1]));

		System.out.println("Got " + pa.size() + " points.");

		return pa;
	}

}


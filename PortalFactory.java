import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;  
import java.util.*;




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
	public static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
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


		//System.out.println("link: " + linkApi);
		//System.out.println("portal: " + portalApi);
	}
	
	public HashMap<String,Portal> portalsFromString (String portalDescription) throws IOException
	{
		
		return null;
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
		String[] parts = clusterDescription.split("/");
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
		String[] parts = clusterDescription.split("/");
		
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
		
		// single (or range)
		else if (size == 1) {			
			// split on :
			String[] ranges = clusterDescription.split(":");
			int rangeSize = ranges.length;
			
			String range = new String("0");
			
			if (rangeSize == 1) 
			{
				range = new String("0");
			}
			else if (rangeSize == 2) 
			{
				range = ranges[1];
			}
			else 
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
		
		String urlString = portalApi + "ll=" + URLEncoder.encode(loc1,"UTF-8") +"&l2="+URLEncoder.encode(loc2,"UTF-8") ;
		
		// System.out.println(urlString);
		
		return readPortalsFromUrl(urlString);
		
		
	}
	
	public HashMap<String,Portal> getPortalsInTri (String loc1, String loc2, String loc3) throws IOException
	{
		
		String urlString = portalApi + "ll=" + URLEncoder.encode(loc1,"UTF-8") +"&l2="+URLEncoder.encode(loc2,"UTF-8") + "&l3=" + URLEncoder.encode(loc3,"UTF-8");
		
		// System.out.println(urlString);
		
		return readPortalsFromUrl(urlString);
		
		
	}
	
	public HashMap<String,Portal> getPortals(String location, String range) throws IOException
	{
		
		String urlString = portalApi + "ll=" + URLEncoder.encode(location,"UTF-8") +"&rr="+URLEncoder.encode(range,"UTF-8");
		
		// System.out.println(urlString);
		
		return readPortalsFromUrl(urlString);
		
	}
	
	
	public Portal getPortal(String location) throws IOException
	{
		
		String urlString = portalApi + "ll=" + URLEncoder.encode(location,"UTF-8") ;
		
		// System.out.println(urlString);
		
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
	
	protected Point getLocation(HashMap<String,HashMap<String,Object>> portalMap,String locationDesc) 
	{

		// check for lat/lng
		String latlng[] = locationDesc.split(",");
		// check for guid
		boolean guid = locationDesc.matches("/^[0-9a-fA-F]{32}.1[16]$/");
		// check for title

		String search;
		
		if (latlng.length == 2)
		{
			return new Point(Float.valueOf(latlng[0]),Float.valueOf(latlng[1]));
		} else if (guid) {
			search ="guid";
		} else {
			search = "title";
		}
		
		for (Map.Entry<String, HashMap<String,Object>> entry : portalMap.entrySet()) {
			HashMap<String, Object> portalEntry = entry.getValue();
			String portalSearch = (String)portalEntry.get(search);
			if (locationDesc.equals(portalSearch)) {
				return new Point (((Integer)portalEntry.get("lat")).longValue(),((Integer)portalEntry.get("lng")).longValue());
			}
		}

		return null; // or should throw exception.

	}

	protected HashMap<String,HashMap<String,Object>> purgePortals(HashMap<String,HashMap<String,Object>> portalMap, URL url) throws java.io.UnsupportedEncodingException
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
		
		System.out.println(query.get("ll"));
		
		if (query.containsKey("rr")) {

			Point loc = getLocation (portalMap,query.get("ll").get(0));
			//Point loc = getLocation (portalMap,"garbage");

			Float range = Float.valueOf(query.get("rr").get(0));
			//Float range = Float.valueOf("1");
			PortalSelectionStrategy  pss = new PortalSelectionRangeStrategy(loc,range);
			
			portalMap = portalSearch(pss,portalMap);
			
				// filter portal
				// call back filter function...	
			
				//resultMap.put(key,portal);

			// circular range
		} else if (query.containsKey("l3")) {
			// triangle
			
			Point loc1 = getLocation (portalMap,query.get("ll").get(0));
			Point loc2 = getLocation (portalMap,query.get("l2").get(0));
			Point loc3 = getLocation (portalMap,query.get("l3").get(0));

			
		} else if (query.containsKey("l2")) {
			// box
			Point loc1 = getLocation (portalMap,query.get("ll").get(0));
			Point loc2 = getLocation (portalMap,query.get("l2").get(0));

		} else {
			// single
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
		System.out.println("query: " +url.getQuery());

		
		//conn = (HttpURLConnection) url.openConnection();
		//conn.setRequestMethod("GET");
		//rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		rd = new BufferedReader(new InputStreamReader(url.openStream()));
		while ((line = rd.readLine()) != null) {
			result += line;
		}
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
	
	
	public ArrayList<Link> getPurgedLinks (Collection<Portal> portals) throws IOException {
		
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
		
		HashMap<String,Link> links = this.getLinks();
		
		
		for (Link link: links.values()) {
			
			Line linkLine = link.getLine();

		//	System.out.println("link: " + link);
			
			// if link intesects or is contained in bounding box
			if ((line0.intersects(linkLine) ||
				 line1.intersects(linkLine) ||
				 line2.intersects(linkLine) ||
				 line3.intersects(linkLine) ) ||
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
	
	
}


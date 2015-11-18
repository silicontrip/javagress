import java.util.HashMap;
import java.util.ArrayList;
import com.google.common.geometry.*;

public class Portal {

	protected String guid;
	protected String title;
	protected Integer health;
	protected Integer resCount;
	protected String team;
	protected Integer level;
	protected S2LatLng latlng;

	public String getGuid() { return guid; }
	
//	public Long getLatE6() { return lat; }
//	public Long getLngE6() { return lng; }
	
	public Double getLat() { return latlng.latDegrees(); }
	public Double getLng() { return latlng.lngDegrees(); }
	
	public String getTitle() { return title; }
	public Integer getLevel() { return level; }
	public Integer getResCount() { return resCount; }
	public Integer getHealth() { return health; }
	public String getTeam() { return team; }
	public String getShortTeam() { 
        if (team.startsWith("E"))
			return "ENL";
        if (team.startsWith("R"))
			return "RES";
        if (team.startsWith("N"))
			return "NEU";
		
		return "";
	}
		
	//public Point getPoint() { return new Point(getLatE6(),getLngE6()); }
	public S2LatLng getLatLng() { return latlng; }
	
	
	public void setGuid(String g) { guid = g; }
	public void setTitle(String t) { title = t; }
	public void setHealth(Integer h) { health = h; }
	public void setResCount(Integer r) { resCount = r; }
	public void setTeam(String t) { team = t; }
	public void setLevel(Integer l) { level = l; }
	//public void setPoint(Point lo) { setLat(lo.getLat()); setLng(lo.getLng()); }
	public void setLatLng(S2LatLng lo) { latlng = lo; }
	public void setLatLngFromE6(long latE6,long lngE6) { latlng = S2LatLng.fromE6(latE6,lngE6); }
	//public void setLat(Long l) { lat = l; }
	//public void setLng(Long l) { lng = l; }
	
	public boolean isEnlightened() { return (team.startsWith("E")); }
	public boolean isResistance() { return (team.startsWith("R")); }
	
	public int countLinks(Link[] links) 
	{
		int count  = 0;
		for (Link l: links)
		{
			if (l.getdLocation().equals(getLatLng()) ||
				l.getoLocation().equals(getLatLng()))
			{
				count++;
			}
		}
		return count;
		
	}
	
	public int countdLinks(Link[] links) 
	{
		int count  = 0;
		for (Link l: links)
		{
			if (l.getdLocation().equals(getLatLng()))
			{
				count++;
			}
		}
		return count;
		
	}

	public int countoLinks(Link[] links) 
	{
		int count  = 0;
		for (Link l: links)
		{
			if (l.getoLocation().equals(getLatLng()))
			{
				count++;
			}
		}
		return count;
		
	}
	
	Portal[] getConnectedPortals(Link[] links, HashMap<String,Portal> portals)
	{
	
		ArrayList<Portal> connectedPortals = new ArrayList<Portal>();
		for (Link l: links)
		{
			String guid;
			if (l.getoLocation().equals(getLatLng()))
			{
				// add portal
				Portal p = portals.get(l.getdGuid());
				if (p != null) {
					connectedPortals.add(p);
				}
			}
			if (l.getdLocation().equals(getLatLng()))
			{
				Portal p = portals.get(l.getoGuid());
				if (p != null) {
					connectedPortals.add(p);
				}
			}
		}
		return connectedPortals.toArray(new Portal[connectedPortals.size()]);
	}
	
	
	public Portal (HashMap<String,Object> pt) 
	{
		this (
			(String)pt.get("guid"), 
			(String)pt.get("title"),
			(Integer)pt.get("health"),
			(Integer)pt.get("rescount"),
			(String)pt.get("team"),
			(Integer)pt.get("level"),
			(Integer)pt.get("lat"),
			(Integer)pt.get("lng"));
	}
	
	
	public Portal ( String g, String ti, Integer h, Integer r, String te, Integer le, Integer la, Integer lo) 
	{
		setGuid(g);
		setTitle(ti);
		setHealth(h);
		setResCount(r);
		setTeam(te);
		setLevel(le);
		setLatLngFromE6( new Long(la), new Long(lo));
	}
	
	
	public Portal ( String g, String ti, Integer h, Integer r, String te, Integer le, Long la, Long lo) 
	{
		setGuid(g);
		setTitle(ti);
		setHealth(h);
		setResCount(r);
		setTeam(te);
		setLevel(le);
		setLatLngFromE6(la, lo);

	}
	
	@Override
	public String toString () {
	
		// print $portal['title'] . " (" . $portal['level'] . ":" . $portal['rescount'] . "(" . $portal['health'] . "))";
		
		return new String (this.getTitle() + " " + this.getShortTeam() + "(" + this.getLevel() + ":" + this.getResCount() + "(" + this.getHealth() + "))");
	}
	
	public String getUrl() {
		return new String ("https://www.ingress.com/intel?ll=" + this.getLat() +"," + this.getLng() + "&z=18&pll=" + this.getLat() +"," + this.getLng());
	}
	
	public String toExtendedString () {

/*		print " " . $portal['team'] . " ";
		$lat = $portal['lat'] / 1000000;
		$lng = $portal['lng'] / 1000000;
		
		print "https://www.ingress.com/intel?ll=". $lat . "," . $lng . "&z=18&pll=" . $lat . "," . $lng ;
*/		
				
		return new String (this.getTitle() + ", " + this.getShortTeam() + "(" + this.getLevel() + ":" + this.getResCount() + "(" + this.getHealth() + ")) " +
						   " " + this.getUrl()); 
		
	}
}
	

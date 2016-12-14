import java.util.HashMap;
import java.util.ArrayList;

public class Link extends Line {

	
	public static final int NEUTRAL = 0;
	public static final int ENLIGHTENED = 1;
	public static final int RESISTANCE = 2;
	
	
	protected String guid;
//	protected Long dLat;
//	protected Long dLng;
	protected String dGuid;
	
//	protected Long oLat;
//	protected Long oLng;
	protected String oGuid;
	protected String team;
	protected int teamEnum;

//	public Long getdLat() { return dLat; }
//	public Long getdLng() { return dLng; }
//	public void setdLat(Long l) { dLat=l; }
//	public void setdLng(Long l) { dLng=l; }

//	public Long getoLat() { return oLat; }
//	public Long getoLng() { return oLng; }
//	public void setoLat(Long l) { oLat=l; }
//	public void setoLng(Long l) { oLng=l; }

	public String getGuid() { return guid; }
	// public void setGuid(String g) { guid = g; }

	public String getdGuid() { return dGuid; }
	// public void setdGuid(String g) { dGuid = g; }

	public String getoGuid() { return oGuid; }
	// public void setoGuid(String g) { oGuid = g; }
	
	//public void setdLocation(Point lo) { setdLat(lo.getLatE6()); setdLng(lo.getLngE6()); }
	//public void setoLocation(Point lo) { setoLat(lo.getLatE6()); setoLng(lo.getLngE6()); }

	public Point getdLocation() { return new Point(getdLat(),getdLng()); }
	public Point getoLocation() { return new Point(getoLat(),getoLng()); }
	
	public int getTeamEnum() { return teamEnum; }
	public String getTeam() { return team; }

	public ArrayList<Link> getIntersects(Link[] links)
	{
		ArrayList<Link> allLinks = new ArrayList<Link>();
		for (Link link : links)
		{
			if (this.intersects(link)) { allLinks.add(link); }
		}
		return allLinks;
	}
		
		
	/*
	public boolean intersects (Link[] links)
	{
		Line l = getLine();
		
		for (Link link : links)
		{
			if (l.intersects(link.getLine())) { return true; }
		}
		
		return false;
	}
	*/
	/*
	public boolean intersectsOrEqual (Link[] links)
	{
		Line l = getLine();
		
		for (Link link : links)
		{
			if (l.intersectsOrEqual(link.getLine())) { return true; }
		}
		
		return false;
	}
	*/
	
	public void setTeam(String t) { 
		team = t;
		
		if (team.startsWith("R")) {
			teamEnum	= RESISTANCE;
		}
        if (team.startsWith("E")) {
			teamEnum = ENLIGHTENED;
		}
		 
	}

	// public Line getLine() { return new Line (this.getoLat(),this.getoLng(), this.getdLat(), this.getdLng()); }
	
	public Link (HashMap<String,Object> pt) 
	{
		this ((String)pt.get("guid"), 
			  (String)pt.get("dguid"),
			  (Integer)pt.get("dlat"),
			  (Integer)pt.get("dlng"),
			  (String)pt.get("oguid"),
			  (Integer)pt.get("olat"),
			  (Integer)pt.get("olng"),
			  (String)pt.get("team"));
	}
	
	public Link (String g, String dg, Integer dla,Integer dlo, String og, Integer ola,Integer olo,String tt)
	{
		super(new Long(dla),new Long(dlo),new Long(ola),new Long(olo));
		guid = g;
		dGuid=dg;
		oGuid=og;
		team = tt;

		//setGuid(g);
		//setdGuid(dg);
		//setdLat(new Long(dla));
		//setdLng(new Long(dlo));
		//setoGuid(og);
		//setoLat(new Long(ola));
		//setoLng(new Long(olo));
		//setTeam(team);
	}
	
	
	public Link (String g, String dg, Long dla,Long dlo, String og, Long ola,Long olo,String tt)
	{
		super(dla,dlo,ola,olo);
		guid = g;
		dGuid=dg;
		oGuid=og;
		team = tt;
		//setGuid(g);
		//setdGuid(dg);
		//setdLat(dla);
		//setdLng(dlo);
		//setoGuid(og);
		//setoLat(ola);
		//setoLng(olo);
		//setTeam(team);
	}
	/*
	public String toString() { 
		
		return this.getLine().toString();
		
		// return new String ( getoGuid() + " - " + getdGuid() + " : " + getTeam() );
		
	}
	 */
	
}

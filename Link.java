import java.util.HashMap;
import java.util.ArrayList;

public class Link extends Line {
	
	public static final int NEUTRAL = 0;
	public static final int ENLIGHTENED = 1;
	public static final int RESISTANCE = 2;
	
	protected String guid;
	protected String dGuid;
	
	protected String oGuid;
	protected String team;
	protected int teamEnum;

	public String getGuid() { return guid; }
	public String getdGuid() { return dGuid; }
	public String getoGuid() { return oGuid; }
	
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
		
	private void setTeam(String t) {
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
		this.guid = g;
		this.dGuid=dg;
		this.oGuid=og;
		setTeam(tt);

	}
	
	
	public Link (String g, String dg, Long dla,Long dlo, String og, Long ola,Long olo,String tt)
	{
		super(dla,dlo,ola,olo);
		guid = g;
		dGuid=dg;
		oGuid=og;
		setTeam(tt);
	}

	/*
	public String toString() { 
		
		return this.getLine().toString();
		
		// return new String ( getoGuid() + " - " + getdGuid() + " : " + getTeam() );
		
	}
	 */
	
}

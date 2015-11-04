import java.util.LinkedList;
import java.util.List;

public class teamCount {

	
	// should make team name an ENUM
	private Integer resistance = null;
	private Integer enlightened = null;
    private boolean exists = false;

	public static final int NEUTRAL = 0;
	public static final int ENLIGHTENED = 1;
	public static final int RESISTANCE = 2;

	public teamCount () { ; } 
	
// Alphabetical order
	public teamCount (Integer ENL, Integer RES) {
		resistance = RES;
		enlightened = ENL;
	}
	public teamCount (String ENL, String RES) {
		if (RES != null)
			resistance = new Integer(RES);
		else
			RES = null;
		if (ENL != null)
			enlightened = new Integer(ENL);
		else
			ENL = null;
	}
	public teamCount (String[] args) {
		
		List<String> out = new LinkedList<String>();
	
		//enlightened = 0;
		//resistance = 0;

		for (int c=0;c<args.length;c++)
		{
			String a = args[c];
			if ("-E".equals(a)) {
				enlightened = Integer.parseInt(args[c+1]);
				c++;
			}
			else if ("-R".equals(a)) {
				resistance = Integer.parseInt(args[c+1]);
				c++;
			} else {
				out.add(a);
			}
		}
		
		out.toArray(args);
		
	}
	
	public void incTeamEnum(int team) {
	
		if (team == ENLIGHTENED) {
			if (enlightened != null)
				enlightened ++;
			else 
				enlightened = 1;
		}
		if (team == RESISTANCE) {
			if (resistance != null)
				resistance ++;
			else 
				resistance = 1;
		}
		
	}
	
	public void incTeam(String team) {
		if (team.startsWith("R")) {
			resistance ++;
		}
        if (team.startsWith("E")) {
			enlightened ++;
		}
	}
	
	public Integer getResistance() { return resistance; }
	public Integer getEnlightened() { return enlightened; }

	public int getResistanceAsInt() { 
		if (resistance == null) return 0;
		return resistance; 
	}
	public int getEnlightenedAsInt() { 
		if (enlightened == null) return 0;
		return enlightened; 
	}
	
	
	public void setResistance(Integer i) { resistance=i; } 
	public void setEnlightened(Integer i) { enlightened=i; } 

	public boolean noResistance() { return resistance == null; }
	public boolean noEnlightened() { return enlightened == null; }
	
    public boolean getExists() { return exists; }
    public void setExists(boolean b) { exists = b; }
	
	public boolean moreThan(teamCount tc) {
		// should check for null == don't care
		boolean res = false;
		boolean enl = false;

		if (!tc.noEnlightened() && !noEnlightened())
			enl = getEnlightened() > tc.getEnlightened();
		if (!tc.noResistance() && !noResistance())
			res = getResistance() > tc.getResistance();
			
		return res || enl;
			
		
	}
	public boolean anyEnlightenedBlockers() { return enlightened > 0; }
	public boolean anyResistanceBlockers() { return resistance > 0; }
	public boolean anyBlockers() { return resistance >0 || enlightened>0; }
	
    public boolean dontCare() { return resistance == null && enlightened == null; }
    
	public String toString() { return ("E:" + enlightened + ", R:" + resistance); }
	
}


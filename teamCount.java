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
			enlightened ++;
		}
		if (team == RESISTANCE) {
			resistance ++;
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
	
	public void setResistance(Integer i) { resistance=i; } 
	public void setEnlightened(Integer i) { enlightened=i; } 

	public boolean noResistance() { return resistance == null; }
	public boolean noEnlightened() { return enlightened == null; }
	
    public boolean getExists() { return exists; }
    public void setExists(boolean b) { exists = b; }
	
	public boolean moreThan(teamCount tc) {
		// should check for null == don't care
		
		if (tc.noEnlightened() && tc.noResistance()) return false;
		if (tc.noEnlightened() && !tc.noResistance()) return getResistance() > tc.getResistance();
		if (!tc.noEnlightened() && tc.noResistance()) return getEnlightened() > tc.getEnlightened();
		
		return getResistance() > tc.getResistance() || getEnlightened() > tc.getEnlightened();
	}
	public boolean anyEnlightenedBlockers() { return enlightened > 0; }
	public boolean anyResistanceBlockers() { return resistance > 0; }
	public boolean anyBlockers() { return resistance >0 || enlightened>0; }
	
    public boolean dontCare() { return resistance == null && enlightened == null; }
    
	public String toString() { return ("E:" + enlightened + ", R:" + resistance); }
	
}


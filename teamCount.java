import java.util.LinkedList;
import java.util.List;

public class teamCount {
	
	// should make team name an ENUM
	private Integer resistance = null;
	private Integer enlightened = null;
	private Integer neutral = null;
	private boolean exists = false;

	public static final int NEUTRAL = 0;
	public static final int ENLIGHTENED = 1;
	public static final int RESISTANCE = 2;

	public teamCount () { ; } 
	
	// Alphabetical order
	public teamCount (Integer ENL, Integer RES, Integer NEU) {
		resistance = RES;
		enlightened = ENL;
		neutral = NEU;
	}
	public teamCount (String ENL, String RES, String NEU) {
		if (RES != null)
			resistance = new Integer(RES);
		else
			resistance = null;
		if (ENL != null)
			enlightened = new Integer(ENL);
		else
			enlightened = null;
		if (NEU != null)
			neutral= new Integer(NEU);
		else
			neutral=null;
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
			}
			else if ("-N".equals(a)) {
				neutral = Integer.parseInt(args[c+1]);
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
		if (team == NEUTRAL)
			if (neutral != null)
				neutral ++;
			else
				neutral = 1;
		
	}
	
	public void incTeam(String team) {
		if (team.startsWith("R")) { incTeamEnum(RESISTANCE); }
		if (team.startsWith("E")) { incTeamEnum(ENLIGHTENED); }
		if (team.startsWith("N")) { incTeamEnum(NEUTRAL); }

	}
	
	public Integer getResistance() { return resistance; }
	public Integer getEnlightened() { return enlightened; }
	public Integer getNeutral() { return neutral; }

	public int getResistanceAsInt() { 
		if (resistance == null) return 0;
		return resistance; 
	}
	public int getEnlightenedAsInt() { 
		if (enlightened == null) return 0;
		return enlightened; 
	}
	public int getNeutralAsInt() { 
		if (neutral == null) return 0;
		return neutral; 
	}
	
	public void setResistance(Integer i) { resistance=i; } 
	public void setEnlightened(Integer i) { enlightened=i; } 

	public void setNeutral(Integer i) { neutral=i; } 

	public void addResistance(Integer i) { addTeamEnum(RESISTANCE,i); } 
	public void addEnlightened(Integer i) { addTeamEnum(ENLIGHTENED,i); } 
	public void addNeutral(Integer i) { addTeamEnum(NEUTRAL,i); } 

	public void addTeamEnum(int team, Integer i) { 

		if (i != null) {
			if (team == ENLIGHTENED) {
				if (enlightened != null)
					enlightened += i;
				else
					enlightened = i;
			}
			if (team == RESISTANCE) {
				if (resistance != null)
					resistance += i;
				else
					resistance = i;
			}
			if (team == NEUTRAL) {
				if (neutral != null)
					neutral += i;
				else
					neutral = i;
			}
		}

        }

	public boolean noResistance() { return resistance == null; }
	public boolean noEnlightened() { return enlightened == null; }
	public boolean noNeutral() { return neutral == null; }

	public boolean getExists() { return exists; }
	public void setExists(boolean b) { exists = b; }
	
	public boolean moreThan(teamCount tc) {
		// should check for null == don't care
		boolean res = false;
		boolean enl = false;
		boolean neu = false;

		if (!tc.noEnlightened() && !noEnlightened())
			enl = getEnlightened() > tc.getEnlightened();
		if (!tc.noResistance() && !noResistance())
			res = getResistance() > tc.getResistance();
		if (!tc.noNeutral() && !noNeutral())
			neu = getNeutral() > tc.getNeutral();

		return res || enl || neu;
			
		
	}
	public boolean anyEnlightenedBlockers() { return enlightened > 0; }
	public boolean anyResistanceBlockers() { return resistance > 0; }
	public boolean anyNeutralBlockers() { return neutral > 0; }

	public boolean anyBlockers() { return resistance >0 || enlightened>0 || neutral >0; }
	
	public boolean dontCare() { return resistance == null && enlightened == null && neutral==null; }
    
	public String toString() { return ("E:" + enlightened + ", R:" + resistance + ", N:"+neutral); }
	
}


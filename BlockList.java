import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class BlockList {

	HashMap<String,teamCount> blocksPerLink;
	
	public BlockList () {
		blocksPerLink = new HashMap<String,teamCount>();
	}
    
    protected static String orderKeys (String si, String sj)
    {
        int lex =si.compareTo(sj);
        if ( lex < 0) {
            return si + "," + sj;
        } else if (lex == 0) {
            return si;
        }
        return sj + "," + si;
    }
    
	public teamCount put (Portal pi, Portal pj, teamCount bb)
	{
            return blocksPerLink.put( orderKeys(pi.getGuid(), pj.getGuid()), bb);
	}
	
	public teamCount get (Portal p1, Portal p2)
	{
        
        String key = orderKeys(p1.getGuid(), p2.getGuid());
        
		if (blocksPerLink.containsKey( key ))
		{
			return blocksPerLink.get (key);
		}
		return null;
	}
    
    public Set<String> keySet()
    {
        return blocksPerLink.keySet();
    }
    
    public void remove (String si)
    {
        blocksPerLink.remove(si);
    }
    
    public void remove (Portal p1, Portal p2)
    {
        blocksPerLink.remove(orderKeys(p1.getGuid(), p2.getGuid()));
    }
    
    public boolean containsKey (Portal p1, Portal p2)
    {
        return blocksPerLink.containsKey( orderKeys(p1.getGuid(), p2.getGuid()));
    }
	
    public boolean isEmpty() {
        return blocksPerLink.isEmpty();
    }
    
    public int size () {
        return blocksPerLink.size();
    }
    
    public void incTeamEnum(Portal p1, Portal p2, int team) {
        teamCount tc = get(p1,p2);
        if (tc != null) {
            get(p1,p2).incTeamEnum(team);
        } else {
            put(p1,p2,new teamCount());
            get(p1,p2).incTeamEnum(team);
        }
    }
    
    public void setExists(Portal p1, Portal p2, boolean b)
    {
        get(p1,p2).setExists(b);
    }

    public boolean getExists(Portal p1, Portal p2)
    {
        return get(p1,p2).getExists();
    }
    
    public Set<Map.Entry<String,teamCount>> entrySet() {
        return blocksPerLink.entrySet();
    }
    
}
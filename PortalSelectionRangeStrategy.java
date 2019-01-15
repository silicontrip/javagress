import java.util.Map;
import java.util.HashMap;
public class PortalSelectionRangeStrategy extends PortalSelectionStrategy  {

	private Point location;
	private Float range;
	
	public PortalSelectionRangeStrategy (Point l, Float r)
	{
		location = l;
		range = r;
	}
	
	public  boolean match ( HashMap<String,Object> portal)
	{
		Point pp = getPointFromPortal(portal);
		
		Line ll = new Line(location,pp);
		return ll.getGeoDistance() <= range;
	}
}
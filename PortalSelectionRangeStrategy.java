import java.util.Map;
import java.util.HashMap;
public class PortalSelectionRangeStrategy implements PortalSelectionStrategy  {

	private Point location;
	private Float range;
	
	public PortalSelectionRangeStrategy (Point l, Float r)
	{
		location = l;
		range = r;
	}
	
	public  boolean match ( HashMap<String,Object> portal)
	{
		Point pp = new Point (((Integer)portal.get("lat")).longValue(),((Integer)portal.get("lng")).longValue());
		Line ll = new Line(location,pp);
		return ll.getGeoDistance() <= range;
	}
}
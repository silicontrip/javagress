import java.util.Map;
import java.util.HashMap;

public abstract class PortalSelectionStrategy {

	public abstract boolean match(HashMap<String,Object> portal);

	public Point getPointFromPortal(HashMap<String,Object> portal)
	{
		return new Point (((Integer)portal.get("lat")).longValue(),((Integer)portal.get("lng")).longValue());
	}
}




import java.util.Map;
import java.util.HashMap;
public class PortalSelectionBoxStrategy extends PortalSelectionStrategy  {

	private Long minlat;
	private Long maxlat;
	private Long minlng;
	private Long maxlng;
	
	public PortalSelectionBoxStrategy (Point l1, Point l2)
	{

		minlat = l1.getLatE6();
		if (minlat > l2.getLatE6()) minlat = l2.getLatE6();
		
		maxlat = l1.getLatE6();
		if (maxlat < l2.getLatE6()) maxlat = l2.getLatE6();
		
		
		minlng = l1.getLngE6();
		if (minlng > l2.getLngE6()) minlng = l2.getLngE6();

		maxlng = l1.getLngE6();
		if (maxlng < l2.getLngE6()) maxlng = l2.getLngE6();

		
	}
	
	public  boolean match ( HashMap<String,Object> portal)
	{
		Integer lat = (Integer)portal.get("lat");
		Integer lng = (Integer)portal.get("lng");

		return  lat <= maxlat &&  lat >= minlat && lng <= maxlng && lng >= minlng;
	}
}

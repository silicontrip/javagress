import java.util.Map;
import java.util.HashMap;
public class PortalSelectionBoxStrategy extends PortalSelectionStrategy  {

	private Long minlat;
	private Long maxlat;
	private Long minlng;
	private Long maxlng;
	
	public PortalSelectionBoxStrategy (Point l1, Point l2)
	{

		minlat = l1.getLat();
		if (minlat > l2.getLat()) minlat = l2.getLat();
		
		maxlat = l1.getLat();
		if (maxlat < l2.getLat()) maxlat = l2.getLat();
		
		
		minlng = l1.getLng();
		if (minlng > l2.getLng()) minlng = l2.getLng();

		maxlng = l1.getLng();
		if (maxlng < l2.getLng()) maxlng = l2.getLng();

		
	}
	
	public  boolean match ( HashMap<String,Object> portal)
	{
		Integer lat = (Integer)portal.get("lat");
		Integer lng = (Integer)portal.get("lng");

		return  lat <= maxlat &&  lat >= minlat && lng <= maxlng && lng >= minlng;
	}
}

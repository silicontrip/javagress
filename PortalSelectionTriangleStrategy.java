import java.util.Map;
import java.util.HashMap;
public class PortalSelectionTriangleStrategy extends PortalSelectionStrategy  {

	private Field field;
	
	public PortalSelectionTriangleStrategy (Point l1, Point l2,Point l3)
	{
		field = new Field (l1,l2,l3);
	}
	
	public  boolean match ( HashMap<String,Object> portal)
	{
		
		Point pp = getPointFromPortal(portal);

		return  field.inside(pp);
	}
}

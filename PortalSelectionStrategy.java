import java.util.Map;
import java.util.HashMap;

interface PortalSelectionStrategy {

	public abstract boolean match(HashMap<String,Object> portal);

}




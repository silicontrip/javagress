import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import com.google.common.geometry.*;

public class cellfields {

	private static String drawFields(List<Field> fa,DrawTools dt)
	{
		
		dt.erase();
		
		for (Field fi: fa)
			dt.addField(fi);
		
		return dt.out();
		
	}
	
// specify Cell to generate for
// generate fields at range km
// if field.count < threshold { range++ }
// foreach field
// get cell mu intersection
// determine error of cell of interest vs error of other cells.
// pick the best ratio

public static void main(String[] args) {

	int calc=0;
	ArrayList<Point> target=null;
	S2CellId cellid;
	
	Arguments ag = new Arguments(args);
	
	teamCount maxBl = new teamCount(ag.getOptionForKey("E"),ag.getOptionForKey("R"));
	
	DrawTools dt = new DrawTools();
	if (ag.hasOption("C"))
		dt.setDefaultColour(ag.getOptionForKey("C"));
	else
		dt.setDefaultColour("#a24ac3");

	int range = 1;
	if (ag.hasOption("r"))
		range = (new Integer(ag.getOptionForKey("r"))).intValue();
		


    if (ag.hasOption("L"))
        dt.setOutputAsPolyline();
    if (ag.hasOption("O"))
        dt.setOutputAsIntel();

	try {
        PortalFactory pf = PortalFactory.getInstance();
		
		String id = new String ("0x" + ag.getArgumentAt(0));
		cellid = new S2CellId(Long.decode(id) << 32);
		System.out.println("Cell selected: " + cellid.toToken());

		CellServer cs = new CellServer();

		S2Cell cell = new S2Cell(cellid);

		S2LatLng loc = new S2LatLng(cell.getCenter());

    	System.err.println("== Reading portals ==");
    	HashMap<String,Portal> portals = new HashMap<String,Portal>();

		while (portals.size() < 3)
		{
    		portals = pf.portalClusterFromString("" + loc.latDegrees() + "," + loc.lngDegrees() + ":" + range);
		}	

        ArrayList<Link> links = pf.getPurgedLinks(portals.values());
		ArrayList<Line> li = pf.makeLinksFromSingleCluster(portals.values());
		// check that maxBl is set
		ArrayList<Line> l2 = pf.filterLinks(li,links,maxBl);
		ArrayList<Field> allfields = pf.makeFieldsFromSingleLinks(l2);

		for (Field fi: allfields) {
			// get mu cell intersection
			HashMap<S2CellId,Double> cellCover = fi.getCellIntersection();
			// determine error of cell of interest vs error of other cells.
		//	HashMap<S2CellId,UniformDistribution> error = new HashMap<S2CellId,UniformDistribution>();

			double thisError = 0;
			double otherError =0;

			double thisArea = 0;
			double thisPError = 0;
			double otherMU = 0;


			for(S2CellId lcell : cellCover.keySet())
			{

				double area = cellCover.get(lcell);
				UniformDistribution cmu = cs.getMU(lcell);
				if (cmu == null)
				{
					thisError = 0;
					break;
				}
				UniformDistribution mu = cmu.mul(area);

				//System.out.println("" + lcell.toToken() + ": " + cmu + " x " + area + " = " + mu);

				if (lcell.toToken().equals(ag.getArgumentAt(0)))
				{
					thisArea = area;
				}
				else
					otherMU += mu.mean();

				// want to reduce area in non selected cells.
				mu = mu.mul(area);
				if (lcell.toToken().equals(ag.getArgumentAt(0)))
					thisError = mu.error();
				else
					otherError += mu.error();
				//error.put(lcell,mu);
			}
			

			//if (otherError==0) 
			//	otherError = 1E-64;


			double ratio = thisError / otherError;
			if (thisError > 0)	
			{
				if (ratio < 100.0)
				{
				String mumu = String.format("%.12f",(ratio*thisArea));
				String muratio = String.format("%.12f", ratio);
				String mustr = String.format("%.12f", thisArea);
				System.out.println(mumu + " " + mustr + " " + muratio +  " : [" + fi  + "]");
				}
			}
			//}
		
		}
		
		//System.out.println("Total Fields: " + fiList.size());

			// sort through colliding fields.
		
		//  searchFields(dt, new ArrayList<Field>() , fiList.toArray(),0,0.0,0);
		
        
    } catch (Exception e) {
        
        System.out.print ("Exception: ");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
	
}

}

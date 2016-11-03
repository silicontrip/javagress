import com.google.common.geometry.*;
import java.util.*;
import javax.xml.parsers.*;
import java.io.*;
import java.beans.*;
import java.net.*;

public class CellMUDB {


	static CellMUDB instance = null;
	HashMap<String,Double> mudb = null;
	S2RegionCoverer rc;
	int maxLevel;



	public static CellMUDB getInstance() throws ParserConfigurationException, IOException {
		if (instance == null) {
			instance = new CellMUDB();
		}
		return instance;
	}

	private void printCell(S2Cell cell) {

		// quick and dirty drawtools output
		S2LatLng p0 = new S2LatLng(cell.getVertex(0));
		S2LatLng p1 = new S2LatLng(cell.getVertex(1));
		S2LatLng p2 = new S2LatLng(cell.getVertex(2));
		S2LatLng p3 = new S2LatLng(cell.getVertex(3));

		System.out.print("[{\"type\":\"polygon\",\"color\":\"#ffffff\",\"latLngs\":[");
		System.out.print("{\"lat\": " + Double.toString(p0.latDegrees()) + ",\"lng\": " + Double.toString(p0.lngDegrees()) + "},");
		System.out.print("{\"lat\": " + Double.toString(p1.latDegrees()) + ",\"lng\": " + Double.toString(p1.lngDegrees()) + "},");
		System.out.print("{\"lat\": " + Double.toString(p2.latDegrees()) + ",\"lng\": " + Double.toString(p2.lngDegrees()) + "},");
		System.out.print("{\"lat\": " + Double.toString(p3.latDegrees()) + ",\"lng\": " + Double.toString(p3.lngDegrees()) + "}");
		System.out.println("]}]");

	}


	public CellMUDB() throws ParserConfigurationException, IOException {

		int minLevel,maxCells,levelMod;
		String mudbUrl;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		Properties fileProperties = new Properties();

		FileInputStream fis = new FileInputStream(new File("portalfactory.properties"));
		fileProperties.load (fis);
		fis.close();

		mudbUrl = fileProperties.getProperty("mudburl");

		maxLevel = Integer.parseInt( fileProperties.getProperty("maxlevel"));
		minLevel = Integer.parseInt( fileProperties.getProperty("minlevel"));
		maxCells = Integer.parseInt( fileProperties.getProperty("maxcells"));
		levelMod = Integer.parseInt( fileProperties.getProperty("levelmod"));


		rc = new S2RegionCoverer();
                rc.setMaxLevel(maxLevel);
                rc.setMinLevel(minLevel);
                rc.setMaxCells(maxCells);
		rc.setLevelMod(levelMod);


		DocumentBuilder db = dbf.newDocumentBuilder();

		URL xmlURL = new URL(mudbUrl);

		XMLDecoder d = new XMLDecoder(new BufferedInputStream(xmlURL.openStream()));
		mudb  = (HashMap<String,Double>) d.readObject();
		d.close();

	}

	private double getMUKM (S2CellId s2id) {
		
		if (mudb.containsKey(s2id.toToken())) {
			return  mudb.get(s2id.toToken()).doubleValue();
		} else {
			if (s2id.level() < maxLevel) {
			// sub divide cell
			//System.out.println(s2id.level());
			//System.out.println("divide");

				S2CellId id = s2id.childBegin();
				double ttmu = 0;
				for (int pos = 0; pos < 4; ++pos, id = id.next()) {
				//System.out.println(id.toToken());
					ttmu += getMUKM(id);
				}
				mudb.put(s2id.toToken(), ttmu/4.0);
				return ttmu / 4.0;

			}
			String s2tok = s2id.toToken();
			System.out.print(s2tok + " ");
			printCell(new S2Cell(s2id));
			mudb.put(s2tok,1.0);
			return 1.0;
		}
			

	}

	public double getEstMu(Field f) {

                double ttmu=0;
                double area;
                double mukm;

                S2Polygon thisField = getS2Polygon(f);

                for (S2CellId cell: getS2CellUnion(f)) {

                        S2Cell s2cell = new S2Cell(cell);
                        S2Polygon intPoly = new S2Polygon();
                        S2Polygon cellPoly = polyFromCell(s2cell);

                        intPoly.initToIntersection(thisField, cellPoly);
                        area = intPoly.getArea() * 6371 * 6371 ;

                        // get mu for cellid
                        //mudb.getHash().put(cell.toToken(),new Double(mukm));
                        mukm = getMUKM(cell);

                        ttmu += area * mukm;

                }
                return ttmu;
        }


        private static S2Polygon polyFromCell (S2Cell cell)
        {
                S2PolygonBuilder pb = new S2PolygonBuilder(S2PolygonBuilder.Options.UNDIRECTED_UNION);
                pb.addEdge(cell.getVertex(0),cell.getVertex(1));
                pb.addEdge(cell.getVertex(1),cell.getVertex(2));
                pb.addEdge(cell.getVertex(2),cell.getVertex(3));
                pb.addEdge(cell.getVertex(3),cell.getVertex(0));
                return pb.assemblePolygon();

        }

        private S2CellUnion getS2CellUnion(Field f) {
                return rc.getCovering(getS2Polygon(f));
        }

        public S2Polygon getS2Polygon(Field f) {
                S2PolygonBuilder pb = new S2PolygonBuilder(S2PolygonBuilder.Options.UNDIRECTED_UNION);
                pb.addEdge(getS2LatLng(f,0).toPoint(),getS2LatLng(f,1).toPoint());
                pb.addEdge(getS2LatLng(f,1).toPoint(),getS2LatLng(f,2).toPoint());
                pb.addEdge(getS2LatLng(f,2).toPoint(),getS2LatLng(f,0).toPoint());

                return pb.assemblePolygon();

	}

	private S2LatLng getS2LatLng(Field f, int index) { return S2LatLng.fromE6(f.getLat(index),f.getLng(index)); }

}

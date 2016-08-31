import com.google.common.geometry.*;
import java.util.*;
import javax.xml.parsers.*;
import java.io.*;
import java.beans.*;
import java.net.*;

public class CellMUDB {


        static CellMUDB instance = null;
	HashMap<String,Double> mudb = null;
	HashSet<String> seen = null;


        public static CellMUDB getInstance() {
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


	public CellMUDB() {


               DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                try {

                        DocumentBuilder db = dbf.newDocumentBuilder();

				URL xmlURL = new File("mudb.xml").toURI().toURL();
        
			XMLDecoder d = new XMLDecoder(new BufferedInputStream(xmlURL.openStream()));
			mudb  = (HashMap<String,Double>) d.readObject();
			d.close();
                        
                        
                } catch (ParserConfigurationException pce) {
                        System.out.println("Parser Configuration Error " + pce);
                } catch (IOException ioe) {
                        System.out.println("CardFactory: IO Error " + ioe);
                }

//		mudb = new HashMap<String,Double>();
		seen = new HashSet<String>();

	}

	public double getMUKM (S2CellId s2id) {
		

		if (mudb.containsKey(s2id.toToken())) {
			return  mudb.get(s2id.toToken()).doubleValue();
		} else {
			if (s2id.level() < 13) {
			// sub divide cell
			//System.out.println(s2id.level());
			//System.out.println("divide");

				S2CellId id = s2id.childBegin();
				double ttmu = 0;
				for (int pos = 0; pos < 4; ++pos, id = id.next()) {
				//System.out.println(id.toToken());
					ttmu += getMUKM(id);
				}
				mudb.put(s2id.toToken, ttmu/4.0);
				return ttmu / 4.0;

			}
			String s2tok = s2id.toToken();
			System.out.print(s2tok + " ");
			printCell(new S2Cell(s2id));
			mudb.put(s2tok,0);
			return 0;
		}
			

	}

//	public HashMap<String,Double> getHash() { return mudb; }

	public void write() {
	                try {
                        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("mudb.xml")));
                        e.writeObject(mudb);
                        e.close();
                } catch (FileNotFoundException fnfe) {
                        System.out.println("deckfactory: file not found " + fnfe);
                }
	}


}

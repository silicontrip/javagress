import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.*;
import com.google.common.geometry.*;

public class CellServer
    implements Runnable
{
    private final Socket m_socket;
    private final int m_num;
    JSONObject m_mudb;

        private static S2LatLng locationToS2 (JSONObject loc) throws NumberFormatException
        {
                if (loc.has("lat") && loc.has("lng")) {
                        Double lat = loc.getDouble("lat");
                        Double lng = loc.getDouble("lng");
                       
                        return S2LatLng.fromDegrees(lat,lng);
                }

                throw new NumberFormatException("Object doesn't contain lat/lng");
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

	CellServer(JSONObject jo) { m_socket = null; m_num=0; m_mudb=jo;  } 

    CellServer( Socket socket, int num, JSONObject jo )
    {
        m_socket = socket;
        m_num = num;
        m_mudb = jo;

        Thread handler = new Thread( this, "handler-" + m_num );
        handler.start();
    }
    
	public JSONObject cellalize (JSONArray  dtobj)
	{
		JSONObject field = dtobj.getJSONObject(0);
		JSONObject response = new JSONObject();

		if (field.has("latLngs"))
		{
			JSONArray latlng = field.getJSONArray("latLngs");
			if (latlng.length() == 3)
			{
                                    //System.out.println( m_num + " JSON: " + latlng );

                                    S2LatLng v1 = locationToS2(latlng.getJSONObject(0)); 
                                    S2LatLng v2 = locationToS2(latlng.getJSONObject(1)); 
                                    S2LatLng v3 = locationToS2(latlng.getJSONObject(2)); 

                                    S2PolygonBuilder pb = new S2PolygonBuilder(S2PolygonBuilder.Options.UNDIRECTED_UNION);
                                    pb.addEdge(v1.toPoint(),v2.toPoint());
                                    pb.addEdge(v2.toPoint(),v3.toPoint());
                                    pb.addEdge(v3.toPoint(),v1.toPoint());
                                    S2Polygon thisField = pb.assemblePolygon();
                                    S2RegionCoverer rc = new S2RegionCoverer();
                                    // ingress mu calculation specifics
                                    rc.setMaxLevel(13);
                                    rc.setMinLevel(0);
                                    rc.setMaxCells(20);
                                    S2CellUnion cells = rc.getCovering (thisField);
                                    for (S2CellId cell: cells) 
                                    {
                                        S2Polygon intPoly = new S2Polygon();
                                        S2Polygon cellPoly = polyFromCell(new S2Cell(cell));

                                        intPoly.initToIntersection(thisField, cellPoly);
                                        double area = intPoly.getArea() * 6367 * 6367 ;
                                        JSONObject cellinf  ;
                                        if (m_mudb.has(cell.toToken()))
					{
						JSONArray lowhi = m_mudb.getJSONArray(cell.toToken());
                                            cellinf = new JSONObject();
                                            cellinf.put("mu_min",lowhi.get(0));
                                            cellinf.put("mu_max",lowhi.get(1));
					}
                                        else
                                            cellinf = new JSONObject();

                                        cellinf.put("area",area);
                                    
                                        response.put(cell.toToken(),cellinf);
				}
			}
		}
		return response;
	}

    public void run()
    {
        try
        {
            try
            {
                System.out.println( m_num + " Connected." );
                BufferedReader in = new BufferedReader( new InputStreamReader( m_socket.getInputStream() ) );
                OutputStreamWriter out = new OutputStreamWriter( m_socket.getOutputStream() );
                //out.write( "Welcome connection #" + m_num + "\n\r" );
                //out.flush();
                
                    JSONObject response = new JSONObject();

                    String line = in.readLine();
                    if ( line == null )
                    {
                        System.out.println( m_num + " Closed." );
                        return;
                    }
                    else
                    {
                        JSONArray dtobj = new JSONArray(line); 
                        //System.out.println( m_num + " JSON: " + dtobj );
                        if ( dtobj.length()==0 )
                        {
                            System.out.println( m_num + " Closing Connection." );
                            return;
                        }
                        else
                        {
				response = cellalize(dtobj);

                        }
                    }
                    out.write( "" + response + "\n\r" );
                    out.flush();
            }
            finally
            {
                m_socket.close();
            }
        }
        catch ( IOException e )
        {
            System.out.println( m_num + " Error: " + e.toString() );
        }
    }
    
    public static void main( String[] args )
        throws Exception
    {
        int port = 8080;
        if ( args.length > 0 )
        {
            port = Integer.parseInt( args[0] );
        }

        BufferedReader reader = new BufferedReader(new FileReader("mudb.json"));
        String line = reader.readLine();
        JSONObject mudb = new JSONObject(line);
        reader.close();

        //System.out.println("JSON: " + mudb);
        System.out.println( "Accepting connections on port: " + port );

        int nextNum = 1;
        ServerSocket serverSocket = new ServerSocket( port );
        while ( true )
        {
            Socket socket = serverSocket.accept();
            CellServer hw = new CellServer( socket, nextNum++,mudb );
        }
    }
}


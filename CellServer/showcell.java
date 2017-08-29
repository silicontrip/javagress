import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import org.json.*;
import com.google.common.geometry.*;

public class showcell
{

    public static void main(String[] args)
    {
        try
        {
		BufferedReader reader = new BufferedReader(new FileReader("res50.json"));
		String line = reader.readLine();
		JSONObject mudb = new JSONObject(line);
		reader.close();
		 	CellServer cs = new CellServer(mudb);
                
                    JSONObject response = new JSONObject();

		for(String data: args) {
			JSONArray dtobj = new JSONArray(data); 
                        //System.out.println( m_num + " JSON: " + dtobj );
                        if ( dtobj.length()==0 )
                        {
                            System.out.println( "No Fields." );
                            return;
                        }
                        else
                        {
				response = cs.cellalize(dtobj);

                        }
                    System.out.println( "" + response + "\n\r" );
			double min_mu = 0;
			double max_mu = 0;
			for (Iterator<String> id= response.keys(); id.hasNext();)
			{
				JSONObject cell = response.getJSONObject(id.next());
				//System.out.println(cell);
				min_mu += cell.getDouble("area") * cell.getDouble("mu_min");
				max_mu += cell.getDouble("area") * cell.getDouble("mu_max");
			}
			System.out.println ("MU: [" + min_mu + "," + max_mu + "] " + ((min_mu + max_mu) / 2 ) + " +/- " + (max_mu - min_mu) / (max_mu+min_mu) * 100 + "%");
		}
        }
        catch ( IOException e )
        {
            System.out.println( " Error: " + e.toString() );
        }
    }
    
}


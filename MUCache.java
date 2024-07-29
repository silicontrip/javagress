import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MUCache {

    private static final Logger logger = Logger.getLogger(MUCache.class.getName());

    // Cache to store MU values for S2 cell tokens
    private Map<String, UniformDistribution> muCache;
	private String apiEndpointUrl;
    private static MUCache instance;


    public static synchronized MUCache getInstance() throws java.io.IOException {
        if (instance == null) {
            instance = new MUCache();
        }
        return instance;
    }

    private MUCache() throws java.io.IOException {
        this.muCache = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() throws java.io.IOException {
	Properties prop = new Properties();
	    FileInputStream fis = new FileInputStream(new File("portalfactory.properties"));
	    prop.load (fis);
	    fis.close();

	    // will use file:// for cache
	    apiEndpointUrl = prop.getProperty("cellurl");
    }

    // Public method to query MU using cache for a single token
    public UniformDistribution queryMu(String s2CellToken) {
        // Check if the value is already in the cache
        if (muCache.containsKey(s2CellToken)) {
            return muCache.get(s2CellToken);
        }

        // If not in cache, query the servlet for the value
        Map<String, UniformDistribution> result = queryMuFromServlet(new String[]{s2CellToken});

        // Retrieve the value from the result map
        UniformDistribution muValue = result.get(s2CellToken);

        // Cache the retrieved value
        if (muValue != null) {
            muCache.put(s2CellToken, muValue);
        }

        return muValue;

    }

	// Public method to query MU using cache for an array of tokens
	public HashMap<String, UniformDistribution> queryMu(String[] s2CellTokens) {
        HashMap<String, UniformDistribution> result = new HashMap<>();

        // List to collect tokens that need to be queried from servlet
        List<String> tokensToQuery = new ArrayList<>();

        // Step 1: Retrieve cached MU values
        for (String token : s2CellTokens) {
            if (muCache.containsKey(token)) {
                // If value exists in cache, retrieve it
                result.put(token, muCache.get(token));
            } else {
                // If not in cache, add to tokensToQuery list
                tokensToQuery.add(token);
            }
        }

        // Step 2: Query MU values from servlet for tokens not in cache
        if (!tokensToQuery.isEmpty()) {
            Map<String, UniformDistribution> servletMuValues = queryMuFromServlet(tokensToQuery.toArray(new String[0]));

            // Step 3: Cache retrieved MU values
            for (Map.Entry<String, UniformDistribution> entry : servletMuValues.entrySet()) {
                String token = entry.getKey();
                UniformDistribution muValue = entry.getValue();

                // Cache the retrieved value
                muCache.put(token, muValue);

                // Add to the result map
                result.put(token, muValue);
            }
        }

        return result;
    }

    // Helper method to parse UniformDistribution from JSON array
    private UniformDistribution parseUniformDistribution(JSONArray jsonArray) {
        // Implement parsing logic based on your API response format
        if (jsonArray.length() == 2) {
            double lower = jsonArray.getDouble(0);
            double upper = jsonArray.getDouble(1);
            return new UniformDistribution(lower, upper);
        }
        return null; // Handle parsing errors
    }

    // Helper method to parse MU values from API response into UniformDistribution objects
    private Map<String, UniformDistribution> parseMuResponse(JSONObject jsonResponse) {
        Map<String, UniformDistribution> muValues = new HashMap<>();

        // Example parsing logic, adjust as per your API response format
        for (String token : jsonResponse.keySet()) {
            JSONArray muArray = jsonResponse.getJSONArray(token);
            UniformDistribution muValue = parseUniformDistribution(muArray);
            if (muValue != null) {
                muValues.put(token, muValue);
            }
        }

        return muValues;
    }

    private Map<String, UniformDistribution> queryMuFromServlet(String[] s2CellTokens) {
        try {

            // Convert s2CellTokens to a JSON array
            JSONArray jsonArray = new JSONArray(Arrays.asList(s2CellTokens));

            // Construct URL with JSON array as a query parameter
            StringBuilder urlBuilder = new StringBuilder(apiEndpointUrl);
            urlBuilder.append("?mu=").append(jsonArray.toString());
        

		//System.out.println(urlBuilder.toString());
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            StringBuilder response = new StringBuilder();
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

                //System.out.println(response.toString());

            conn.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());


            // Assuming the response is in a format that can be parsed into UniformDistribution for each token
            Map<String, UniformDistribution> muValues = parseMuResponse(jsonResponse);


            // Cache the results
            muCache.putAll(muValues);

            return muValues;

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., log error, throw custom exception)
            return null; // or handle error condition appropriately
        }
    }


}


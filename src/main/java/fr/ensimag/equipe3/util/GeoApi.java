package fr.ensimag.equipe3.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ensimag.equipe3.model.City;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class GeoApi {
    private static final String baseUrl = "https://geo.api.gouv.fr/communes?";

    public static ArrayList<City> getCitiesFromPostalCode(int postalCode) throws Exception {
        String fullUrl = baseUrl + "codePostal=";
        if (postalCode < 10000)
            fullUrl += "0" + postalCode;
        else
            fullUrl += postalCode;
        String json = makeRequest(fullUrl);
        JsonElement tree = JsonParser.parseString(json);

        ArrayList<City> cities = new ArrayList<>();

        JsonArray array = tree.getAsJsonArray();
        for (JsonElement object : array) {
            JsonObject cityObject = object.getAsJsonObject();
            String name = cityObject.get("nom").getAsString();
            City city = new City(name, postalCode);
            cities.add(city);
        }

        return cities;
    }

    private static String makeRequest(String url) throws Exception {
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream())
        );
        String inputline;
        StringBuffer response = new StringBuffer();

        while ((inputline = in.readLine()) != null) {
            response.append(inputline);
        }
        in.close();

        return response.toString();
    }

    public static City getCityByGPS(double lat, double lon) throws Exception {
        String baseSearchURL = "https://api-adresse.data.gouv.fr/reverse?";
        String fullUrl = baseSearchURL + "lon=" + lon + "&lat=" + lat;
        String json = makeRequest(fullUrl);
        JsonElement tree = JsonParser.parseString(json);

        JsonObject object = tree.getAsJsonObject();
        JsonArray featuresArray = (JsonArray)(object.get("features"));
        JsonElement features = featuresArray.get(0);
        JsonObject featuresObject = features.getAsJsonObject();
        JsonObject properties = featuresObject.get("properties").getAsJsonObject();
        String name = properties.get("city").getAsString();
        String code = properties.get("postcode").getAsString();

        return new City(name, parseInt(code));
    }
}

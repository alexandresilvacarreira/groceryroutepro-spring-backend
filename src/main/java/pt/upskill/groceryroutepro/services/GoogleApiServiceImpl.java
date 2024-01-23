package pt.upskill.groceryroutepro.services;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.ClosestChainModel;
import pt.upskill.groceryroutepro.models.LatLng;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GoogleApiServiceImpl implements GoogleApiService {

    @Value("${google.api.key}")
    private String googleKey;


    private List<LatLng> decodePolyline(String encodedPolyline) {

        List<LatLng> polylinePoints = new ArrayList<>();
        int index = 0;
        int len = encodedPolyline.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;

            do {
                b = encodedPolyline.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1F);

            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;

            do {
                b = encodedPolyline.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1F);

            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            LatLng point = new LatLng(lat * 1e-5, lng * 1e-5);
            polylinePoints.add(point);
        }

        return polylinePoints;
    }

    @Override
    public String createRoute(LatLng partida, LatLng destino, User user) {
        //create route between partida and destino and return polyline
        List<String> result = new ArrayList<>();
        String polyline = this.createAToBRoute(partida, destino);
        System.out.println("Partida + Destino: " + polyline);
        //User get chain from shopping list
        // create new list of chains

        ArrayList<String> chains = new ArrayList<>();
        chains.add("auchan");
        chains.add("pingo doce");


        List<LatLng> routeCoordinates = decodePolyline(polyline);
        // ciclo forpara introduzir modulos

        //get the closest store locations based on the route submited by the user
        ClosestChainModel[] closestChainModels = findClosestLocation(routeCoordinates, chains, partida, destino, 3000);


        //create route based on the new waypoints and get polyline?


        return createRouteWithWaypoints(partida,destino,closestChainModels);
    }

    private ClosestChainModel[]  findClosestLocation(List<LatLng> routeCoordinates, ArrayList<String> chainlist, LatLng partida, LatLng destino, double radius) {
        boolean allPlacesFound = true;
        int count=1;
        ClosestChainModel[] closestChainArray = new ClosestChainModel[chainlist.size()];
        for (int i = 0; i < closestChainArray.length; i++) {
            ClosestChainModel closestChainModel = new ClosestChainModel(chainlist.get(i));
            closestChainArray[i]=(closestChainModel);
        }

        do {
            Boolean[] placeFound= new Boolean[closestChainArray.length];
            // Initialize booleanList with false values
            Arrays.fill(placeFound, false);


            //for each coordinate find the closest chain store
            int adder =routeCoordinates.size()/6;

            for (int i = 0; i < routeCoordinates.size(); i = i + adder) {
                for (int j = 0; j < closestChainArray.length; j++) {
                    //Coordinate from the route chosen by the user
                    LatLng newCoordinate = getPlaceCoordinate(closestChainArray[j].getChain(), routeCoordinates.get(i), radius);

                    if (newCoordinate != null) {
                        //calcular distancias das coordenadas à partida e ao destino
                        placeFound[j]=true;
                        double distPartida = calculateHaversineDistance(newCoordinate.getLat(), newCoordinate.getLng(), partida.getLat(), partida.getLng());
                        double distDestino = calculateHaversineDistance(newCoordinate.getLat(), newCoordinate.getLng(), destino.getLat(), destino.getLng());
                        double total = distPartida + distDestino;
                        if (closestChainArray[j].getDist()==0){
                            closestChainArray[j].setDist(total);
                            closestChainArray[j].setCoordinates(newCoordinate);
                        }
                        //if dist is smaller than this is teh new best location
                        if (total < closestChainArray[j].getDist()) {
                            closestChainArray[j].setDist(total);
                            closestChainArray[j].setCoordinates(newCoordinate);
                        }
                    }
                }
            }
            //loop to check if all places were found sets to true, if it finds 1 that isnt then change it to false
            allPlacesFound = true;
            checkloop:
            for (int i = 0; i < placeFound.length; i++) {

                if (!placeFound[i]) {
                    radius += 1000;
                    allPlacesFound = false;
                    break checkloop;

                }
            }

            //TODO APAGAR

            count+=1;
            System.out.println(count);


        }   while (!allPlacesFound && radius <= 7000);
        if (radius > 7000) {
            // no places found in the 7 km range
            //TODO fazer qualquer coisa BAD REQUEST?
            String out = "não encontrou";
            System.out.println(out);


        }
        return closestChainArray;


    }

    private String createAToBRoute(LatLng partida, LatLng destino) {
        try {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");

            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                            partida.getLat() +
                            "," +
                            partida.getLng() +
                            "&destination=" +
                            destino.getLat() +
                            "," +
                            destino.getLng() +
                            "&key=" + googleKey)
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);

            String points = (String) ((Map<String, Object>) ((Map<String, Object>) ((List<Map<String, Object>>) responseMap.get("routes")).get(0)).get("overview_polyline")).get("points");

            return points;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String createRouteWithWaypoints(LatLng partida, LatLng destino, ClosestChainModel[] closestChainModels) {
        try {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");

            String waypoints ="";

            for (int i = 0; i < closestChainModels.length; i++) {
                waypoints+="|"+closestChainModels[i].getCoordinates().getLat()+","+
                        closestChainModels[i].getCoordinates().getLng();
            }

            //1st create url string
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                    partida.getLat() +
                    "," +
                    partida.getLng() +
                    "&destination=" +
                    destino.getLat() +
                    "," +
                    destino.getLng() +
                    "&waypoints=optimize:true"+
                    waypoints +
                    "&key=" + googleKey;

            System.out.println(url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);

            String polyline = (String) ((Map<String, Object>) ((Map<String, Object>) ((List<Map<String, Object>>) responseMap.get("routes")).get(0)).get("overview_polyline")).get("points");

            String replacedPolyline = polyline.replace("\\\\", "\\");

            return replacedPolyline;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LatLng getPlaceCoordinate(String chain, LatLng biasCoordinates, double radius) {
        try {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");

            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?" +
                            "input=" +
                            chain +
                            "&inputtype=textquery&" +
                            "locationbias=circle%3A" +
                            radius +
                            "%40" +
                            biasCoordinates.getLat() +
                            "%2C" +
                            biasCoordinates.getLng() +
                            "&fields=formatted_address%2Cname%2Crating" +
                            "%2Copening_hours%2Cgeometry" +
                            "&key=" + googleKey)

                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);



            // Access the "candidates" array
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

            // Assuming there's at least one candidate in the array
            if (!candidates.isEmpty()) {
                // Access the first candidate in the array
                Map<String, Object> candidate = candidates.get(0);


                // Access the "geometry" object
                Map<String, Object> geometry = (Map<String, Object>) candidate.get("geometry");


                // Access the "location" object
                Map<String,Object> locationMap =  (Map<String,Object>) geometry.get("location");
                Double lat = (Double) locationMap.get("lat");
                Double lng = (Double) locationMap.get("lng");

                LatLng location = new LatLng(lat, lng);

                return location;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in meters
        final double R = 6371000;

        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate distance
        double distance = R * c;

        return distance;
    }


}

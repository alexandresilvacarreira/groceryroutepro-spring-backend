package pt.upskill.groceryroutepro.services;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.entities.Route;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.models.ClosestChainModel;
import pt.upskill.groceryroutepro.models.CreateRouteModel;
import pt.upskill.groceryroutepro.models.LatLng;
import pt.upskill.groceryroutepro.models.LatLngName;
import pt.upskill.groceryroutepro.repositories.CheapestMarkerRepository;
import pt.upskill.groceryroutepro.repositories.FastestMarkerRepository;
import pt.upskill.groceryroutepro.repositories.RouteRepository;
import pt.upskill.groceryroutepro.repositories.UserRepository;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleApiServiceImpl implements GoogleApiService {

    @Value("${google.api.key}")
    private String googleKey;

    @Autowired
    UserService userService;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    CheapestMarkerRepository cheapestMarkerRepository;
    @Autowired
    FastestMarkerRepository fastestMarkerRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public List<CreateRouteModel> generateRoutes(LatLngName partida, LatLngName destino) {
        User user = userService.getAuthenticatedUser();

        if (user == null) throw new BadRequestException("Utilizador não autenticado");

        //por definição calculamos sempre o mais barato primeiro e depois o mais rappido


        ShoppingList shoppingList = user.getCurrentShoppingList();

        if (shoppingList == null) throw new BadRequestException("A tua lista de compras não tem produtos");
        List<String> cheapestChainsList = shoppingList.getCheapestProductQuantities().stream()
                .map(c -> c.getProduct().getChain().getName()).collect(Collectors.toList());

        List<String> uniqueCheapestChainList = new ArrayList<>();
        for (String cheapestChain : cheapestChainsList) {
            if (!uniqueCheapestChainList.contains(cheapestChain)) {
                uniqueCheapestChainList.add(cheapestChain);
            }
        }


        List<String> fastestChainsList = shoppingList.getFastestProductQuantities().stream()
                .map(c -> c.getProduct().getChain().getName()).collect(Collectors.toList());

        List<String> uniqueFastestChainList = new ArrayList<>();
        for (String fastestChain : fastestChainsList) {
            if (!uniqueFastestChainList.contains(fastestChain)) {
                uniqueFastestChainList.add(fastestChain);
            }
        }


        CreateRouteModel createdRouteCheapest = createRoute(partida, destino, uniqueCheapestChainList, shoppingList.getCheapestListCost());
        CreateRouteModel createdRouteFastest = createRoute(partida, destino, uniqueFastestChainList, shoppingList.getFastestListCost());



        List<CreateRouteModel> rotas = new ArrayList<>();
        rotas.add(createdRouteCheapest);
        rotas.add(createdRouteFastest);

        //saving route in repository
        Route route = new Route();
        //set user
        route.setUser(user);
        //set polylines
        route.setCheapestPolyline(createdRouteCheapest.getPolyline());
        route.setFastestPolyline(createdRouteFastest.getPolyline());
        //total time
        route.setTotalCheapestTime(createdRouteCheapest.getTotalTime());
        route.setTotalFastestTime(createdRouteFastest.getTotalTime());
        //total cost
        route.setTotalCheapestCost(createdRouteCheapest.getShoppingListCost());
        route.setTotalFastestCost(createdRouteFastest.getShoppingListCost());


        user.getRoutes().add(route);
        userRepository.save(user);
        routeRepository.save(route);

        //Markers cheapest
        for (int i = 0; i < createdRouteCheapest.getCoordenadasMarcadores().size(); i++) {
            CheapestMarker cheapestMarker = new CheapestMarker();
            cheapestMarker.setLat(createdRouteCheapest.getCoordenadasMarcadores().get(i).getLat());
            cheapestMarker.setLng(createdRouteCheapest.getCoordenadasMarcadores().get(i).getLng());
            cheapestMarker.setLabel(createdRouteCheapest.getCoordenadasMarcadores().get(i).getNameLocation());
            cheapestMarker.setRoute(route);

            cheapestMarkerRepository.save(cheapestMarker);
            route.getCheapestMarkers().add(cheapestMarker);
        }
        //markers fastest
        for (int i = 0; i < createdRouteFastest.getCoordenadasMarcadores().size(); i++) {
            FastestMarker fastestMarker = new FastestMarker();
            fastestMarker.setLat(createdRouteFastest.getCoordenadasMarcadores().get(i).getLat());
            fastestMarker.setLng(createdRouteFastest.getCoordenadasMarcadores().get(i).getLng());
            fastestMarker.setLabel(createdRouteFastest.getCoordenadasMarcadores().get(i).getNameLocation());
            fastestMarker.setRoute(route);

            fastestMarkerRepository.save(fastestMarker);
            route.getFastestMarkers().add(fastestMarker);
        }

        user.getRoutes().add(route);
        user.setCurrentRoute(route);
        routeRepository.save(route);
        userRepository.save(user);


        //TODO current user???? que isto?


        return rotas;


    }

    @Override
    public List<CreateRouteModel> getRoutes() {
        User user = userService.getAuthenticatedUser();

        if (user == null) throw new BadRequestException("Utilizador não autenticado");
        //get last
        Route routes = user.getCurrentRoute();

        if (routes == null) throw new BadRequestException("Não existem rotas criadas");
        List<CreateRouteModel> routesObject = new ArrayList<>();

        // getting chain ids
        ShoppingList shoppingList = user.getCurrentShoppingList();

        List<Long> cheapestChainsIdList = shoppingList.getCheapestProductQuantities().stream()
                .map(c -> c.getProduct().getChain().getId()).collect(Collectors.toList());
        List<String> cheapestChainsList = shoppingList.getCheapestProductQuantities().stream()
                .map(c -> c.getProduct().getChain().getName()).collect(Collectors.toList());

        List<Long> uniqueCheapestIdChainList = new ArrayList<>();
        List<String> uniqueCheapestChainList = new ArrayList<>();
        for (Long cheapestChain : cheapestChainsIdList) {
            if (!uniqueCheapestIdChainList.contains(cheapestChain)) {
                uniqueCheapestIdChainList.add(cheapestChain);
            }
        }
        for (String cheapestChain : cheapestChainsList) {
            if (!uniqueCheapestChainList.contains(cheapestChain)) {
                uniqueCheapestChainList.add(cheapestChain);
            }
        }

        List<Long> fastestChainsIdList = shoppingList.getFastestProductQuantities().stream()
                .map(c -> c.getProduct().getChain().getId()).collect(Collectors.toList());
        List<String> fastestChainsList = shoppingList.getFastestProductQuantities().stream()
                .map(c -> c.getProduct().getChain().getName()).collect(Collectors.toList());

        List<Long> uniqueFastestChainIdList = new ArrayList<>();
        List<String> uniqueFastestChainNameList = new ArrayList<>();
        for (Long fastestChain : fastestChainsIdList) {
            if (!uniqueFastestChainIdList.contains(fastestChain)) {
                uniqueFastestChainIdList.add(fastestChain);
            }
        }

        for (String fastestChain : fastestChainsList) {
            if (!uniqueFastestChainNameList.contains(fastestChain)) {
                uniqueFastestChainNameList.add(fastestChain);
            }
        }
        //cheapest








        //Cheapest route getting into dto
        ArrayList<LatLngName> cheapestMarkers = new ArrayList<>();
        for (int i = 0; i < routes.getCheapestMarkers().size(); i++) {

            LatLngName cheapestMarker = new LatLngName(routes.getCheapestMarkers().get(i).getLat(),
                    routes.getCheapestMarkers().get(i).getLng());
            cheapestMarker.setNameLocation(routes.getCheapestMarkers().get(i).getLabel());
            cheapestMarkers.add(cheapestMarker);
        }

        CreateRouteModel cheapestRoute = new CreateRouteModel(routes.getCheapestPolyline(),cheapestMarkers,
                routes.getTotalCheapestTime(),routes.getTotalCheapestCost());

        List<LatLng> cheapestVertices = decodePolyline(routes.getCheapestPolyline());
        cheapestRoute.setVertices(cheapestVertices);
        routesObject.add(cheapestRoute);

        //Fastets route getting into Dto

        ArrayList<LatLngName> fastestMarkers = new ArrayList<>();
        for (int i = 0; i < routes.getFastestMarkers().size(); i++) {

            LatLngName fastestMarker = new LatLngName(routes.getFastestMarkers().get(i).getLat(),
                    routes.getFastestMarkers().get(i).getLng());
            fastestMarker.setNameLocation(routes.getFastestMarkers().get(i).getLabel());
            cheapestMarkers.add(fastestMarker);
        }

        CreateRouteModel fastestRoute = new CreateRouteModel(routes.getFastestPolyline(),fastestMarkers,
                routes.getTotalFastestTime(),routes.getTotalFastestCost());

        List<LatLng> fastestVertices = decodePolyline(routes.getFastestPolyline());
        fastestRoute.setVertices(fastestVertices);
        routesObject.add(fastestRoute);




        routesObject.get(0).setChainNameList(uniqueCheapestChainList);
        routesObject.get(0).setChainIdList(uniqueCheapestIdChainList);

        routesObject.get(1).setChainNameList(uniqueFastestChainNameList);
        routesObject.get(1).setChainIdList(uniqueFastestChainIdList);







        return routesObject;


    }

    private static List<LatLng> decodePolyline(String encodedPolyline) {

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


    public CreateRouteModel createRoute(LatLngName partida, LatLngName destino, List<String> chains, double totalCost) {
        //TODO MUDAR QUANDO RECEBER A SHOPPING LIST

        //create route between partida and destino and return polyline
        String polyline = this.createAToBRoute(partida, destino);

        //User get chain from shopping list
        // create new list of chains
        //TODO PASSAR PARA VARIAVEL NO METODO


        List<LatLng> routeCoordinates = decodePolyline(polyline);
        // ciclo forpara introduzir modulos

        //get the closest store locations based on the route submited by the user
        ClosestChainModel[] closestChainModels = findClosestLocation(routeCoordinates, chains, partida, destino, 3000);


        Map<String, Object> createRouteWithWaypointsResult = createRouteWithWaypoints(partida, destino, closestChainModels);

        String finalPolyline = (String) createRouteWithWaypointsResult.get("polyline");
        Integer totalTime = (Integer) createRouteWithWaypointsResult.get("totalTime");
        ArrayList<LatLngName> markers = new ArrayList<>();
        markers.add(partida);
        for (int i = 0; i < closestChainModels.length; i++) {
            markers.add(closestChainModels[i].getCoordinates());
        }
        markers.add(destino);

        CreateRouteModel newRoute = new CreateRouteModel(finalPolyline, markers, totalTime, totalCost);
        return newRoute;
    }

    private ClosestChainModel[] findClosestLocation(List<LatLng> routeCoordinates, List<String> chainlist, LatLng partida, LatLng destino, double radius) {
        boolean allPlacesFound = true;

        ClosestChainModel[] closestChainArray = new ClosestChainModel[chainlist.size()];
        for (int i = 0; i < closestChainArray.length; i++) {
            ClosestChainModel closestChainModel = new ClosestChainModel(chainlist.get(i));
            closestChainArray[i] = (closestChainModel);
        }

        do {
            Boolean[] placeFound = new Boolean[closestChainArray.length];
            // Initialize booleanList with false values
            Arrays.fill(placeFound, false);


            //for each coordinate find the closest chain store
            int adder = routeCoordinates.size() / 6;

            for (int i = 0; i < routeCoordinates.size(); i = i + adder) {
                for (int j = 0; j < closestChainArray.length; j++) {
                    //Coordinate from the route chosen by the user
                    LatLngName newCoordinate = getPlaceCoordinate(closestChainArray[j].getChain(), routeCoordinates.get(i), radius);


                    if (newCoordinate != null) {
                        //calcular distancias das coordenadas à partida e ao destino
                        placeFound[j] = true;
                        newCoordinate.setNameLocation(closestChainArray[j].getChain());
                        double distPartida = calculateHaversineDistance(newCoordinate.getLat(), newCoordinate.getLng(), partida.getLat(), partida.getLng());
                        double distDestino = calculateHaversineDistance(newCoordinate.getLat(), newCoordinate.getLng(), destino.getLat(), destino.getLng());
                        double total = distPartida + distDestino;
                        if (closestChainArray[j].getDist() == 0) {
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


        } while (!allPlacesFound && radius <= 7000);
        if (radius > 7000) throw new BadRequestException("Não foi possível criar encontrar lojas na sua localização");
        // no places found in the 7 km range


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

    private Map<String, Object> createRouteWithWaypoints(LatLng partida, LatLng destino, ClosestChainModel[] closestChainModels) {
        try {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");

            String waypoints = "";

            for (int i = 0; i < closestChainModels.length; i++) {
                waypoints += "|" + closestChainModels[i].getCoordinates().getLat() + "," +
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
                    "&waypoints=optimize:true" +
                    waypoints +
                    "&key=" + googleKey;


            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);

            String polyline = (String) ((Map<String, Object>) ((Map<String, Object>) ((List<Map<String, Object>>) responseMap.get("routes")).get(0)).get("overview_polyline")).get("points");

            String replacedPolyline = polyline;//.replace("\\\\+", "\\\\");


            // calculate total time
            List<Map<String, Object>> routes = (List<Map<String, Object>>) responseMap.get("routes");

            // Assuming there's at least one candidate in the array

            // Access the first candidate in the array
            Map<String, Object> route = routes.get(0);


            // Access the "geometry" object
            List<Map<String, Object>> legs = (List<Map<String, Object>>) route.get("legs");

            Integer totalTime = 0;

            for (int i = 0; i < legs.size(); i++) {
                Map<String, Object> leg = legs.get(i);
                Map<String, Object> duration = (Map<String, Object>) leg.get("duration");

                totalTime = totalTime + ((Integer) duration.get("value"));

            }


            // Access the "location" object


            Map<String, Object> results = new HashMap<>();
            results.put("polyline", polyline);
            results.put("totalTime", totalTime);

            return results;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LatLngName getPlaceCoordinate(String chain, LatLng biasCoordinates, double radius) {
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
                Map<String, Object> locationMap = (Map<String, Object>) geometry.get("location");
                Double lat = (Double) locationMap.get("lat");
                Double lng = (Double) locationMap.get("lng");

                LatLngName location = new LatLngName(lat, lng);

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


    public boolean checkShoppingList(){
        User user = userService.getAuthenticatedUser();
        ShoppingList shoppingList = user.getCurrentShoppingList();

        List<String> cheapestChainsIdList = shoppingList.getCheapestProductQuantities().stream()
                .map(c -> c.getProduct().getChain().getName()).collect(Collectors.toList());
        
        List<String> chainNameList = new ArrayList<>();
        for (int i = 0; i < user.getCurrentRoute().getCheapestMarkers().size(); i++) {
            chainNameList.add(user.getCurrentRoute().getCheapestMarkers().get(i).getLabel());
        }

        chainNameList.add("partida");
        chainNameList.add("");

        Route route = user.getCurrentRoute();

        List<String> markerLabelList = new ArrayList<>();

        for (int i = 0; i < route.getCheapestMarkers().size(); i++) {
            markerLabelList.add(route.getCheapestMarkers().get(i).getLabel());
        }


        return markerLabelList.containsAll(chainNameList);





        






    }


}

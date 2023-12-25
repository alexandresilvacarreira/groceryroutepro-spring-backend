package pt.upskill.groceryroutepro.services;

import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.List;
import java.util.Map;

public interface ScraperService {

    void scrapeContinenteAll();

    void scrapeContinente(String url, String category);

    void scrapeAuchanAll();

    void scrapeAuchan(String url, String category);

    void scrapeMiniprecoAll();

    void scrapeMinipreco(String url, String category);

    void scrapePingoDoce(String url, String category);

    void scrapePingoDoceAll();

    void scrapeIntermarche(String url, String category, String requestBody);

    void scrapeIntermarcheAll();

//    Map<String, List<Integer>> getSubcategoriesIntermarche(String storeUrl);

}

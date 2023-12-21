package pt.upskill.groceryroutepro.services;

import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.Map;

public interface ScraperService {

    void scrapeContinenteAll();

    void scrapeContinente(String url, String category);

    void scrapeAuchan(String url, String category);

    void scrapeMinipreco(String url, String category);

    void scrapePingoDoce(String url, String category);

    void scrapeIntermarche(String url, String category, String requestBody);

}

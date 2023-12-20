package pt.upskill.groceryroutepro.services;

import okhttp3.Response;

public interface ScraperService {
    void scrapeContinente(String url, String category);

    void scrapeAuchan(String url, String category);

    void scrapeMinipreco(String url, String category);

    Response scrapePingoDoce(String url, String category);

}

package pt.upskill.groceryroutepro.services;



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



}

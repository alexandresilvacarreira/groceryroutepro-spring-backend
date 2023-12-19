package pt.upskill.groceryroutepro.services;

public interface ScraperService {
    void scrapeContinente(String url, String category);

    void scrapeAuchan(String url, String category);

}

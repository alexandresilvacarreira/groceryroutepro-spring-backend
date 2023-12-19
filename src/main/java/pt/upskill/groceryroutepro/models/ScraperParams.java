package pt.upskill.groceryroutepro.models;

public class ScraperParams {
    private String url;
    private String category;

    public ScraperParams() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

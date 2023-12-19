package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.upskill.groceryroutepro.models.ScraperParams;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.services.ScraperService;

import java.util.Map;

@RestController
@Component
public class ScraperController {

    @Autowired
    ScraperService scraperService;

    @PostMapping("/scraper/continente")
    public ResponseEntity<String> scrapeContinente(@RequestBody ScraperParams scraperParams) {
        try {
            this.scraperService.scrapeContinente(scraperParams.getUrl(), scraperParams.getCategory());
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

}

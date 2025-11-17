package intern.devlab.currencyratefallback.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class CurrencyConversionController {

    @GetMapping("/ping")
    public String ping() {
        return "Service is running!";
    }
}

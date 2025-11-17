package intern.devlab.currencyratefallback.controller;

import intern.devlab.currencyratefallback.service.CurrencyRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
public class CurrencyConversionController {

    private final CurrencyRateService currencyRateService;

    public CurrencyConversionController(CurrencyRateService currencyRateService) {
        this.currencyRateService = currencyRateService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Service is running!";
    }

    @GetMapping("/rate")
    public String getRate(@RequestParam("to") String to) {
        Optional<BigDecimal> rateOpt = currencyRateService.getUsdTo(to);

        return rateOpt
                .map(rate -> "Current USD -> " + to.toUpperCase() + " rate: " + rate)
                .orElse("Rate for currency " + to + " not found");
    }

    @GetMapping("/rate/raw")
    public String getRawRates() {
        return currencyRateService.getRawRatesJson();
    }
}

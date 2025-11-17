package intern.devlab.currencyratefallback.service;

import intern.devlab.currencyratefallback.client.ExternalRateClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CurrencyRateService {

    private final ExternalRateClient externalRateClient;

    public CurrencyRateService(ExternalRateClient externalRateClient) {
        this.externalRateClient = externalRateClient;
    }

    public Optional<BigDecimal> getUsdTo(String targetCurrency) {
        return externalRateClient.getRateFor(targetCurrency);
    }

    public String getRawRatesJson() {
        return externalRateClient.getRawLatestRatesJson();
    }
}

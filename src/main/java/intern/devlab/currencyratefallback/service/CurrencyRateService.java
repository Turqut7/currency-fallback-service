package intern.devlab.currencyratefallback.service;

import intern.devlab.currencyratefallback.cache.CurrencyRateCache;
import intern.devlab.currencyratefallback.client.ExternalRateClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CurrencyRateService {

    private final ExternalRateClient externalRateClient;
    private final CurrencyRateCache cache;

    public CurrencyRateService(ExternalRateClient externalRateClient, CurrencyRateCache cache) {
        this.externalRateClient = externalRateClient;
        this.cache = cache;
    }

    public Optional<BigDecimal> getUsdTo(String targetCurrency) {
        BigDecimal cached = cache.getRate("USD", targetCurrency);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<BigDecimal> apiRate = externalRateClient.getRateFor(targetCurrency);

        apiRate.ifPresent(rate -> cache.saveRate("USD", targetCurrency, rate));

        return apiRate;
    }

    public String getRawRatesJson() {
        return externalRateClient.getRawLatestRatesJson();
    }
}

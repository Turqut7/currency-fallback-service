package intern.devlab.currencyratefallback.service;

import intern.devlab.currencyratefallback.cache.CurrencyRateCache;
import intern.devlab.currencyratefallback.client.ExternalRateClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Service
public class CurrencyRateService {

    private static final Logger log =
            LoggerFactory.getLogger(CurrencyRateService.class);

    private final ExternalRateClient externalRateClient;
    private final CurrencyRateCache cache;

    public CurrencyRateService(
            ExternalRateClient externalRateClient,
            CurrencyRateCache cache
    ) {
        this.externalRateClient = externalRateClient;
        this.cache = cache;
    }

    public Optional<BigDecimal> getUsdTo(String targetCurrency) {
        String normalizedTarget =
                targetCurrency.toUpperCase(Locale.ROOT);

        // 1. Əvvəl Redis-i yoxlayırıq.
        BigDecimal cachedRate =
                cache.getRate("USD", normalizedTarget);

        if (cachedRate != null) {
            log.debug(
                    "Returning cached rate for USD:{}",
                    normalizedTarget
            );

            return Optional.of(cachedRate);
        }

        // 2. Cache-də məlumat yoxdursa, xarici API-yə müraciət edirik.
        try {
            Optional<BigDecimal> apiRate =
                    externalRateClient.getRateFor(normalizedTarget);

            // 3. API-dən məzənnə gəlibsə, Redis-də saxlayırıq.
            apiRate.ifPresent(rate ->
                    cache.saveRate("USD", normalizedTarget, rate)
            );

            return apiRate;

        } catch (RestClientException e) {
            log.warn(
                    "External API failed for USD:{}. No cached rate available.",
                    normalizedTarget,
                    e
            );

            return Optional.empty();
        }
    }

    public String getRawRatesJson() {
        return externalRateClient.getRawLatestRatesJson();
    }
}
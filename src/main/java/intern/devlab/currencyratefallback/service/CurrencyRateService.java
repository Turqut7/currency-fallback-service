package intern.devlab.currencyratefallback.service;

import intern.devlab.currencyratefallback.cache.CurrencyRateCache;
import intern.devlab.currencyratefallback.client.ExternalRateClient;
import intern.devlab.currencyratefallback.model.ConversionResponse;
import intern.devlab.currencyratefallback.model.ExternalRatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Optional;
import java.util.Map;

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

    public void refreshRates() {
        log.info("Starting currency rates refresh");

        try {
            ExternalRatesResponse response =
                    externalRateClient.getLatestRates();

            Map<String, BigDecimal> rates = response.getRates();

            if (rates == null || rates.isEmpty()) {
                log.warn("Currency rates response is empty");
                return;
            }

            for (Map.Entry<String, BigDecimal> entry
                    : rates.entrySet()) {

                String targetCurrency = entry.getKey();
                BigDecimal rate = entry.getValue();

                if (rate == null) {
                    continue;
                }

                cache.saveRate(
                        "USD",
                        targetCurrency.toUpperCase(Locale.ROOT),
                        rate
                );
            }

            log.info(
                    "Currency rates refresh completed. Rates processed: {}",
                    rates.size()
            );

        } catch (RestClientException e) {
            log.error(
                    "Currency rates refresh failed. Existing cached rates will be retained.",
                    e
            );
        }
    }

    public ConversionResponse convert(
            BigDecimal amount,
            String targetCurrency
    ) {
        // 1. Validate amount.
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero"
            );
        }

        // 2. Validate target currency.
        if (targetCurrency == null
                || !targetCurrency.matches("(?i)[A-Z]{3}")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target currency must be a valid 3-letter code"
            );
        }

        String normalizedTarget =
                targetCurrency.toUpperCase(Locale.ROOT);

        // 3. Get exchange rate.
        Optional<BigDecimal> rateOptional =
                getUsdTo(normalizedTarget);

        if (rateOptional.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Exchange rate not available for currency: "
                            + normalizedTarget
            );
        }

        BigDecimal rate = rateOptional.get();

        // 4. Calculate converted amount.
        BigDecimal convertedAmount =
                amount.multiply(rate)
                        .setScale(2, RoundingMode.HALF_UP);

        // 5. Return conversion response.
        return new ConversionResponse(
                "USD",
                normalizedTarget,
                amount,
                rate,
                convertedAmount
        );
    }
}
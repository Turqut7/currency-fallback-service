package intern.devlab.currencyratefallback.client;

import intern.devlab.currencyratefallback.model.ExternalRatesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Component
public class ExternalRateClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String latestPath;
    private final String baseCurrency;

    public ExternalRateClient(RestTemplate restTemplate,
                              @Value("${external.rates.base-url}") String baseUrl,
                              @Value("${external.rates.latest-path}") String latestPath,
                              @Value("${external.rates.base-currency}") String baseCurrency) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.latestPath = latestPath;
        this.baseCurrency = baseCurrency;
    }

    private String buildLatestUrl() {
        return baseUrl + latestPath + baseCurrency;
    }

    public Optional<BigDecimal> getRateFor(String targetCurrency) {
        String uppercaseTarget = targetCurrency.toUpperCase(Locale.ROOT);
        String url = buildLatestUrl();

        ExternalRatesResponse response =
                restTemplate.getForObject(url, ExternalRatesResponse.class);

        if (response == null
                || response.getRates() == null
                || response.getResult() == null
                || !"success".equalsIgnoreCase(response.getResult())) {
            return Optional.empty();
        }

        BigDecimal rate = response.getRates().get(uppercaseTarget);
        return Optional.ofNullable(rate);
    }

    public String getRawLatestRatesJson() {
        String url = buildLatestUrl();
        return restTemplate.getForObject(url, String.class);
    }
}

package intern.devlab.currencyratefallback.scheduler;

import intern.devlab.currencyratefallback.service.CurrencyRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CurrencyRateScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(CurrencyRateScheduler.class);

    private final CurrencyRateService currencyRateService;

    public CurrencyRateScheduler(
            CurrencyRateService currencyRateService
    ) {
        this.currencyRateService = currencyRateService;
    }

    @Scheduled(
            fixedRateString = "${currency.rates.refresh-interval:3600000}",
            initialDelayString = "${currency.rates.refresh-initial-delay:0}"
    )
    public void refreshRates() {
        log.info("Scheduled currency rates refresh triggered");

        currencyRateService.refreshRates();
    }
}
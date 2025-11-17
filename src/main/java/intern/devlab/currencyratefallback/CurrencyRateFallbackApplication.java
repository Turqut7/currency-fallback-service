package intern.devlab.currencyratefallback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class CurrencyRateFallbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyRateFallbackApplication.class, args);
    }

}

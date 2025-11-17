package intern.devlab.currencyratefallback.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ExternalRatesResponse {

    private String result;
    private String base_code;
    private Map<String, BigDecimal> rates;
}

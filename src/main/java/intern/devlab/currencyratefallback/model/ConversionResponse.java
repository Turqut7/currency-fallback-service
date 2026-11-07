package intern.devlab.currencyratefallback.model;

import java.math.BigDecimal;

public record ConversionResponse(
        String from,
        String to,
        BigDecimal amount,
        BigDecimal rate,
        BigDecimal convertedAmount
) {
}

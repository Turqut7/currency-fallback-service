package intern.devlab.currencyratefallback.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class ExternalRateClientSpec extends Specification {

    @Autowired
    ExternalRateClient externalRateClient

    def "should fetch USD to EUR exchange rate"() {
        given: "External rate client is available"
        def targetCurrency = "EUR"

        when: "We request the exchange rate"
        def result = externalRateClient.getRateFor(targetCurrency)

        then: "A positive exchange rate should be returned"
        result.isPresent()
        result.get() > 0
    }

    def "should fetch raw exchange rates JSON"() {
        when: "We request the raw rates response"
        def response = externalRateClient.getRawLatestRatesJson()

        then: "A non-empty JSON response should be returned"
        response != null
        !response.isBlank()
    }
}
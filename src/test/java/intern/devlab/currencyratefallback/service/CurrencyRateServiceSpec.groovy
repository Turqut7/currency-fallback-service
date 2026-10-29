package intern.devlab.currencyratefallback.service

import intern.devlab.currencyratefallback.cache.CurrencyRateCache
import intern.devlab.currencyratefallback.client.ExternalRateClient
import org.springframework.web.client.RestClientException
import spock.lang.Specification

import java.math.BigDecimal

class CurrencyRateServiceSpec extends Specification {

    ExternalRateClient externalRateClient = Mock()
    CurrencyRateCache cache = Mock()

    CurrencyRateService currencyRateService =
            new CurrencyRateService(externalRateClient, cache)

    def "should return cached rate without calling external API"() {
        given:
        def cachedRate = new BigDecimal("0.92")

        when:
        def result = currencyRateService.getUsdTo("EUR")

        then:
        1 * cache.getRate("USD", "EUR") >> cachedRate

        and:
        0 * externalRateClient._

        and:
        result.isPresent()
        result.get() == cachedRate
    }

    def "should fetch rate from API and cache it when cache is empty"() {
        given:
        def apiRate = new BigDecimal("0.93")

        when:
        def result = currencyRateService.getUsdTo("EUR")

        then:
        1 * cache.getRate("USD", "EUR") >> null

        and:
        1 * externalRateClient.getRateFor("EUR") >>
                Optional.of(apiRate)

        and:
        1 * cache.saveRate("USD", "EUR", apiRate)

        and:
        result.isPresent()
        result.get() == apiRate
    }

    def "should return empty when cache is empty and API fails"() {
        when:
        def result = currencyRateService.getUsdTo("EUR")

        then:
        1 * cache.getRate("USD", "EUR") >> null

        and:
        1 * externalRateClient.getRateFor("EUR") >>
                { throw new RestClientException("API unavailable") }

        and:
        0 * cache.saveRate(_, _, _)

        and:
        !result.isPresent()
    }

    def "should normalize target currency to uppercase"() {
        given:
        def cachedRate = new BigDecimal("0.92")

        when:
        def result = currencyRateService.getUsdTo("eur")

        then:
        1 * cache.getRate("USD", "EUR") >> cachedRate

        and:
        0 * externalRateClient._

        and:
        result.get() == cachedRate
    }
}
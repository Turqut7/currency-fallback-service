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

    def "should convert USD to target currency"() {
        given:
        def amount = new BigDecimal("100")
        def rate = new BigDecimal("0.86")

        when:
        def result = currencyRateService.convert(amount, "EUR")

        then:
        1 * cache.getRate("USD", "EUR") >> rate
        0 * externalRateClient._

        and:
        result.from() == "USD"
        result.to() == "EUR"
        result.amount() == amount
        result.rate() == rate
        result.convertedAmount() == new BigDecimal("86.00")
    }

    def "should round converted amount to two decimal places"() {
        given:
        def amount = new BigDecimal("10")
        def rate = new BigDecimal("0.123456")

        when:
        def result = currencyRateService.convert(amount, "EUR")

        then:
        1 * cache.getRate("USD", "EUR") >> rate
        0 * externalRateClient._

        and:
        result.convertedAmount() == new BigDecimal("1.23")
    }

    def "should reject zero amount"() {
        when:
        currencyRateService.convert(BigDecimal.ZERO, "EUR")

        then:
        thrown(org.springframework.web.server.ResponseStatusException)
        0 * cache._
        0 * externalRateClient._
    }

    def "should reject negative amount"() {
        when:
        currencyRateService.convert(new BigDecimal("-10"), "EUR")

        then:
        thrown(org.springframework.web.server.ResponseStatusException)
        0 * cache._
        0 * externalRateClient._
    }

    def "should reject invalid currency code"() {
        when:
        currencyRateService.convert(
                new BigDecimal("100"),
                "EURO"
        )

        then:
        thrown(org.springframework.web.server.ResponseStatusException)
        0 * cache._
        0 * externalRateClient._
    }

    def "should reject unavailable currency rate"() {
        when:
        currencyRateService.convert(
                new BigDecimal("100"),
                "XXX"
        )

        then:
        1 * cache.getRate("USD", "XXX") >> null
        1 * externalRateClient.getRateFor("XXX") >> Optional.empty()
        0 * cache.saveRate(_, _, _)

        and:
        thrown(org.springframework.web.server.ResponseStatusException)
    }
}
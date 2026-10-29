package intern.devlab.currencyratefallback.client

import intern.devlab.currencyratefallback.model.ExternalRatesResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject
import org.spockframework.spring.SpringBean

@SpringBootTest
class ExternalRateClientRetrySpec extends Specification {

    @SpringBean
    RestTemplate restTemplate = Mock()

    @Autowired
    @Subject
    ExternalRateClient externalRateClient

    def "should retry failed requests and return rates when a later attempt succeeds"() {
        given: "an API response containing the EUR rate"
        def response = new ExternalRatesResponse()
        response.setResult("success")
        response.setBase_code("USD")
        response.setRates([EUR: new BigDecimal("0.92")])

        when: "the client requests the EUR rate"
        def result = externalRateClient.getRateFor("EUR")

        then:
        1 * restTemplate.getForObject(_, ExternalRatesResponse) >>
                { throw new RestClientException("First attempt failed") }

        1 * restTemplate.getForObject(_, ExternalRatesResponse) >>
                { throw new RestClientException("Second attempt failed") }

        1 * restTemplate.getForObject(_, ExternalRatesResponse) >> response

        and:
        result.isPresent()
        result.get() == new BigDecimal("0.92")
    }
}
package intern.devlab.currencyratefallback.cache

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification

import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class CurrencyRateCacheSpec extends Specification {

    RedisTemplate<String, Object> redisTemplate = Mock()
    ValueOperations<String, Object> valueOperations = Mock()

    CurrencyRateCache cache = new CurrencyRateCache(redisTemplate)

    def setup() {
        redisTemplate.opsForValue() >> valueOperations
    }

    def "should save rate to Redis with one hour TTL"() {
        given:
        def rate = new BigDecimal("1.70")

        when:
        cache.saveRate("USD", "AZN", rate)

        then:
        1 * valueOperations.set(
                "USD:AZN",
                rate,
                1L,
                TimeUnit.HOURS
        )
    }

    def "should return cached rate when Redis contains BigDecimal"() {
        given:
        def rate = new BigDecimal("1.70")

        when:
        def result = cache.getRate("USD", "AZN")

        then:
        1 * valueOperations.get("USD:AZN") >> rate

        and:
        result == rate
    }

    def "should return null when Redis has no cached rate"() {
        when:
        def result = cache.getRate("USD", "AZN")

        then:
        1 * valueOperations.get("USD:AZN") >> null

        and:
        result == null
    }

    def "should return null when Redis operation fails"() {
        when:
        def result = cache.getRate("USD", "AZN")

        then:
        1 * valueOperations.get("USD:AZN") >>
                { throw new RuntimeException("Redis unavailable") }

        and:
        result == null
    }

    def "should not propagate exception when saving rate fails"() {
        given:
        def rate = new BigDecimal("1.70")

        when:
        cache.saveRate("USD", "AZN", rate)

        then:
        1 * valueOperations.set(
                "USD:AZN",
                rate,
                1L,
                TimeUnit.HOURS
        ) >> { throw new RuntimeException("Redis unavailable") }

        noExceptionThrown()
    }
}
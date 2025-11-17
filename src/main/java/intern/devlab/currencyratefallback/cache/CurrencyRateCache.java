package intern.devlab.currencyratefallback.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
public class CurrencyRateCache {

    private final RedisTemplate<String, Object> redisTemplate;

    public CurrencyRateCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRate(String base, String target, BigDecimal rate) {
        String key = base + ":" + target;  // Məs: USD:AZN
        try {
            redisTemplate
                    .opsForValue()
                    .set(key, rate, 1, TimeUnit.HOURS);
        } catch (Exception e) {
        }
    }

    public BigDecimal getRate(String base, String target) {
        String key = base + ":" + target;
        try {
            Object value = redisTemplate
                    .opsForValue()
                    .get(key);

            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}

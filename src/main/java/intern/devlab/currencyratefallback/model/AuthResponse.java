package intern.devlab.currencyratefallback.model;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
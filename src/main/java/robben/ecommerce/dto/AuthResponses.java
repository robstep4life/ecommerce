package robben.ecommerce.dto;

public class AuthResponses {

    public record AuthResponse(
            String token,
            String tokenType
    ) {}

    public record MeResponse(
            Long id,
            String email,
            String role
    ) {}
}

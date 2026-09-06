package com.walkin.controller;

import com.walkin.dto.*;
import com.walkin.entity.ApplicationUser;
import com.walkin.repository.ApplicationUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;
    private final ApplicationUserRepository users;
    private final Duration ttl;
    public AuthController(AuthenticationManager authenticationManager, JwtEncoder encoder,
            ApplicationUserRepository users,
            @org.springframework.beans.factory.annotation.Value("${app.jwt.ttl:PT15M}") Duration ttl) {
        this.authenticationManager=authenticationManager;
        this.encoder=encoder;
        this.users=users;
        this.ttl=ttl;
    }
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth=authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        Instant now=Instant.now(), expires=now.plus(ttl);
        String roles=auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
        ApplicationUser user = users.findByUsernameIgnoreCase(auth.getName())
                .orElseThrow(() -> new BadCredentialsException("Authenticated user no longer exists"));
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().issuer("walkin-api")
                .issuedAt(now).expiresAt(expires).subject(auth.getName()).claim("roles", roles);
        if (user.getCompany() != null) {
            claims.claim("companyId", user.getCompany().getCompanyId());
        }
        return new TokenResponse(
                encoder.encode(JwtEncoderParameters.from(claims.build())).getTokenValue(),
                "Bearer", expires);
    }
}

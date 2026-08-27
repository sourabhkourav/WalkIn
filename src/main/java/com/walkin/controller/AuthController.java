package com.walkin.controller;

import com.walkin.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager; private final JwtEncoder encoder; private final Duration ttl;
    public AuthController(AuthenticationManager authenticationManager, JwtEncoder encoder,
            @org.springframework.beans.factory.annotation.Value("${app.jwt.ttl:PT15M}") Duration ttl) {
        this.authenticationManager=authenticationManager; this.encoder=encoder; this.ttl=ttl;
    }
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth=authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        Instant now=Instant.now(), expires=now.plus(ttl);
        String roles=auth.getAuthorities().stream().map(a -> a.getAuthority()).reduce((a,b)->a+" "+b).orElse("");
        JwtClaimsSet claims=JwtClaimsSet.builder().issuer("walkin-api").issuedAt(now).expiresAt(expires)
                .subject(auth.getName()).claim("roles", roles).build();
        return new TokenResponse(encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), "Bearer", expires);
    }
}

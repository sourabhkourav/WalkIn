package com.walkin.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CompanyTenantAccess {

    public boolean isPlatformAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_PLATFORM_ADMIN".equals(authority.getAuthority()));
    }

    public Integer requireCompanyId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw denied();
        }
        Object claim = jwtAuthentication.getToken().getClaims().get("companyId");
        if (claim instanceof Number number) {
            return number.intValue();
        }
        if (claim instanceof String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException ignored) {
                throw denied();
            }
        }
        throw denied();
    }

    public void requireCompanyAccess(Authentication authentication, Integer resourceCompanyId) {
        if (isPlatformAdmin(authentication)) {
            return;
        }
        if (!requireCompanyId(authentication).equals(resourceCompanyId)) {
            throw denied();
        }
    }

    private AccessDeniedException denied() {
        return new AccessDeniedException("You do not have access to this company resource");
    }
}

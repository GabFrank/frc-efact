package com.frcefact.security.apikey;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Autenticación de Spring Security para una API key ya validada.
 *
 * <p>Sus únicas authorities son {@code SCOPE_*}: nunca {@code ROLE_*}. Así una key no pasa los
 * {@code @PreAuthorize("hasRole(...)")} de la API de usuarios, aunque llegara a una ruta de esa API.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final ApiKeyPrincipal principal;

    public ApiKeyAuthenticationToken(ApiKeyPrincipal principal) {
        super(principal.scopes().stream().map(s -> new SimpleGrantedAuthority("SCOPE_" + s)).toList());
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public ApiKeyPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.nombreAuditoria();
    }
}

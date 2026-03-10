package com.bcp.training.expenses;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oidc")
public class OidcController {

    @GetMapping
    public ResponseEntity<OidcUser> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new OidcUser(Set.of()));
        }
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().cachePrivate().mustRevalidate())
                .body(new OidcUser(roles));
    }

    public static class OidcUser {
        private final Set<String> roles;

        public OidcUser(Set<String> roles) {
            this.roles = roles;
        }

        public Set<String> getRoles() {
            return roles;
        }
    }
}

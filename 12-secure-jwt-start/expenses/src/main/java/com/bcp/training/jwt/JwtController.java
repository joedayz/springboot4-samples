package com.bcp.training.jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jwt")
public class JwtController {

    private final JwtGenerator jwtGenerator;

    public JwtController(JwtGenerator jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    @GetMapping("/{username}")
    public String getJwt(@PathVariable String username) {
        if ("admin".equalsIgnoreCase(username)) {
            return jwtGenerator.generateJwtForAdmin(username);
        }
        return jwtGenerator.generateJwtForRegularUser(username);
    }
}

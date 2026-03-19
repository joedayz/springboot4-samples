package com.bcp.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adder")
public class AdderResource {

    private static final Logger log = LoggerFactory.getLogger(AdderResource.class);
    private final SolverClient solverClient;

    public AdderResource(SolverClient solverClient) {
        this.solverClient = solverClient;
    }

    @GetMapping(value = "/{lhs}/{rhs}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Float add(@PathVariable String lhs, @PathVariable String rhs) {
        log.info("Adding {} to {}", lhs, rhs);
        return solverClient.solve(lhs) + solverClient.solve(rhs);
    }
}


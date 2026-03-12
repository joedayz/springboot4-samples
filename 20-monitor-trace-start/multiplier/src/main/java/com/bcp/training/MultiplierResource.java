package com.bcp.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/multiplier")
public class MultiplierResource {

    private static final Logger log = LoggerFactory.getLogger(MultiplierResource.class);
    private final SolverClient solverClient;

    public MultiplierResource(SolverClient solverClient) {
        this.solverClient = solverClient;
    }

    @GetMapping(value = "/{lhs}/{rhs}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Float multiply(@PathVariable String lhs, @PathVariable String rhs) {
        log.info("Multiplying {} by {}", lhs, rhs);
        return solverClient.solve(lhs) * solverClient.solve(rhs);
    }
}

package com.bcp.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/solver")
public class SolverResource {

    private static final Logger log = LoggerFactory.getLogger(SolverResource.class);
    private static final Pattern MULTIPLY_PATTERN = Pattern.compile("(.*)\\*(.*)");
    private static final Pattern ADD_PATTERN = Pattern.compile("(.*)\\+(.*)");

    private final AdderClient adderClient;
    private final MultiplierClient multiplierClient;

    public SolverResource(AdderClient adderClient, MultiplierClient multiplierClient) {
        this.adderClient = adderClient;
        this.multiplierClient = multiplierClient;
    }

    @GetMapping(value = "/{equation}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> solve(@PathVariable String equation) {
        log.info("Solving '{}'", equation);
        try {
            return ResponseEntity.ok(doSolve(equation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Float doSolve(String equation) {
        try {
            return Float.valueOf(equation);
        } catch (NumberFormatException e) {
            Matcher addMatcher = ADD_PATTERN.matcher(equation);
            if (addMatcher.matches()) {
                return adderClient.add(addMatcher.group(1), addMatcher.group(2));
            }
            Matcher multiplyMatcher = MULTIPLY_PATTERN.matcher(equation);
            if (multiplyMatcher.matches()) {
                return multiplierClient.multiply(multiplyMatcher.group(1), multiplyMatcher.group(2));
            }
            throw new IllegalArgumentException("Unable to parse: " + equation);
        }
    }
}

package com.bcp.training;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/solver")
public class SolverResource {

    private static final Pattern MULTIPLY_PATTERN = Pattern.compile("(.+)\\*(.+)");
    private static final Pattern ADD_PATTERN = Pattern.compile("(.+)\\+(.+)");

    @GetMapping(value = "/{equation}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> solve(@PathVariable("equation") String equation) {
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
                return add(addMatcher.group(1), addMatcher.group(2));
            }
            Matcher multiplyMatcher = MULTIPLY_PATTERN.matcher(equation);
            if (multiplyMatcher.matches()) {
                return multiply(multiplyMatcher.group(1), multiplyMatcher.group(2));
            }
            throw new IllegalArgumentException("Unable to parse: " + equation);
        }
    }

    private Float add(String lhs, String rhs) {
        return doSolve(lhs) + doSolve(rhs);
    }

    private Float multiply(String lhs, String rhs) {
        return doSolve(lhs) * doSolve(rhs);
    }
}

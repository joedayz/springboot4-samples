package com.bcp.training;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ExpenseValidatorCli implements CommandLineRunner {

    private final ExpenseValidator validator;

    public ExpenseValidatorCli(ExpenseValidator validator) {
        this.validator = validator;
    }

    @Override
    public void run(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("The command requires 1 argument");
        }

        try {
            int amountValue = Integer.parseInt(args[0]);

            if (validator.isValidAmount(amountValue)) {
                System.out.println("Valid amount: " + amountValue);
                return;
            }

            System.out.println("Invalid amount: " + amountValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The argument must be an integer", e);
        }
    }
}

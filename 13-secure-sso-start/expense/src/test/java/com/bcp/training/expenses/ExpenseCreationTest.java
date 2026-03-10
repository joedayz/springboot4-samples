package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.bcp.training.expenses.Expense.PaymentMethod;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class ExpenseCreationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ExpenseRepository expenseRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
    }

    @Test
    void testCreateExpense() throws Exception {
        String body = """
                {"name":"Test Expense","paymentMethod":"CASH","amount":"2"}
                """;

        mockMvc.perform(post("/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/expense")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Expense"))
                .andExpect(jsonPath("$[0].paymentMethod").value(PaymentMethod.CASH.name()))
                .andExpect(jsonPath("$[0].amount").value(2.0));
    }
}

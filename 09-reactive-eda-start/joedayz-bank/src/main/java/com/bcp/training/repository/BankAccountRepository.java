package com.bcp.training.repository;

import com.bcp.training.model.BankAccount;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BankAccountRepository extends ReactiveCrudRepository<BankAccount, Long> {

    @Query("SELECT * FROM bank_account ORDER BY id")
    Flux<BankAccount> findAllOrderById();
}

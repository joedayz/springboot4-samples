package com.bcp.training.expenses;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/associates")
public class AssociateController {

    private final AssociateRepository repository;

    public AssociateController(AssociateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Associate> list() {
        return repository.findAll();
    }
}

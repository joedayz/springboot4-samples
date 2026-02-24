package com.bcp.training.speaker.idgenerator;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RandomIdGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}

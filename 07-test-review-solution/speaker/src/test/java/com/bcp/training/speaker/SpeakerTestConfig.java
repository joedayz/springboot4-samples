package com.bcp.training.speaker;

import com.bcp.training.speaker.idgenerator.IdGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SpeakerTestConfig {

    @Bean
    @Primary
    public IdGenerator idGenerator() {
        return new DeterministicIdGenerator();
    }
}

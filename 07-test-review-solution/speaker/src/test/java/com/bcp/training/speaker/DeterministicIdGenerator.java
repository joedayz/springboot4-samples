package com.bcp.training.speaker;

import com.bcp.training.speaker.idgenerator.IdGenerator;

import java.util.UUID;

public class DeterministicIdGenerator implements IdGenerator {

    private UUID nextUUID = new UUID(0, 0);

    @Override
    public String generate() {
        UUID result = nextUUID;
        nextUUID = null;
        return result.toString();
    }

    public void setNextUUID(UUID nextUUID) {
        this.nextUUID = nextUUID;
    }
}

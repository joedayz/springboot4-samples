package com.bcp.training.service;

import org.springframework.stereotype.Service;

@Service
public class StateService {

    private boolean alive = true;

    public void up() {
        alive = true;
    }

    public void down() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }
}

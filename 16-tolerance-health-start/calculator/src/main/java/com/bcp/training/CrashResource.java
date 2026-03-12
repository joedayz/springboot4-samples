package com.bcp.training;

import com.bcp.training.service.StateService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crash")
public class CrashResource {

    private final StateService applicationState;

    public CrashResource(StateService applicationState) {
        this.applicationState = applicationState;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String setCrash() {
        applicationState.down();
        return "Service not alive\n";
    }
}

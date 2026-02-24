package com.bcp.training.speaker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SpeakerNotFoundException extends RuntimeException {
}

package com.pulse.pulse.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse.pulse.application.PulseService;
import com.pulse.pulse.domain.InputData;
import com.pulse.pulse.domain.PulseResponse;

@RestController
@RequestMapping("/pulse")
public class PulseController {

    private final PulseService pulseService;

    public PulseController(PulseService pulseService) {
        this.pulseService = pulseService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<PulseResponse> analyze(@RequestBody InputData input) {
        return ResponseEntity.ok(pulseService.analyzeData(input));
    }
}

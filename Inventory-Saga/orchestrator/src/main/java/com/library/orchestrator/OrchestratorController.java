package com.library.orchestrator;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/saga")
public class OrchestratorController {

    private final SagaOrchestrator orchestrator;

    public OrchestratorController(SagaOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/reserve")
    public Map<String, Object> reserve(@RequestBody ReservationRequest request) {
        return orchestrator.executeReservationSaga(request);
    }
}
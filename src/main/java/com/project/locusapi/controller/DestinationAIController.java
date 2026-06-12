package com.project.locusapi.controller;

import com.project.locusapi.dto.destination.DestinationAIResponse;
import com.project.locusapi.service.ai.DestinationAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/destinations")
@RequiredArgsConstructor
public class DestinationAIController {

    private final DestinationAIService destinationAIService;

    @GetMapping("/recommendations")
    public ResponseEntity<DestinationAIResponse> recommendTouristPoints(@RequestParam String city) {
        return ResponseEntity.ok(destinationAIService.recommendTouristPoints(city));
    }
}

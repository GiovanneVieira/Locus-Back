package com.project.locusapi.controller;

import com.project.locusapi.dto.message.MessageResponseDTO;
import com.project.locusapi.dto.otp.OTPRequestDTO;
import com.project.locusapi.dto.otp.OTPResponseDTO;
import com.project.locusapi.dto.otp.OTPValidationDTO;
import com.project.locusapi.service.EmailService;
import com.project.locusapi.service.OTPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OTPController {

    private final OTPService otpService;
    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody @Valid OTPRequestDTO otpDto){
        var otp = otpService.generateAndSaveOtp(otpDto.email());
        emailService.sendOTPEmail(otpDto.email(), otp, otpDto.username());
        return ResponseEntity.ok().body(new MessageResponseDTO("OTP Sent Successfully"));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateOtp(@RequestBody @Valid OTPValidationDTO otpDto){

        String otpToken = otpService.validateOtp(otpDto.email(), otpDto.otpCode());

        if(otpToken.isEmpty())
        {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Invalid OTP Code"));
        }
            return ResponseEntity.ok().body(new OTPResponseDTO("Otp validate successfully", otpToken));
    }
}

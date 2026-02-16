package com.tiuon.moneymanager.controller;

import com.tiuon.moneymanager.dto.AuthDto;
import com.tiuon.moneymanager.dto.ProfileDto;
import com.tiuon.moneymanager.service.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor

public class ProfileController {
    private final IProfileService iProfileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDto> registerProfile(@RequestBody ProfileDto profileDto) {
        ProfileDto registeredProfileDto = iProfileService.registerProfile(profileDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfileDto);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token){
        boolean isActivated = iProfileService.activateProfile(token);
        if (isActivated) {
            return ResponseEntity.status(HttpStatus.OK).body("Successfully activate your profile!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Actication token not found or already used!");
        }

    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login (@RequestBody AuthDto authDto) {
        try {
            if (!iProfileService.isAccountActive(authDto.getEmail())) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message","Account is not active. Please activate your account first."
                ));
            }
            Map<String, Object> response = iProfileService.authenticationAndGenerateToken(authDto);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDto> getPublicProfile() {
        ProfileDto profileDto = iProfileService.getPublicProfile(null);
        return  ResponseEntity.status(HttpStatus.OK).body(profileDto);
    }

    @GetMapping("/test")
    public String test() {
        return "Test successful";
    }
}

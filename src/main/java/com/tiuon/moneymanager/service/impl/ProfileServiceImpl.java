package com.tiuon.moneymanager.service.impl;

import com.tiuon.moneymanager.dto.AuthDto;
import com.tiuon.moneymanager.dto.ProfileDto;
import com.tiuon.moneymanager.entity.ProfileEntity;
import com.tiuon.moneymanager.mapper.ProfileMapper;
import com.tiuon.moneymanager.repository.ProfileRepository;
import com.tiuon.moneymanager.service.IEmailService;
import com.tiuon.moneymanager.service.IProfileService;
import com.tiuon.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {
    private final ProfileRepository profileRepository;
    private final IEmailService iEmailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AppUserDetailsServiceImpl appUserDetailsServiceImpl;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDto registerProfile(ProfileDto profileDto) {
        ProfileEntity newProfile = ProfileMapper.toEntity(profileDto);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile.setPassword(passwordEncoder.encode(profileDto.getPassword()));
        newProfile = profileRepository.save(newProfile);

        // send activation email
        String activationLink = activationURL + "/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your Money Manager Account";
        String body = "Click on the following link to activate your account: " + activationLink;
        iEmailService.sendEmail(newProfile.getEmail(), subject, body);

        return ProfileMapper.toDto(newProfile);
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profileEntity -> {
                    profileEntity.setIsActive(true);
                    profileRepository.save(profileEntity);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    @Override
    public Map<String, Object> authenticationAndGenerateToken(AuthDto authDto) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword()));
            // Generate JWT Token
            UserDetails userDetails = appUserDetailsServiceImpl.loadUserByUsername(authDto.getEmail());
            String token = jwtUtil.generateToken(userDetails);
            return Map.of(
              "token", token,
              "user", getPublicProfile(authDto.getEmail())
            );
        } catch (Exception e) {
            throw new RuntimeException("Error: "+ e.getMessage());
        }
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDto getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " +email));
        }
        return ProfileMapper.toDto(currentUser);
    }
}

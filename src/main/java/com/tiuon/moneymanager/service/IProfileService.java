package com.tiuon.moneymanager.service;

import com.tiuon.moneymanager.dto.AuthDto;
import com.tiuon.moneymanager.dto.ProfileDto;
import com.tiuon.moneymanager.entity.ProfileEntity;

import java.util.Map;

public interface IProfileService {

    ProfileDto registerProfile(ProfileDto profileDto);
    boolean activateProfile(String activationToken);
    boolean isAccountActive(String email);
    Map<String, Object> authenticationAndGenerateToken(AuthDto authDto);
    ProfileEntity getCurrentProfile();
    ProfileDto getPublicProfile(String email);
}

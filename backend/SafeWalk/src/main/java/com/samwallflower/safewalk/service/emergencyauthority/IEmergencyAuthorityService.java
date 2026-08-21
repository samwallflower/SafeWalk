package com.samwallflower.safewalk.service.emergencyauthority;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.model.EmergencyAuthority;
import com.samwallflower.safewalk.request.emergencyauthority.AddEmergencyAuthorityRequest;
import com.samwallflower.safewalk.request.emergencyauthority.UpdateEmergencyAuthorityRequest;

import java.util.List;

public interface IEmergencyAuthorityService {
    List<EmergencyAuthorityDto> getAllEmergencyAuthorities();

    EmergencyAuthorityDto addEmergencyAuthority(AddEmergencyAuthorityRequest request);
    EmergencyAuthorityDto updateEmergencyAuthority(Long id , UpdateEmergencyAuthorityRequest request);
    void  deleteEmergencyAuthority(Long id);
    void deleteEmergencyAuthorityByCountryName(String countryName);

    EmergencyAuthorityDto findEmergencyAuthorityById(long id);
    EmergencyAuthorityDto findByCountryCode(String countryCode);
    EmergencyAuthorityDto findByCountryName(String countryName);
    EmergencyAuthorityDto findEmergencyAuthorityByLocation(double latitude, double longitude);
    EmergencyAuthorityDto convertToDto(EmergencyAuthority emergencyAuthority);
}

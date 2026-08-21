package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.request.emergencyauthority.AddEmergencyAuthorityRequest;
import com.samwallflower.safewalk.request.emergencyauthority.UpdateEmergencyAuthorityRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.emergencyauthority.IEmergencyAuthorityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/emergency-authority")
public class EmergencyAuthorityController {
    private final IEmergencyAuthorityService emergencyAuthorityService;

    //TODO: Add Admin restriction later
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addEmergencyAuthority(@Valid @RequestBody AddEmergencyAuthorityRequest request){
        EmergencyAuthorityDto emergencyAuthorityDto = emergencyAuthorityService.addEmergencyAuthority(request);
        return ResponseEntity.ok(new ApiResponse("Emergency authority added successfully.", emergencyAuthorityDto));
    }

    //TODO:ADMIN only
    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse> updateEmergencyAuthority(@PathVariable Long id , @Valid @RequestBody UpdateEmergencyAuthorityRequest request){
        EmergencyAuthorityDto updated = emergencyAuthorityService.updateEmergencyAuthority(id, request);
        return ResponseEntity.ok(new ApiResponse("Emergency authority updated successfully.", updated));
    }

    //TODO:ADMIN only
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteEmergencyAuthority(@PathVariable Long id){
        emergencyAuthorityService.deleteEmergencyAuthority(id);
        return ResponseEntity.ok(new ApiResponse("Emergency authority deleted successfully.", null));
    }

    //TODO: ADMIN only
    @DeleteMapping("/authority/by-country-name/delete")
    public ResponseEntity<ApiResponse> deleteEmergencyAuthorityByCountryName(@RequestParam @NotBlank String countryName){
        emergencyAuthorityService.deleteEmergencyAuthorityByCountryName(countryName);
        return ResponseEntity.ok(new ApiResponse("Emergency authority deleted successfully.", null));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllEmergencyAuthorities(){
        List<EmergencyAuthorityDto>  emergencyAuthorities = emergencyAuthorityService.getAllEmergencyAuthorities();
        return ResponseEntity.ok(new ApiResponse("Emergency authorities fetched successfully.", emergencyAuthorities));
    }

    @GetMapping("/{id}/authority")
    public ResponseEntity<ApiResponse> getEmergencyAuthorityById(@PathVariable Long id){
        EmergencyAuthorityDto authority = emergencyAuthorityService.findEmergencyAuthorityById(id);
        return ResponseEntity.ok(new ApiResponse("Emergency authority found successfully.", authority));
    }

    @GetMapping("/authority/by-country-name")
    public ResponseEntity<ApiResponse> getEmergencyAuthorityByCountryName(@RequestParam @NotBlank String countryName){
        EmergencyAuthorityDto authority = emergencyAuthorityService.findByCountryName(countryName);
        return ResponseEntity.ok(new ApiResponse("Emergency authority found successfully.", authority));
    }

    @GetMapping("/authority/by-country-code")
    public ResponseEntity<ApiResponse> getEmergencyAuthorityByCountryCode(@RequestParam @NotBlank @Size(min = 2, max = 2) String countryCode){
        EmergencyAuthorityDto authority = emergencyAuthorityService.findByCountryCode(countryCode);
        return ResponseEntity.ok(new  ApiResponse("Emergency authority found successfully.", authority));
    }

    @GetMapping("/authority/by-location")
    public ResponseEntity<ApiResponse> getEmergencyAuthorityByLocation(@RequestParam Double latitude, @RequestParam Double longitude){
        EmergencyAuthorityDto authority = emergencyAuthorityService.findEmergencyAuthorityByLocation(latitude, longitude);
        return ResponseEntity.ok(new ApiResponse("Emergency authority found successfully.", authority));
    }

}

package com.samwallflower.safewalk.service.emergencyauthority;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.integration.googlemaps.GoogleMapsClient;
import com.samwallflower.safewalk.model.EmergencyAuthority;
import com.samwallflower.safewalk.repository.EmergencyAuthorityRepository;
import com.samwallflower.safewalk.request.emergencyauthority.AddEmergencyAuthorityRequest;
import com.samwallflower.safewalk.request.emergencyauthority.UpdateEmergencyAuthorityRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmergencyAuthorityService implements IEmergencyAuthorityService{
    private final EmergencyAuthorityRepository emergencyAuthorityRepository;
    private final GoogleMapsClient googleMapsClient;
    private final ModelMapper modelMapper;

    @Override
    public List<EmergencyAuthorityDto> getAllEmergencyAuthorities() {
        return emergencyAuthorityRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    // before we add we should check if this country already exists
    @Override
    public EmergencyAuthorityDto addEmergencyAuthority(AddEmergencyAuthorityRequest request) {

        if(emergencyAuthorityRepository.existsByCountryCode(request.getCountryCode()) ||
        emergencyAuthorityRepository.existsByCountryName(request.getCountryName())){
            throw new ResourceAlreadyExistsException("Emergency Authority already exists with country name "+ request.getCountryName() + " and country code "+ request.getCountryCode());
        }

        EmergencyAuthority emergencyAuthority = new EmergencyAuthority();

        emergencyAuthority.setCountryCode(request.getCountryCode());
        Optional.ofNullable(request.getCountryName()).ifPresent(emergencyAuthority::setCountryName);
        emergencyAuthority.setPoliceNumber(request.getPoliceNumber());
        emergencyAuthority.setAmbulanceNumber(request.getAmbulanceNumber());
        Optional.ofNullable(request.getGeneralEmergencyNumber()).ifPresent(emergencyAuthority::setGeneralEmergencyNumber);

        return convertToDto(emergencyAuthorityRepository.save(emergencyAuthority));

    }

    @Override
    public EmergencyAuthorityDto updateEmergencyAuthority(Long id, UpdateEmergencyAuthorityRequest request) {
        EmergencyAuthority emergencyAuthority = emergencyAuthorityRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency Authority Not Found with id: "+ id));

        Optional.ofNullable(request.getCountryCode()).ifPresent(emergencyAuthority::setCountryCode);
        Optional.ofNullable(request.getCountryName()).ifPresent(emergencyAuthority::setCountryName);
        Optional.ofNullable(request.getPoliceNumber()).ifPresent(emergencyAuthority::setPoliceNumber);
        Optional.ofNullable(request.getAmbulanceNumber()).ifPresent(emergencyAuthority::setAmbulanceNumber);
        Optional.ofNullable(request.getGeneralEmergencyNumber()).ifPresent(emergencyAuthority::setGeneralEmergencyNumber);

        return convertToDto(emergencyAuthorityRepository.save(emergencyAuthority));
    }

    @Override
    public void deleteEmergencyAuthority(Long id) {
        EmergencyAuthority emergencyAuthority = emergencyAuthorityRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency Authority Not Found with id: "+ id));
        emergencyAuthorityRepository.delete(emergencyAuthority);
    }

    @Override
    public void deleteEmergencyAuthorityByCountryName(String countryName) {
        EmergencyAuthority emergencyAuthority = emergencyAuthorityRepository.findByCountryNameIgnoreCase(countryName)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency Authority Not Found with countryName: "+ countryName));
        emergencyAuthorityRepository.delete(emergencyAuthority);

    }

    @Override
    public EmergencyAuthorityDto findEmergencyAuthorityById(long id) {
        return emergencyAuthorityRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency Authority Not Found with id : "+ id));
    }

    @Override
    public EmergencyAuthorityDto findByCountryCode(String countryCode) {
        return emergencyAuthorityRepository.findByCountryCode(countryCode.trim().toUpperCase())
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency Authority Not Found with countryCode : "+ countryCode));
    }

    @Override
    public EmergencyAuthorityDto findByCountryName(String countryName) {
        return emergencyAuthorityRepository.findByCountryNameIgnoreCase(countryName)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Emergency Authority Not Found with countryName : "+ countryName));
    }

    @Override
    public EmergencyAuthorityDto findEmergencyAuthorityByLocation(double latitude, double longitude) {
        String countryCode = googleMapsClient.reverseGeocodeCountryCode(latitude, longitude);

        return emergencyAuthorityRepository.findByCountryCode(countryCode)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "No emergency authority data found for country: "+ countryCode
                ));
    }

    @Override
    public EmergencyAuthorityDto convertToDto(EmergencyAuthority emergencyAuthority) {
        return modelMapper.map(emergencyAuthority, EmergencyAuthorityDto.class);
    }
}

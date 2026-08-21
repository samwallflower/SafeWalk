package com.samwallflower.safewalk.service.emergencyauthority;

import com.samwallflower.safewalk.dto.EmergencyAuthorityDto;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.integration.googlemaps.GoogleMapsClient;
import com.samwallflower.safewalk.model.EmergencyAuthority;
import com.samwallflower.safewalk.repository.EmergencyAuthorityRepository;
import com.samwallflower.safewalk.request.emergencyauthority.AddEmergencyAuthorityRequest;
import com.samwallflower.safewalk.request.emergencyauthority.UpdateEmergencyAuthorityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyAuthorityServiceTest {

    @Mock private EmergencyAuthorityRepository emergencyAuthorityRepository;
    @Mock private GoogleMapsClient googleMapsClient;

    private EmergencyAuthorityService service;

    @BeforeEach
    void setUp() {
        service = new EmergencyAuthorityService(emergencyAuthorityRepository, googleMapsClient, new ModelMapper());
    }

    private EmergencyAuthority buildAuthority(Long id, String countryCode, String countryName,
                                              String police, String ambulance, String general) {
        EmergencyAuthority a = new EmergencyAuthority();
        a.setId(id);
        a.setCountryCode(countryCode);
        a.setCountryName(countryName);
        a.setPoliceNumber(police);
        a.setAmbulanceNumber(ambulance);
        a.setGeneralEmergencyNumber(general);
        return a;
    }

    // ---------- addEmergencyAuthority ----------

    @Test
    void addEmergencyAuthority_success_whenCountryNotAlreadyPresent() {
        AddEmergencyAuthorityRequest request = new AddEmergencyAuthorityRequest();
        request.setCountryCode("HU");
        request.setCountryName("Hungary");
        request.setPoliceNumber("107");
        request.setAmbulanceNumber("104");
        request.setGeneralEmergencyNumber("112");

        when(emergencyAuthorityRepository.existsByCountryCode("HU")).thenReturn(false);
        when(emergencyAuthorityRepository.existsByCountryName("Hungary")).thenReturn(false);
        when(emergencyAuthorityRepository.save(any(EmergencyAuthority.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmergencyAuthorityDto result = service.addEmergencyAuthority(request);

        assertThat(result.getCountryCode()).isEqualTo("HU");
        assertThat(result.getPoliceNumber()).isEqualTo("107");
        verify(emergencyAuthorityRepository).save(any(EmergencyAuthority.class));
    }

    @Test
    void addEmergencyAuthority_throws_whenCountryCodeAlreadyExists() {
        AddEmergencyAuthorityRequest request = new AddEmergencyAuthorityRequest();
        request.setCountryCode("HU");
        request.setCountryName("Hungary");

        when(emergencyAuthorityRepository.existsByCountryCode("HU")).thenReturn(true);

        assertThatThrownBy(() -> service.addEmergencyAuthority(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("Hungary")
                .hasMessageContaining("HU");

        verify(emergencyAuthorityRepository, never()).save(any());
    }

    @Test
    void addEmergencyAuthority_throws_whenCountryNameAlreadyExists() {
        AddEmergencyAuthorityRequest request = new AddEmergencyAuthorityRequest();
        request.setCountryCode("DE");
        request.setCountryName("Germany");

        when(emergencyAuthorityRepository.existsByCountryCode("DE")).thenReturn(false);
        when(emergencyAuthorityRepository.existsByCountryName("Germany")).thenReturn(true);

        assertThatThrownBy(() -> service.addEmergencyAuthority(request))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(emergencyAuthorityRepository, never()).save(any());
    }

    @Test
    void addEmergencyAuthority_success_withNullOptionalFields() {
        AddEmergencyAuthorityRequest request = new AddEmergencyAuthorityRequest();
        request.setCountryCode("US");
        request.setCountryName(null); // optional
        request.setPoliceNumber("911");
        request.setAmbulanceNumber("911");
        request.setGeneralEmergencyNumber(null); // optional

        when(emergencyAuthorityRepository.existsByCountryCode("US")).thenReturn(false);
        when(emergencyAuthorityRepository.existsByCountryName(null)).thenReturn(false);
        when(emergencyAuthorityRepository.save(any(EmergencyAuthority.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmergencyAuthorityDto result = service.addEmergencyAuthority(request);

        assertThat(result.getCountryCode()).isEqualTo("US");
        assertThat(result.getCountryName()).isNull();
        assertThat(result.getGeneralEmergencyNumber()).isNull();
    }

    // ---------- updateEmergencyAuthority ----------

    @Test
    void updateEmergencyAuthority_updatesOnlyProvidedFields() {
        EmergencyAuthority existing = buildAuthority(1L, "HU", "Hungary", "107", "104", "112");

        UpdateEmergencyAuthorityRequest request = new UpdateEmergencyAuthorityRequest();
        request.setPoliceNumber("999"); // only this field provided

        when(emergencyAuthorityRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(emergencyAuthorityRepository.save(any(EmergencyAuthority.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmergencyAuthorityDto result = service.updateEmergencyAuthority(1L, request);

        assertThat(result.getPoliceNumber()).isEqualTo("999");
        assertThat(existing.getAmbulanceNumber()).isEqualTo("104"); // untouched
        assertThat(existing.getCountryCode()).isEqualTo("HU"); // untouched
    }

    @Test
    void updateEmergencyAuthority_updatesAllFields_whenAllProvided() {
        EmergencyAuthority existing = buildAuthority(1L, "HU", "Hungary", "107", "104", "112");

        UpdateEmergencyAuthorityRequest request = new UpdateEmergencyAuthorityRequest();
        request.setCountryCode("AT");
        request.setCountryName("Austria");
        request.setPoliceNumber("133");
        request.setAmbulanceNumber("144");
        request.setGeneralEmergencyNumber("112");

        when(emergencyAuthorityRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(emergencyAuthorityRepository.save(any(EmergencyAuthority.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmergencyAuthorityDto result = service.updateEmergencyAuthority(1L, request);

        assertThat(result.getCountryCode()).isEqualTo("AT");
        assertThat(result.getCountryName()).isEqualTo("Austria");
        assertThat(result.getPoliceNumber()).isEqualTo("133");
    }

    @Test
    void updateEmergencyAuthority_throws_whenNotFound() {
        UpdateEmergencyAuthorityRequest request = new UpdateEmergencyAuthorityRequest();
        when(emergencyAuthorityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateEmergencyAuthority(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emergencyAuthorityRepository, never()).save(any());
    }

    // ---------- deleteEmergencyAuthority ----------

    @Test
    void deleteEmergencyAuthority_success() {
        EmergencyAuthority existing = buildAuthority(1L, "HU", "Hungary", "107", "104", "112");
        when(emergencyAuthorityRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteEmergencyAuthority(1L);

        verify(emergencyAuthorityRepository).delete(existing);
    }

    @Test
    void deleteEmergencyAuthority_throws_whenNotFound() {
        when(emergencyAuthorityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteEmergencyAuthority(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emergencyAuthorityRepository, never()).delete(any());
    }

    // ---------- deleteEmergencyAuthorityByCountryName ----------

    @Test
    void deleteEmergencyAuthorityByCountryName_success() {
        EmergencyAuthority existing = buildAuthority(1L, "HU", "Hungary", "107", "104", "112");
        when(emergencyAuthorityRepository.findByCountryNameIgnoreCase("hungary")).thenReturn(Optional.of(existing));

        service.deleteEmergencyAuthorityByCountryName("hungary");

        verify(emergencyAuthorityRepository).delete(existing);
    }

    @Test
    void deleteEmergencyAuthorityByCountryName_throws_whenNotFound() {
        when(emergencyAuthorityRepository.findByCountryNameIgnoreCase("Atlantis")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteEmergencyAuthorityByCountryName("Atlantis"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- read paths ----------

    @Test
    void getAllEmergencyAuthorities_returnsMappedList() {
        when(emergencyAuthorityRepository.findAll()).thenReturn(List.of(
                buildAuthority(1L, "HU", "Hungary", "107", "104", "112"),
                buildAuthority(2L, "DE", "Germany", "110", "112", "112")
        ));

        List<EmergencyAuthorityDto> result = service.getAllEmergencyAuthorities();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EmergencyAuthorityDto::getCountryCode)
                .containsExactlyInAnyOrder("HU", "DE");
    }

    @Test
    void findEmergencyAuthorityById_returnsDto_whenFound() {
        when(emergencyAuthorityRepository.findById(1L))
                .thenReturn(Optional.of(buildAuthority(1L, "HU", "Hungary", "107", "104", "112")));

        EmergencyAuthorityDto result = service.findEmergencyAuthorityById(1L);

        assertThat(result.getCountryCode()).isEqualTo("HU");
    }

    @Test
    void findEmergencyAuthorityById_throws_whenNotFound() {
        when(emergencyAuthorityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findEmergencyAuthorityById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findByCountryCode_normalizesInput_trimsAndUppercases() {
        when(emergencyAuthorityRepository.findByCountryCode("HU"))
                .thenReturn(Optional.of(buildAuthority(1L, "HU", "Hungary", "107", "104", "112")));

        EmergencyAuthorityDto result = service.findByCountryCode("  hu  "); // lowercase with whitespace

        assertThat(result.getCountryCode()).isEqualTo("HU");
        verify(emergencyAuthorityRepository).findByCountryCode("HU"); // confirms normalization happened
    }

    @Test
    void findByCountryCode_throws_whenNotFound() {
        when(emergencyAuthorityRepository.findByCountryCode("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCountryCode("xx"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByCountryName_isCaseInsensitive() {
        when(emergencyAuthorityRepository.findByCountryNameIgnoreCase("hungary"))
                .thenReturn(Optional.of(buildAuthority(1L, "HU", "Hungary", "107", "104", "112")));

        EmergencyAuthorityDto result = service.findByCountryName("hungary");

        assertThat(result.getCountryName()).isEqualTo("Hungary");
    }

    @Test
    void findByCountryName_throws_whenNotFound() {
        when(emergencyAuthorityRepository.findByCountryNameIgnoreCase("Narnia")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCountryName("Narnia"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- findEmergencyAuthorityByLocation ----------

    @Test
    void findEmergencyAuthorityByLocation_success() {
        when(googleMapsClient.reverseGeocodeCountryCode(47.5, 21.6)).thenReturn("HU");
        when(emergencyAuthorityRepository.findByCountryCode("HU"))
                .thenReturn(Optional.of(buildAuthority(1L, "HU", "Hungary", "107", "104", "112")));

        EmergencyAuthorityDto result = service.findEmergencyAuthorityByLocation(47.5, 21.6);

        assertThat(result.getCountryCode()).isEqualTo("HU");
        assertThat(result.getPoliceNumber()).isEqualTo("107");
    }

    @Test
    void findEmergencyAuthorityByLocation_throws_whenCountryNotSeeded() {
        when(googleMapsClient.reverseGeocodeCountryCode(48.8, 2.3)).thenReturn("FR");
        when(emergencyAuthorityRepository.findByCountryCode("FR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findEmergencyAuthorityByLocation(48.8, 2.3))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("FR");
    }

    @Test
    void findEmergencyAuthorityByLocation_propagatesGeocodingFailure() {
        when(googleMapsClient.reverseGeocodeCountryCode(0.0, 0.0))
                .thenThrow(new com.samwallflower.safewalk.exception.ResourceProcessingException(
                        "Reverse geocoding unavailable. Please try again later."));

        assertThatThrownBy(() -> service.findEmergencyAuthorityByLocation(0.0, 0.0))
                .isInstanceOf(com.samwallflower.safewalk.exception.ResourceProcessingException.class);

        verifyNoInteractions(emergencyAuthorityRepository);
    }
}
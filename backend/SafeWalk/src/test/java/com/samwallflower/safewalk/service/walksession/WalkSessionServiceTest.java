package com.samwallflower.safewalk.service.walksession;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.model.Route;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.model.WalkSession;
import com.samwallflower.safewalk.repository.RouteRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.repository.WalkSessionRepository;
import com.samwallflower.safewalk.request.walksession.AddWalkSessionRequest;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalkSessionServiceTest {

    @Mock
    private WalkSessionRepository walkSessionRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalkSessionService walkSessionService;

    private User user;
    private Route route;
    private WalkSession walkSession;
    private WalkSessionDto walkSessionDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setWalkSessions(new ArrayList<>());

        route = new Route();
        route.setId(10L);
        route.setWalkSessions(new ArrayList<>());

        walkSession = new WalkSession();
        walkSession.setId(100L);
        walkSession.setUser(user);
        walkSession.setRoute(route);
        walkSession.setStatus(SessionStatus.ACTIVE);

        walkSessionDto = new WalkSessionDto();
        walkSessionDto.setId(100L);
    }

    @Test
    void startWalkSessionDto_Success() {
        // Arrange
        AddWalkSessionRequest request = new AddWalkSessionRequest();
        request.setChosenRouteId(10L);
        request.setOriginLatitude(40.7128);
        request.setOriginLongitude(-74.0060);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(walkSessionRepository.save(any(WalkSession.class))).thenReturn(walkSession);
        when(modelMapper.map(any(WalkSession.class), eq(WalkSessionDto.class))).thenReturn(walkSessionDto);

        // Act
        WalkSessionDto result = walkSessionService.startWalkSessionDto(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(userRepository, times(1)).save(user);
        verify(routeRepository, times(1)).save(route);
        assertTrue(user.getWalkSessions().contains(walkSession));
    }

    @Test
    void startWalkSessionDto_UserNotFound_ThrowsException() {
        // Arrange
        AddWalkSessionRequest request = new AddWalkSessionRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> walkSessionService.startWalkSessionDto(1L, request));
        verify(walkSessionRepository, never()).save(any());
    }

    @Test
    void updateLocation_Success() {
        // Arrange
        UpdateWalkSession request = new UpdateWalkSession();
        request.setLatitude(40.7130);
        request.setLongitude(-74.0065);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walkSessionRepository.findById(100L)).thenReturn(Optional.of(walkSession));
        when(walkSessionRepository.save(any(WalkSession.class))).thenReturn(walkSession);
        when(modelMapper.map(any(WalkSession.class), eq(WalkSessionDto.class))).thenReturn(walkSessionDto);

        // Act
        WalkSessionDto result = walkSessionService.updateLocation(100L, 1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(40.7130, walkSession.getLastKnownLatitude());
        verify(walkSessionRepository, times(1)).save(walkSession);
    }

    @Test
    void updateLocation_UnauthorizedUser_ThrowsException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(2L); // Different user ID

        UpdateWalkSession request = new UpdateWalkSession();

        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(walkSessionRepository.findById(100L)).thenReturn(Optional.of(walkSession)); // Session belongs to user 1L

        // Act & Assert
        ResourceProcessingException exception = assertThrows(ResourceProcessingException.class,
                () -> walkSessionService.updateLocation(100L, 2L, request));
        assertTrue(exception.getMessage().contains("does not belong to user"));
    }

    @Test
    void endSessionByIdAndUserId_Success() {
        // Arrange
        when(walkSessionRepository.findById(100L)).thenReturn(Optional.of(walkSession));
        when(walkSessionRepository.save(any(WalkSession.class))).thenReturn(walkSession);
        when(modelMapper.map(any(WalkSession.class), eq(WalkSessionDto.class))).thenReturn(walkSessionDto);

        // Act
        WalkSessionDto result = walkSessionService.endSessionByIdAndUserId(100L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(SessionStatus.COMPLETED, walkSession.getStatus());
        assertNotNull(walkSession.getEndTime());
        verify(walkSessionRepository, times(1)).save(walkSession);
    }

    @Test
    void endSessionByIdAndUserId_AlreadyCompleted_ThrowsException() {
        // Arrange
        walkSession.setStatus(SessionStatus.COMPLETED);
        when(walkSessionRepository.findById(100L)).thenReturn(Optional.of(walkSession));

        // Act & Assert
        assertThrows(ResourceProcessingException.class, () -> walkSessionService.endSessionByIdAndUserId(100L, 1L));
    }

    @Test
    void getWalkSessionByStatus_InvalidStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> walkSessionService.getWalkSessionByStatus("UNKNOWN_STATUS"));
    }
}
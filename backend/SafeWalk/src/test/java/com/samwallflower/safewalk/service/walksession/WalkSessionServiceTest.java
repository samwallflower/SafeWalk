package com.samwallflower.safewalk.service.walksession;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.exception.RateLimitExceededException;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalkSessionServiceTest {

    @Mock private WalkSessionRepository walkSessionRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private WalkSessionService service;

    @BeforeEach
    void setUp() {
        service = new WalkSessionService(walkSessionRepository, new ModelMapper(), routeRepository, userRepository);
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setWalkSessions(new ArrayList<>());
        return u;
    }

    private Route buildRoute(Long id) {
        Route r = new Route();
        r.setId(id);
        r.setWalkSessions(new ArrayList<>());
        return r;
    }

    private AddWalkSessionRequest buildAddRequest(Long routeId) {
        AddWalkSessionRequest request = new AddWalkSessionRequest();
        request.setOriginLatitude(47.4979);
        request.setOriginLongitude(21.6244);
        request.setDestinationLatitude(47.5316);
        request.setDestinationLongitude(21.6273);
        request.setChosenRouteId(routeId);
        return request;
    }

    private WalkSession buildSession(Long id, User user, Route route, SessionStatus status) {
        WalkSession ws = new WalkSession();
        ws.setId(id);
        ws.setUser(user);
        ws.setRoute(route);
        ws.setStatus(status);
        return ws;
    }

    // ---------- startWalkSessionDto ----------

    @Test
    void startWalkSession_throws_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startWalkSessionDto(1L, buildAddRequest(10L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(routeRepository);
    }

    @Test
    void startWalkSession_throws_whenRouteNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(routeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startWalkSessionDto(1L, buildAddRequest(10L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void startWalkSession_throws_whenPriorSessionNotCompleted() {
        User user = buildUser(1L);
        Route route = buildRoute(10L);
        WalkSession priorSession = buildSession(5L, user, route, SessionStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(walkSessionRepository.findTopByUserIdOrderByStartTimeDesc(1L))
                .thenReturn(Optional.of(priorSession));

        assertThatThrownBy(() -> service.startWalkSessionDto(1L, buildAddRequest(10L)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("5"); // prior session id in the message

        verify(walkSessionRepository, never()).save(any());
    }

    @Test
    void startWalkSession_success_whenPriorSessionCompleted() {
        User user = buildUser(1L);
        Route route = buildRoute(10L);
        WalkSession priorSession = buildSession(5L, user, route, SessionStatus.COMPLETED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(walkSessionRepository.findTopByUserIdOrderByStartTimeDesc(1L))
                .thenReturn(Optional.of(priorSession));
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(inv -> {
            WalkSession ws = inv.getArgument(0);
            ws.setId(20L);
            return ws;
        });

        WalkSessionDto result = service.startWalkSessionDto(1L, buildAddRequest(10L));

        assertThat(result.getId()).isEqualTo(20L);
        verify(userRepository).save(user);
        verify(routeRepository).save(route);
        assertThat(user.getWalkSessions()).hasSize(1);
        assertThat(route.getWalkSessions()).hasSize(1);
    }

    @Test
    void startWalkSession_success_whenNoPriorSession() {
        User user = buildUser(1L);
        Route route = buildRoute(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(walkSessionRepository.findTopByUserIdOrderByStartTimeDesc(1L))
                .thenReturn(Optional.empty());
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(inv -> {
            WalkSession ws = inv.getArgument(0);
            ws.setId(20L);
            return ws;
        });

        WalkSessionDto result = service.startWalkSessionDto(1L, buildAddRequest(10L));

        assertThat(result.getId()).isEqualTo(20L);
    }

    @Test
    void startWalkSession_setsInitialLastKnownLocation_toOrigin() {
        User user = buildUser(1L);
        Route route = buildRoute(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(walkSessionRepository.findTopByUserIdOrderByStartTimeDesc(1L))
                .thenReturn(Optional.empty());
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WalkSessionDto result = service.startWalkSessionDto(1L, buildAddRequest(10L));

        assertThat(result.getLastKnownLatitude()).isEqualTo(47.4979);
        assertThat(result.getLastKnownLongitude()).isEqualTo(21.6244);
        assertThat(result.getStatus()).isEqualTo(SessionStatus.ACTIVE);
    }

    // ---------- updateLocation ----------

    @Test
    void updateLocation_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateWalkSession request = new UpdateWalkSession();

        assertThatThrownBy(() -> service.updateLocation(1L, 99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateLocation_throws_whenSessionNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(walkSessionRepository.findById(5L)).thenReturn(Optional.empty());

        UpdateWalkSession request = new UpdateWalkSession();

        assertThatThrownBy(() -> service.updateLocation(5L, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateLocation_throws_whenSessionAlreadyCompleted() {
        User user = buildUser(1L);
        WalkSession session = buildSession(5L, user, buildRoute(10L), SessionStatus.COMPLETED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));

        UpdateWalkSession request = new UpdateWalkSession();

        assertThatThrownBy(() -> service.updateLocation(5L, 1L, request))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("already completed");

        verify(walkSessionRepository, never()).save(any());
    }

    @Test
    void updateLocation_throws_whenNotOwner() {
        User owner = buildUser(1L);
        User requester = buildUser(2L);
        WalkSession session = buildSession(5L, owner, buildRoute(10L), SessionStatus.ACTIVE);

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));

        UpdateWalkSession request = new UpdateWalkSession();

        assertThatThrownBy(() -> service.updateLocation(5L, 2L, request))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void updateLocation_success_updatesLastKnownLocation() {
        User user = buildUser(1L);
        WalkSession session = buildSession(5L, user, buildRoute(10L), SessionStatus.ACTIVE);

        UpdateWalkSession request = new UpdateWalkSession();
        request.setLatitude(47.51);
        request.setLongitude(21.63);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WalkSessionDto result = service.updateLocation(5L, 1L, request);

        assertThat(result.getLastKnownLatitude()).isEqualTo(47.51);
        assertThat(result.getLastKnownLongitude()).isEqualTo(21.63);
    }

    // ---------- endSessionById (admin) ----------

    @Test
    void endSessionById_success() {
        WalkSession session = buildSession(5L, buildUser(1L), buildRoute(10L), SessionStatus.ACTIVE);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WalkSessionDto result = service.endSessionById(5L);

        assertThat(result.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getEndTime()).isNotNull();
        assertThat(session.getLastArrivedAt()).isNotNull();
    }

    @Test
    void endSessionById_throws_whenAlreadyCompleted() {
        WalkSession session = buildSession(5L, buildUser(1L), buildRoute(10L), SessionStatus.COMPLETED);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.endSessionById(5L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("already ended");
    }

    @Test
    void endSessionById_throws_whenNotFound() {
        when(walkSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.endSessionById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- endSessionByIdAndUserId ----------

    @Test
    void endSessionByIdAndUserId_success_whenOwner() {
        User user = buildUser(1L);
        WalkSession session = buildSession(5L, user, buildRoute(10L), SessionStatus.ACTIVE);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WalkSessionDto result = service.endSessionByIdAndUserId(5L, 1L);

        assertThat(result.getStatus()).isEqualTo(SessionStatus.COMPLETED);
    }

    @Test
    void endSessionByIdAndUserId_throws_whenNotOwner() {
        User owner = buildUser(1L);
        WalkSession session = buildSession(5L, owner, buildRoute(10L), SessionStatus.ACTIVE);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.endSessionByIdAndUserId(5L, 999L))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("not allowed");

        verify(walkSessionRepository, never()).save(any());
    }

    // ---------- deleteWalkSessionById ----------

    @Test
    void deleteWalkSession_success_whenOwner() {
        User user = buildUser(1L);
        Route route = buildRoute(10L);
        WalkSession session = buildSession(5L, user, route, SessionStatus.COMPLETED);
        user.getWalkSessions().add(session);
        route.getWalkSessions().add(session);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        service.deleteWalkSessionById(5L, 1L);

        assertThat(user.getWalkSessions()).doesNotContain(session);
        assertThat(route.getWalkSessions()).doesNotContain(session);
        verify(walkSessionRepository).delete(session);
    }

    @Test
    void deleteWalkSession_throws_whenNotOwner() {
        User owner = buildUser(1L);
        Route route = buildRoute(10L);
        WalkSession session = buildSession(5L, owner, route, SessionStatus.COMPLETED);

        when(userRepository.findById(999L)).thenReturn(Optional.of(buildUser(999L)));
        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> service.deleteWalkSessionById(5L, 999L))
                .isInstanceOf(ResourceProcessingException.class);

        verify(walkSessionRepository, never()).delete(any());
    }

    @Test
    void deleteWalkSession_throws_whenSessionNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(walkSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteWalkSessionById(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- updateWalkSessionStatus (admin) ----------

    @Test
    void updateWalkSessionStatus_success_validNonCompletedStatus() {
        WalkSession session = buildSession(5L, buildUser(1L), buildRoute(10L), SessionStatus.ACTIVE);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(walkSessionRepository.save(any(WalkSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WalkSessionDto result = service.updateWalkSessionStatus(5L, "emergency");

        assertThat(result.getStatus()).isEqualTo(SessionStatus.EMERGENCY);
    }

    @Test
    void updateWalkSessionStatus_throws_whenAlreadyCompleted() {
        WalkSession session = buildSession(5L, buildUser(1L), buildRoute(10L), SessionStatus.COMPLETED);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.updateWalkSessionStatus(5L, "active"))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("already ended");

        verify(walkSessionRepository, never()).save(any());
    }

    @Test
    void updateWalkSessionStatus_throws_whenTargetStatusIsCompleted() {
        WalkSession session = buildSession(5L, buildUser(1L), buildRoute(10L), SessionStatus.ACTIVE);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.updateWalkSessionStatus(5L, "completed"))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("dedicated endpoint");

        verify(walkSessionRepository, never()).save(any());
    }

    @Test
    void updateWalkSessionStatus_throws_whenStatusInvalid() {
        WalkSession session = buildSession(5L, buildUser(1L), buildRoute(10L), SessionStatus.ACTIVE);

        when(walkSessionRepository.findById(5L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.updateWalkSessionStatus(5L, "garbage"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid");
    }

    // ---------- read paths ----------

    @Test
    void getWalkSessionDtoById_throws_whenNotFound() {
        when(walkSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWalkSessionDtoById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getWalkSessionByIdAndUserId_throws_whenNotFound() {
        when(walkSessionRepository.findByUserIdAndId(1L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWalkSessionByIdAndUserId(5L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getWalkSessionsByUserId_returnsMappedList() {
        when(walkSessionRepository.findByUserId(1L)).thenReturn(List.of(
                buildSession(1L, buildUser(1L), buildRoute(10L), SessionStatus.ACTIVE),
                buildSession(2L, buildUser(1L), buildRoute(10L), SessionStatus.COMPLETED)
        ));

        List<WalkSessionDto> result = service.getWalkSessionsByUserId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getWalkSessionByStatus_throws_whenStatusInvalid() {
        assertThatThrownBy(() -> service.getWalkSessionByStatus("nonsense"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(walkSessionRepository);
    }

    @Test
    void getWalkSessionByStatus_returnsMatchingList() {
        when(walkSessionRepository.findByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(
                buildSession(1L, buildUser(1L), buildRoute(10L), SessionStatus.ACTIVE)
        ));

        List<WalkSessionDto> result = service.getWalkSessionByStatus("active");

        assertThat(result).hasSize(1);
    }

    @Test
    void getWalkSessionByRouteIdAndUserId_throws_whenNotFound() {
        when(walkSessionRepository.findByRouteIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWalkSessionByRouteIdAndUserId(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
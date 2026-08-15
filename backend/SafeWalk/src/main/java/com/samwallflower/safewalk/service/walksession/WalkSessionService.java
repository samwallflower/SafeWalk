package com.samwallflower.safewalk.service.walksession;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.enums.SessionStatus;
import com.samwallflower.safewalk.exception.RateLimitExceededException;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalkSessionService implements IWalkSessionService {
    private final WalkSessionRepository walkSessionRepository;
    private final ModelMapper modelMapper;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    // basically we get the user and the route from their respective repository
    // then we instantiate a walk session object and set relevant fields
    // then we save that object which will give us an object with id
    // then we set that saved object into our user and route object's walk session list
    // finally we save the user and route objects in their repositories
    // and return the dto
    //if someone already has active sessions they cannot start new sessions before ending the old ones
    @Override
    @Transactional
    public WalkSessionDto startWalkSessionDto(Long userId, AddWalkSessionRequest request) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + userId));
        Route route = routeRepository.findById(request.getChosenRouteId()).orElseThrow(()-> new ResourceNotFoundException("Route not found with id: " + request.getChosenRouteId()));

        walkSessionRepository.findTopByUserIdOrderByStartTimeDesc(userId)
                .ifPresent(session->{
                    if (session.getStatus()!=SessionStatus.COMPLETED){
                        throw new RateLimitExceededException("Please complete previous session with id: " + session.getId()+" before starting a new session");
                    }
                });

        WalkSession walkSession = createWalkSession(user, route, request);
        WalkSession saved = walkSessionRepository.save(walkSession);

        user.getWalkSessions().add(saved);

        route.getWalkSessions().add(saved);

        userRepository.save(user);

        routeRepository.save(route);

        return convertToDto(saved);
    }

    private WalkSession createWalkSession(User user, Route route, AddWalkSessionRequest request) {
        WalkSession walkSession = new WalkSession();
        walkSession.setUser(user);
        walkSession.setRoute(route);
        walkSession.setStartTime(LocalDateTime.now());
        walkSession.setStatus(SessionStatus.ACTIVE);
        walkSession.setOriginLatitude(request.getOriginLatitude());
        walkSession.setOriginLongitude(request.getOriginLongitude());
        walkSession.setDestinationLatitude(request.getDestinationLatitude());
        walkSession.setDestinationLongitude(request.getDestinationLongitude());
        walkSession.setLastKnownLatitude(request.getOriginLatitude());
        walkSession.setLastKnownLongitude(request.getOriginLongitude());
        walkSession.setLastLocationUpdate(LocalDateTime.now());

        return walkSession;
    }

    // we don't allow anyone update anything after a session is complete
    @Override
    @Transactional
    public WalkSessionDto updateLocation(Long id, Long userId, UpdateWalkSession request) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + userId));
        WalkSession walkSession = walkSessionRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with id: " + id));

        if(walkSession.getStatus()==SessionStatus.COMPLETED)
            throw new ResourceProcessingException("WalkSession already completed with id: " + id);

        if(!walkSession.getUser().getId().equals(user.getId())) {
            throw new ResourceProcessingException("Walk Session with id: "+ id + "does not belong to user with id: "+ userId);
        }
        walkSession.setLastKnownLatitude(request.getLatitude());
        walkSession.setLastKnownLongitude(request.getLongitude());
        walkSession.setLastLocationUpdate(LocalDateTime.now());
        WalkSession saved = walkSessionRepository.save(walkSession);
        return convertToDto(saved);
    }

    @Override
    public WalkSessionDto getWalkSessionDtoById(Long id) {
        return walkSessionRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with id: "+ id));
    }

    // for admin or autocomplete
    @Override
    @Transactional
    public WalkSessionDto endSessionById(Long id) {
        return walkSessionRepository.findById(id)
                .map(walkSession -> {
                    WalkSession saved = walkSessionRepository.save(endSession(walkSession));
                    return convertToDto(saved);
                }).orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with id"+ id));
    }

    private WalkSession endSession(WalkSession walkSession) {
        if (walkSession.getStatus().equals(SessionStatus.COMPLETED)) {
            throw new  ResourceProcessingException("WalkSession with id: "+walkSession.getId()+" has already ended");
        }
        walkSession.setEndTime(LocalDateTime.now());
        walkSession.setLastArrivedAt(LocalDateTime.now());
        walkSession.setStatus(SessionStatus.COMPLETED);
        return walkSession;
    }

    // when the user wants to end a session
    // we check if the session belongs to a user
    // we should also check if it's already completed or no
    @Override
    @Transactional
    public WalkSessionDto endSessionByIdAndUserId(Long id, Long userId) {
        return walkSessionRepository.findById(id)
                .map(walkSession->{
                    if(!walkSession.getUser().getId().equals(userId)){
                        throw new ResourceProcessingException("You are not allowed to end this session. This session does not belong to you.");
                    }
                    WalkSession saved = walkSessionRepository.save(endSession(walkSession));
                    return convertToDto(saved);
                }).orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with id"+ id));
    }

    @Override
    public WalkSessionDto getWalkSessionByIdAndUserId(Long id, Long userId) {
        return walkSessionRepository.findByUserIdAndId(userId,id)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with id "+ id +" and user id "+ userId));
    }

    @Override
    public List<WalkSessionDto> getWalkSessionsByUserId(Long userId) {
        return walkSessionRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<WalkSessionDto> getAllWalkSession() {
        return walkSessionRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // get all active sessions
    @Override
    public List<WalkSessionDto> getWalkSessionByStatus(String status) {
        return walkSessionRepository.findByStatus(resolveStatus(status))
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<WalkSessionDto> getWalkSessionByRouteId(Long routeId) {
        return walkSessionRepository.findByRouteId(routeId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteWalkSessionById(Long id, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+ userId));
        WalkSession walkSession = walkSessionRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with id: " + id));
        Route route = routeRepository.findById(walkSession.getRoute().getId()).orElseThrow(()-> new ResourceNotFoundException("Route not found with id: "+ id));
        if(!walkSession.getUser().getId().equals(userId))
            throw new ResourceProcessingException("Walk session with id: "+ id + " does not belong to user with id: "+ userId);

        user.getWalkSessions().remove(walkSession);
        userRepository.save(user);

        route.getWalkSessions().remove(walkSession);
        routeRepository.save(route);

        walkSession.setUser(null);
        walkSession.setRoute(null);

        walkSessionRepository.delete(walkSession);
    }

    // only for admin hence no user id
    // this method should not be used for ending the session as there are
    // dedicated endpoint for it
    // hence we will throw exception instead prompting the client to use the proper end point
    // first we check if the session is already ended or no
    // if it is already completed no changes are allowed
    @Override
    @Transactional
    public WalkSessionDto updateWalkSessionStatus(Long id, String status) {
        return walkSessionRepository.findById(id)
                .map(walkSession -> {
                    if(walkSession.getStatus()==SessionStatus.COMPLETED){
                        throw new  ResourceProcessingException("WalkSession with id: "+id+" has already ended");
                    }
                    SessionStatus statusResolved = resolveStatus(status);
                    if(statusResolved == SessionStatus.COMPLETED){
                        throw new ResourceProcessingException("Please use the dedicated endpoint to end a session");
                    }
                    walkSession.setStatus(statusResolved);
                    return convertToDto(walkSessionRepository.save(walkSession));
                }).orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with id: "+ id));
    }

    @Override
    public WalkSessionDto getWalkSessionByRouteIdAndUserId(Long routeId, Long userId) {
        return walkSessionRepository.findByRouteIdAndUserId(routeId,userId)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("WalkSession not found with route id: "+ routeId + " and user id: "+ userId));
    }

    @Override
    public WalkSessionDto convertToDto(WalkSession walkSession) {
        return modelMapper.map(walkSession, WalkSessionDto.class);
    }

    private SessionStatus resolveStatus(String status) {
        return switch (status.toLowerCase().trim()){
            case "active" -> SessionStatus.ACTIVE;
            case "completed" -> SessionStatus.COMPLETED;
            case "emergency" -> SessionStatus.EMERGENCY;
            case "abandoned" -> SessionStatus.ABANDONED;
            default -> throw new IllegalArgumentException("Invalid  session status " + status);
        };
    }
}

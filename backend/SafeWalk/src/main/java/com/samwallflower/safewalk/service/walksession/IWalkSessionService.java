package com.samwallflower.safewalk.service.walksession;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.model.WalkSession;
import com.samwallflower.safewalk.request.walksession.AddWalkSessionRequest;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;

import java.util.List;

public interface IWalkSessionService {
    WalkSessionDto startWalkSessionDto(Long userId, AddWalkSessionRequest  request);
    WalkSessionDto updateLocation(Long id, Long userId, UpdateWalkSession request);
    WalkSessionDto getWalkSessionDtoById(Long id);
    WalkSessionDto endSessionById(Long id);

    WalkSessionDto resolveIdleWarning(Long id, Long userId);

    WalkSessionDto endSessionByIdAndUserId(Long id, Long userId);
    WalkSessionDto getWalkSessionByIdAndUserId(Long id, Long userId);
    List<WalkSessionDto> getWalkSessionsByUserId(Long userId);
    List<WalkSessionDto> getAllWalkSession();
    List<WalkSessionDto> getWalkSessionByStatus(String status);
    List<WalkSessionDto> getWalkSessionByRouteId(Long routeId);
    List<WalkSessionDto> getWalkSessionByUserIdAndStatus(Long userId, String status);
    void deleteWalkSessionById(Long id, Long userId);
    WalkSessionDto updateWalkSessionStatus(Long id, String status);

    long countWalkSessionsByUserIdAndStatus(Long userId, String status);
    long countAllByStatus(String status);

    WalkSessionDto getWalkSessionByRouteIdAndUserId(Long routeId, Long userId);
    WalkSessionDto convertToDto(WalkSession walkSession);

}

package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.WalkSessionDto;
import com.samwallflower.safewalk.request.walksession.AddWalkSessionRequest;
import com.samwallflower.safewalk.request.walksession.UpdateWalkSession;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.walksession.IWalkSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/walk-sessions")
public class WalkSessionController {
    private final IWalkSessionService walkSessionService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/user/{userId}/add")
    public ResponseEntity<ApiResponse> startSession(@PathVariable Long userId, @Valid @RequestBody AddWalkSessionRequest request){
        WalkSessionDto walkSessionDto = walkSessionService.startWalkSessionDto(userId,request);
        return ResponseEntity.ok(new ApiResponse("WalkSession started successfully", walkSessionDto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}/session/user/{userId}/update/location")
    public ResponseEntity<ApiResponse> updateWalkSessionLocation(@PathVariable Long id, @PathVariable Long userId, @Valid @RequestBody UpdateWalkSession request){
        WalkSessionDto walkSessionDto = walkSessionService.updateLocation(id, userId,request);
        return ResponseEntity.ok(new ApiResponse("WalkSession location updated successfully", walkSessionDto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{id}/session/user/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteWalkSession(@PathVariable Long id, @PathVariable Long userId) {
        walkSessionService.deleteWalkSessionById(id,userId);
        return ResponseEntity.ok(new ApiResponse("WalkSession deleted successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/session/end")
    public ResponseEntity<ApiResponse> endWalkSessionById(@PathVariable Long id) {
        WalkSessionDto walkSessionDto = walkSessionService.endSessionById(id);
        return ResponseEntity.ok(new ApiResponse("WalkSession ended successfully", walkSessionDto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}/session/user/{userId}/end")
    public ResponseEntity<ApiResponse> endWalkSessionByIdAndUserId(@PathVariable Long id, @PathVariable Long userId) {
        WalkSessionDto walkSessionDto = walkSessionService.endSessionByIdAndUserId(id,userId);
        return ResponseEntity.ok(new ApiResponse("WalkSession ended successfully", walkSessionDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/session/update/by-status")
    public ResponseEntity<ApiResponse> updateWalkSessionStatus(@PathVariable Long id, @RequestParam String status) {
        WalkSessionDto walkSessionDto = walkSessionService.updateWalkSessionStatus(id,status);
        return ResponseEntity.ok(new ApiResponse("WalkSession status updated successfully", walkSessionDto));
    }

    // now all get methods
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllWalkSessions(){
        List<WalkSessionDto> walkSessions = walkSessionService.getAllWalkSession();
        return ResponseEntity.ok(new ApiResponse("WalkSessions found successfully", walkSessions));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/session")
    public ResponseEntity<ApiResponse> getWalkSessionById(@PathVariable Long id){
        WalkSessionDto walkSessionDto = walkSessionService.getWalkSessionDtoById(id);
        return ResponseEntity.ok(new ApiResponse("WalkSession found successfully", walkSessionDto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}/user/{userId}/session")
    public ResponseEntity<ApiResponse> getWalkSessionByIdAndUserId(@PathVariable Long id,@PathVariable Long userId){
        WalkSessionDto walkSessionDto = walkSessionService.getWalkSessionByIdAndUserId(id,userId);
        return ResponseEntity.ok(new ApiResponse("WalkSession found successfully", walkSessionDto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}/session")
    public ResponseEntity<ApiResponse> getWalkSessionsByUserId(@PathVariable Long userId){
        List<WalkSessionDto> walkSessionDtos = walkSessionService.getWalkSessionsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("WalkSessions found successfully", walkSessionDtos));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/route/{routeId}/user/{userId}/session")
    public ResponseEntity<ApiResponse> getWalkSessionByRouteIdAndUserId(@PathVariable Long routeId, @PathVariable Long userId){
        WalkSessionDto walkSessionDto = walkSessionService.getWalkSessionByRouteIdAndUserId(routeId,userId);
        return ResponseEntity.ok(new ApiResponse("WalkSession found successfully", walkSessionDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/route/{routeId}/session")
    public ResponseEntity<ApiResponse> getWalkSessionsByRouteId(@PathVariable Long routeId){
        List<WalkSessionDto> sessions = walkSessionService.getWalkSessionByRouteId(routeId);
        return ResponseEntity.ok(new ApiResponse("WalkSessions found successfully", sessions));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-status/session")
    public ResponseEntity<ApiResponse> getWalkSessionsByStatus(@RequestParam String status){
        List<WalkSessionDto> sessions = walkSessionService.getWalkSessionByStatus(status);
        return ResponseEntity.ok(new ApiResponse("WalkSessions found successfully", sessions));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/by-user-id-and-status/user/{userId}/session")
    public ResponseEntity<ApiResponse> countWalkSessionsByUserIdAndStatus(@PathVariable Long userId, @RequestParam String status){
        long count = walkSessionService.countWalkSessionsByUserIdAndStatus(userId,status);
        return ResponseEntity.ok(new ApiResponse("WalkSessions count found successfully", count));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/by-status/session")
    public ResponseEntity<ApiResponse> countWalkSessionsByStatus(@RequestParam String status){
        long count = walkSessionService.countAllByStatus(status);
        return ResponseEntity.ok(new ApiResponse("WalkSessions count found successfully", count));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/by-user-id-and-status/user/{userId}/session")
    public ResponseEntity<ApiResponse> getWalkSessionByUserIdAndStatus(@PathVariable Long userId, @RequestParam String status){
        List<WalkSessionDto> sessions = walkSessionService.getWalkSessionByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(new ApiResponse("WalkSessions found successfully", sessions));
    }

}

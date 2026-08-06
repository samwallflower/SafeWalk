package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.IncidentCategoryDto;
import com.samwallflower.safewalk.request.incidentcategory.AddIncidentCategoryRequest;
import com.samwallflower.safewalk.request.incidentcategory.UpdateIncidentCategoryRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.incidentcategory.IIncidentCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/incident-categories")
public class IncidentCategoryController {
    private final IIncidentCategoryService incidentCategoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllIncidentCategories() {
        List<IncidentCategoryDto> incidentCategories = incidentCategoryService.getAllIncidentCategories();
        return ResponseEntity.ok(new ApiResponse("Incident categories retrieved successfully", incidentCategories));
    }

    @GetMapping("/{id}/category")
    public ResponseEntity<ApiResponse> getIncidentCategoryById(@PathVariable Long id) {
        IncidentCategoryDto incidentCategory = incidentCategoryService.getIncidentCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Incident category retrieved successfully", incidentCategory));
    }

    @GetMapping("/by-name/category")
    public ResponseEntity<ApiResponse> getIncidentCategoryByName(@RequestParam String name) {
        IncidentCategoryDto incidentCategory = incidentCategoryService.getIncidentCategoryByName(name);
        return ResponseEntity.ok(new ApiResponse("Incident category retrieved successfully", incidentCategory));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addIncidentCategory(@RequestBody AddIncidentCategoryRequest incidentCategoryDto) {
        IncidentCategoryDto createdIncidentCategory = incidentCategoryService.addIncidentCategory(incidentCategoryDto);
        return ResponseEntity.ok(new ApiResponse("Incident category added successfully", createdIncidentCategory));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse> updateIncidentCategory(@PathVariable Long id, @RequestBody UpdateIncidentCategoryRequest incidentCategoryDto) {
        IncidentCategoryDto updatedIncidentCategory = incidentCategoryService.updateIncidentCategory(id, incidentCategoryDto);
        return ResponseEntity.ok(new ApiResponse("Incident category updated successfully", updatedIncidentCategory));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteIncidentCategory(@PathVariable Long id) {
        incidentCategoryService.deleteIncidentCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Incident category deleted successfully", null));
    }

    @GetMapping("/by-severity-weight/category")
    public ResponseEntity<ApiResponse> getIncidentCategoryBySeverity(@RequestParam Integer severity) {
        List<IncidentCategoryDto> incidentCategories = incidentCategoryService.getIncidentCategoryBySeverity(severity);
        return ResponseEntity.ok(new ApiResponse("Incident categories retrieved successfully", incidentCategories));
    }
}

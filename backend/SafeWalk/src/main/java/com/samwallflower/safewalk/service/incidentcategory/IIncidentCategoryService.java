package com.samwallflower.safewalk.service.incidentcategory;

import com.samwallflower.safewalk.dto.IncidentCategoryDto;
import com.samwallflower.safewalk.model.IncidentCategory;
import com.samwallflower.safewalk.request.incidentcategory.AddIncidentCategoryRequest;
import com.samwallflower.safewalk.request.incidentcategory.UpdateIncidentCategoryRequest;

import java.util.List;

public interface IIncidentCategoryService {

    List<IncidentCategoryDto> getAllIncidentCategories();

    IncidentCategoryDto getIncidentCategoryById(Long id);
    IncidentCategoryDto getIncidentCategoryByName(String name);
    List<IncidentCategoryDto> getIncidentCategoryBySeverity(Integer severity);
    // only admin should be able to create delete update categories
    IncidentCategoryDto addIncidentCategory(AddIncidentCategoryRequest categoryRequest);
    IncidentCategoryDto updateIncidentCategory(Long categoryId, UpdateIncidentCategoryRequest categoryRequest);
    void deleteIncidentCategoryById(Long id);

    IncidentCategoryDto convertToDto(IncidentCategory incidentCategory);
}

package com.samwallflower.safewalk.service.incidentcategory;

import com.samwallflower.safewalk.dto.IncidentCategoryDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.model.IncidentCategory;
import com.samwallflower.safewalk.repository.IncidentCategoryRepository;
import com.samwallflower.safewalk.request.incidentcategory.AddIncidentCategoryRequest;
import com.samwallflower.safewalk.request.incidentcategory.UpdateIncidentCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncidentCategoryService implements IIncidentCategoryService{
    private final IncidentCategoryRepository incidentCategoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<IncidentCategoryDto> getAllIncidentCategories() {
        return incidentCategoryRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public IncidentCategoryDto getIncidentCategoryById(Long id) {
        return incidentCategoryRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Incident Category not found with id: " + id));
    }

    @Override
    public IncidentCategoryDto getIncidentCategoryByName(String name) {

        return incidentCategoryRepository.findByNameIgnoreCase(name)
                .map(this::convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Incident Category not found with name: " + name));
    }

    @Override
    public List<IncidentCategoryDto> getIncidentCategoryBySeverity(Integer severity) {
        return incidentCategoryRepository.findAllBySeverityWeight(severity).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public IncidentCategoryDto addIncidentCategory(AddIncidentCategoryRequest categoryRequest) {
        IncidentCategory newCategory = new IncidentCategory();
        newCategory.setName(categoryRequest.getName());
        newCategory.setSeverityWeight(categoryRequest.getSeverityWeight());
        Optional.ofNullable(categoryRequest.getDescription()).ifPresent(newCategory::setDescription);
        incidentCategoryRepository.save(newCategory);
        return convertToDto(newCategory);
    }

    @Override
    public IncidentCategoryDto updateIncidentCategory(Long categoryId, UpdateIncidentCategoryRequest categoryRequest) {
        return incidentCategoryRepository.findById(categoryId)
                .map(existingCategory -> {
                    Optional.ofNullable(categoryRequest.getName()).ifPresent(existingCategory::setName);
                    Optional.ofNullable(categoryRequest.getSeverityWeight()).ifPresent(existingCategory::setSeverityWeight);
                    Optional.ofNullable(categoryRequest.getDescription()).ifPresent(existingCategory::setDescription);
                    incidentCategoryRepository.save(existingCategory);
                    return convertToDto(existingCategory);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Incident Category not found with id: " + categoryId));
    }

    @Override
    public void deleteIncidentCategoryById(Long id) {
        IncidentCategory category = incidentCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident Category not found with id: " + id));
        incidentCategoryRepository.delete(category);
    }

    @Override
    public IncidentCategoryDto convertToDto(IncidentCategory incidentCategory) {
        return modelMapper.map(incidentCategory, IncidentCategoryDto.class);
    }
}

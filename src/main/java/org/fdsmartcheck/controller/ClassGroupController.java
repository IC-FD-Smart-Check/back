package org.fdsmartcheck.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.ClassGroupRequest;
import org.fdsmartcheck.dto.response.ClassGroupResponse;
import org.fdsmartcheck.service.ClassGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/class-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @PostMapping
    public ResponseEntity<ClassGroupResponse> createClassGroup(@Valid @RequestBody ClassGroupRequest request) {
        ClassGroupResponse created = classGroupService.createClassGroup(request);
        URI location = URI.create("/api/class-groups/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ClassGroupResponse>> getAllClassGroups() {
        return ResponseEntity.ok(classGroupService.getAllClassGroups());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ClassGroupResponse>> getClassGroupsByCourseId(@PathVariable String courseId) {
        return ResponseEntity.ok(classGroupService.getClassGroupsByCourseId(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassGroupResponse> getClassGroupById(@PathVariable String id) {
        return ResponseEntity.ok(classGroupService.getClassGroupById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassGroupResponse> updateClassGroup(
            @PathVariable String id,
            @Valid @RequestBody ClassGroupRequest request) {
        return ResponseEntity.ok(classGroupService.updateClassGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassGroup(@PathVariable String id) {
        classGroupService.deleteClassGroup(id);
        return ResponseEntity.noContent().build();
    }
}

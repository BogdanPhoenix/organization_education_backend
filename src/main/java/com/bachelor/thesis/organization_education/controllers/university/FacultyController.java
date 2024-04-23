package com.bachelor.thesis.organization_education.controllers.university;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import com.bachelor.thesis.organization_education.controllers.ResourceController;
import com.bachelor.thesis.organization_education.responces.university.FacultyResponse;
import com.bachelor.thesis.organization_education.services.interfaces.university.FacultyService;
import com.bachelor.thesis.organization_education.requests.find.university.FacultyFindRequest;
import com.bachelor.thesis.organization_education.requests.insert.university.FacultyInsertRequest;
import com.bachelor.thesis.organization_education.requests.update.university.FacultyUpdateRequest;

import java.security.Principal;

@RestController
@RequestMapping("/faculty")
public class FacultyController extends ResourceController<FacultyService> {
    @Autowired
    public FacultyController(FacultyService service) {
        super(service);
    }

    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    @PostMapping
    public ResponseEntity<FacultyResponse> add(
            @RequestBody @Valid FacultyInsertRequest request,
            Principal principal
    ) {
        var response = service.addResource(request, principal.getName())
                .getResponse();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<FacultyResponse> get(@RequestBody @Valid FacultyFindRequest request) {
        return super.get(request);
    }

    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FacultyResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid FacultyUpdateRequest request
    ) {
        return super.updateEntity(id, request);
    }

    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    @DeleteMapping
    public ResponseEntity<Void> deactivate(@RequestBody @Valid FacultyFindRequest request) {
        return super.deactivate(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody @Valid FacultyFindRequest request) {
        return super.delete(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate")
    public ResponseEntity<Void> activate(@RequestBody @Valid FacultyFindRequest request) {
        return super.activate(request);
    }
}

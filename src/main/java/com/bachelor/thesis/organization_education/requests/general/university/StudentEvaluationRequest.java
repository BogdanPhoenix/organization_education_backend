package com.bachelor.thesis.organization_education.requests.general.university;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.bachelor.thesis.organization_education.dto.Student;
import com.bachelor.thesis.organization_education.dto.ClassRecording;
import com.bachelor.thesis.organization_education.annotations.ProhibitValueAssignment;
import com.bachelor.thesis.organization_education.requests.update.abstracts.UpdateRequest;
import com.bachelor.thesis.organization_education.requests.general.abstracts.InsertRequest;
import com.bachelor.thesis.organization_education.requests.find.university.StudentEvaluationFindRequest;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@GroupSequence({StudentEvaluationRequest.class, InsertRequest.class, UpdateRequest.class})
public class StudentEvaluationRequest implements InsertRequest, UpdateRequest {
    @NotNull(groups = InsertRequest.class)
    @ProhibitValueAssignment(groups = UpdateRequest.class)
    private Student student;

    @NotNull(groups = InsertRequest.class)
    @ProhibitValueAssignment(groups = UpdateRequest.class)
    private ClassRecording classRecording;

    @Min(value = 0, groups = {InsertRequest.class, UpdateRequest.class})
    @Max(value = 100, groups = {InsertRequest.class, UpdateRequest.class})
    @NotNull(groups = InsertRequest.class)
    private Short evaluation;

    @NotNull(groups = InsertRequest.class)
    private boolean present;

    @Override
    public StudentEvaluationFindRequest getFindRequest() {
        return StudentEvaluationFindRequest
                .builder()
                .student(student)
                .classRecording(classRecording)
                .build();
    }
}

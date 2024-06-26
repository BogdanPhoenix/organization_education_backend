package com.bachelor.thesis.organization_education.requests.general.university;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;
import com.bachelor.thesis.organization_education.dto.Audience;
import com.bachelor.thesis.organization_education.enums.DayWeek;
import com.bachelor.thesis.organization_education.enums.Frequency;
import com.bachelor.thesis.organization_education.enums.TypeClass;
import com.bachelor.thesis.organization_education.dto.GroupDiscipline;
import com.bachelor.thesis.organization_education.annotations.ProhibitValueAssignment;
import com.bachelor.thesis.organization_education.requests.general.abstracts.TimeRange;
import com.bachelor.thesis.organization_education.requests.update.abstracts.UpdateRequest;
import com.bachelor.thesis.organization_education.requests.general.abstracts.InsertRequest;
import com.bachelor.thesis.organization_education.requests.find.university.ScheduleFindRequest;

import java.sql.Time;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@GroupSequence({ScheduleRequest.class, InsertRequest.class, UpdateRequest.class})
public class ScheduleRequest implements InsertRequest, UpdateRequest, TimeRange {
    @NotNull(groups = InsertRequest.class)
    @ProhibitValueAssignment(groups = UpdateRequest.class)
    private GroupDiscipline groupDiscipline;

    @NotNull(groups = InsertRequest.class)
    private Audience audience;

    @NotNull(groups = InsertRequest.class)
    @ProhibitValueAssignment(groups = UpdateRequest.class)
    private TypeClass typeClass;

    @NotNull(groups = InsertRequest.class)
    private DayWeek dayWeek;

    @NotNull(groups = InsertRequest.class)
    private Frequency frequency;

    @NotNull(groups = {InsertRequest.class, UpdateRequest.class})
    private Time startTime;

    @NotNull(groups = {InsertRequest.class, UpdateRequest.class})
    private Time endTime;

    @Override
    public ScheduleFindRequest getFindRequest() {
        return ScheduleFindRequest.builder()
                .groupDiscipline(groupDiscipline)
                .typeClass(typeClass)
                .build();
    }
}

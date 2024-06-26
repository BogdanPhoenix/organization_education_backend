package com.bachelor.thesis.organization_education.services.implementations.university;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.bachelor.thesis.organization_education.dto.Schedule;
import com.bachelor.thesis.organization_education.dto.GroupDiscipline;
import com.bachelor.thesis.organization_education.exceptions.DuplicateException;
import com.bachelor.thesis.organization_education.dto.abstract_type.BaseTableInfo;
import com.bachelor.thesis.organization_education.responces.abstract_type.Response;
import com.bachelor.thesis.organization_education.requests.find.abstracts.FindRequest;
import com.bachelor.thesis.organization_education.requests.update.abstracts.UpdateRequest;
import com.bachelor.thesis.organization_education.requests.general.abstracts.InsertRequest;
import com.bachelor.thesis.organization_education.repositories.university.ScheduleRepository;
import com.bachelor.thesis.organization_education.requests.general.university.ScheduleRequest;
import com.bachelor.thesis.organization_education.exceptions.NotFindEntityInDataBaseException;
import com.bachelor.thesis.organization_education.requests.find.university.ScheduleFindRequest;
import com.bachelor.thesis.organization_education.services.interfaces.university.ScheduleService;
import com.bachelor.thesis.organization_education.services.implementations.crud.CrudServiceAbstract;

import java.util.List;
import java.util.UUID;

@Service
public class ScheduleServiceImpl extends CrudServiceAbstract<Schedule, ScheduleRepository> implements ScheduleService {
    @Autowired
    public ScheduleServiceImpl(ScheduleRepository repository, ApplicationContext context) {
        super(repository, "Schedules", context);
    }

    @Override
    protected Schedule createEntity(InsertRequest request) {
        var insertRequest = (ScheduleRequest) request;
        return Schedule.builder()
                .groupDiscipline(insertRequest.getGroupDiscipline())
                .audience(insertRequest.getAudience())
                .typeClass(insertRequest.getTypeClass())
                .dayWeek(insertRequest.getDayWeek())
                .frequency(insertRequest.getFrequency())
                .startTime(insertRequest.getStartTime())
                .endTime(insertRequest.getEndTime())
                .build();
    }

    @Override
    protected List<Schedule> findAllEntitiesByRequest(@NonNull FindRequest request) {
        var findRequest = (ScheduleFindRequest) request;
        return repository.findAllByGroupDisciplineAndTypeClass(
                findRequest.getGroupDiscipline(),
                findRequest.getTypeClass()
        );
    }

    @Override
    public Schedule addValue(@NonNull InsertRequest request) throws DuplicateException, NullPointerException {
        var insertRequest = (ScheduleRequest) request;

        validateMatchesSchedulesByLecturer(insertRequest);
        validateMatchesSchedulesByAudience(insertRequest);

        return super.addValue(request);
    }

    @Override
    public Schedule updateValue(@NonNull UUID id, @NonNull UpdateRequest request) throws DuplicateException, NotFindEntityInDataBaseException {
        var updateRequest = (ScheduleRequest) request;
        var entity = findValueById(id);
        updateRequest.setGroupDiscipline(entity.getGroupDiscipline());
        updateRequest.setTypeClass(entity.getTypeClass());

        validateMatchesSchedulesByLecturer(updateRequest);
        validateMatchesSchedulesByAudience(updateRequest);

        return super.updateValue(id, request);
    }

    private void validateMatchesSchedulesByLecturer(ScheduleRequest request) {
        var schedules = repository.findForMatchesByLecturer(
                request.getGroupDiscipline().getLecturer(),
                request.getDayWeek(),
                request.getStartTime(),
                request.getEndTime()
        );

        handleScheduleValidation(schedules, request.getGroupDiscipline(), FindBy.LECTURER);
    }

    private void validateMatchesSchedulesByAudience(ScheduleRequest request) {
        var schedules = repository.findForMatchesByAuditory(
                request.getAudience(),
                request.getDayWeek(),
                request.getStartTime(),
                request.getEndTime()
        );

        handleScheduleValidation(schedules, request.getGroupDiscipline(), FindBy.AUDIENCE);
    }

    private void handleScheduleValidation(List<Schedule> schedules, GroupDiscipline groupDiscipline, FindBy type) {
        var conflictingSchedule = schedules.stream()
                .filter(schedule -> !schedule.getGroupDiscipline().equals(groupDiscipline))
                .findFirst();

        if (conflictingSchedule.isPresent()) {
            var errorMessage = switch (type) {
                case LECTURER ->
                        "It's not possible to add the specified entity. The specified teacher already has another class scheduled for the specified hour.";
                case AUDIENCE ->
                        "The specified entity cannot be added. The audience is already occupied for the specified time period.";
            };
            throw new DuplicateException(errorMessage);
        }

        if (!schedules.isEmpty()) {
            var errorMessage = switch (type) {
                case LECTURER -> "The lesson to be held during the specified time period is already saved in the database.";
                case AUDIENCE -> "The database already contains a record of a class in the specified classroom.";
            };
            throw new DuplicateException(errorMessage);
        }
    }

    @Override
    protected void updateEntity(Schedule entity, UpdateRequest request) {
        var updateRequest = (ScheduleRequest) request;

        updateIfPresent(updateRequest::getAudience, entity::setAudience);
        updateIfPresent(updateRequest::getDayWeek, entity::setDayWeek);
        updateIfPresent(updateRequest::getFrequency, entity::setFrequency);
        updateIfPresent(updateRequest::getStartTime, entity::setStartTime);
        updateIfPresent(updateRequest::getEndTime, entity::setEndTime);
    }

    @Override
    protected boolean checkOwner(Schedule entity, UUID userId) {
        return entity.getAudience()
                .getUniversity()
                .getAdminId()
                .equals(userId);
    }

    @Override
    public Page<Response> getAllByUniversityAdmin(@NonNull Pageable pageable) {
        var uuid = super.getAuthenticationUUID();
        return repository.findAllByUniversityAdmin(uuid, pageable)
                .map(BaseTableInfo::getResponse);
    }

    @Override
    public Page<Response> getAllByLecturer(@NonNull Pageable pageable) {
        var uuid = super.getAuthenticationUUID();
        return repository.findAllByLecturer(uuid, pageable)
                .map(BaseTableInfo::getResponse);
    }

    @Override
    public Page<Response> getAllByStudent(@NonNull Pageable pageable) {
        var uuid = super.getAuthenticationUUID();
        return repository.findAllByStudent(uuid, pageable)
                .map(BaseTableInfo::getResponse);
    }

    private enum FindBy {
        LECTURER, AUDIENCE
    }
}

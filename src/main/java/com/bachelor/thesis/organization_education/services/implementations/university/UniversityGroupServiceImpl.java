package com.bachelor.thesis.organization_education.services.implementations.university;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.bachelor.thesis.organization_education.dto.UniversityGroup;
import com.bachelor.thesis.organization_education.dto.abstract_type.BaseTableInfo;
import com.bachelor.thesis.organization_education.responces.abstract_type.Response;
import com.bachelor.thesis.organization_education.requests.find.abstracts.FindRequest;
import com.bachelor.thesis.organization_education.requests.update.abstracts.UpdateRequest;
import com.bachelor.thesis.organization_education.requests.general.abstracts.InsertRequest;
import com.bachelor.thesis.organization_education.exceptions.NotFindEntityInDataBaseException;
import com.bachelor.thesis.organization_education.services.implementations.user.StudentServiceImpl;
import com.bachelor.thesis.organization_education.services.implementations.crud.CrudServiceAbstract;
import com.bachelor.thesis.organization_education.repositories.university.UniversityGroupRepository;
import com.bachelor.thesis.organization_education.requests.general.university.UniversityGroupRequest;
import com.bachelor.thesis.organization_education.requests.find.university.UniversityGroupFindRequest;
import com.bachelor.thesis.organization_education.services.interfaces.university.UniversityGroupService;

import java.util.List;
import java.util.UUID;

@Service
public class UniversityGroupServiceImpl extends CrudServiceAbstract<UniversityGroup, UniversityGroupRepository> implements UniversityGroupService {
    @Autowired
    public UniversityGroupServiceImpl(UniversityGroupRepository repository, ApplicationContext context) {
        super(repository, "Groups", context);
    }

    @Override
    protected UniversityGroup createEntity(InsertRequest request) {
        var groupRequest = (UniversityGroupRequest) request;
        return UniversityGroup.builder()
                .curator(groupRequest.getCurator())
                .specialty(groupRequest.getSpecialty())
                .faculty(groupRequest.getFaculty())
                .yearStart(groupRequest.getYearStart())
                .yearEnd(groupRequest.getYearEnd())
                .reducedForm(groupRequest.isReducedForm())
                .build();
    }

    @Override
    public UniversityGroup updateValue(@NonNull UUID id, @NonNull UpdateRequest request) throws NotFindEntityInDataBaseException {
        var updateRequest = (UniversityGroupRequest) request;
        var entity = findValueById(id);
        updateRequest.setSpecialty(entity.getSpecialty());

        validateDuplicate(id, updateRequest.getFindRequest());
        return super.updateValue(id, updateRequest);
    }

    @Override
    protected List<UniversityGroup> findAllEntitiesByRequest(@NonNull FindRequest request) {
        var groupRequest = (UniversityGroupFindRequest) request;

        return repository.findAllBySpecialtyAndYearStartAndReducedForm(
                groupRequest.getSpecialty(),
                groupRequest.getYearStart(),
                groupRequest.isReducedForm()
        );
    }

    @Override
    protected void updateEntity(UniversityGroup entity, UpdateRequest request) {
        var updateRequest = (UniversityGroupRequest) request;

        updateIfPresent(updateRequest::getCurator, entity::setCurator);
        updateIfPresent(updateRequest::getSpecialty, entity::setSpecialty);
        updateIfPresent(updateRequest::getFaculty, entity::setFaculty);
        updateIfPresent(updateRequest::getYearStart, entity::setYearStart);
        updateIfPresent(updateRequest::getYearEnd, entity::setYearEnd);

        entity.setReducedForm(updateRequest.isReducedForm());
    }

    @Override
    protected boolean checkOwner(UniversityGroup entity, UUID userId) {
        return entity.getFaculty()
                .getUniversity()
                .getAdminId()
                .equals(userId);
    }

    @Override
    protected void selectedForDeactivateChild(UniversityGroup entity) {
        deactivatedChild(entity.getGroupsDisciplines(), GroupDisciplineServiceImpl.class);
        deactivatedChild(entity.getStudents(), StudentServiceImpl.class);
    }

    @Override
    public Page<Response> getAllByUniversityAdmin(@NonNull Pageable pageable) {
        var uuid = super.getAuthenticationUUID();
        return repository.findAll(uuid, pageable)
                .map(BaseTableInfo::getResponse);
    }
}

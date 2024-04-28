package com.bachelor.thesis.organization_education.services.implementations.university;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.bachelor.thesis.organization_education.dto.GroupDiscipline;
import com.bachelor.thesis.organization_education.exceptions.DuplicateException;
import com.bachelor.thesis.organization_education.requests.find.abstracts.FindRequest;
import com.bachelor.thesis.organization_education.requests.update.abstracts.UpdateRequest;
import com.bachelor.thesis.organization_education.requests.general.abstracts.InsertRequest;
import com.bachelor.thesis.organization_education.exceptions.NotFindEntityInDataBaseException;
import com.bachelor.thesis.organization_education.services.interfaces.university.GroupService;
import com.bachelor.thesis.organization_education.services.implementations.crud.CrudServiceAbstract;
import com.bachelor.thesis.organization_education.repositories.university.GroupDisciplineRepository;
import com.bachelor.thesis.organization_education.requests.general.university.GroupDisciplineRequest;
import com.bachelor.thesis.organization_education.requests.find.university.GroupDisciplineFindRequest;
import com.bachelor.thesis.organization_education.services.interfaces.university.GroupDisciplineService;
import com.bachelor.thesis.organization_education.services.interfaces.university.AcademicDisciplineService;

import java.util.UUID;
import java.util.Optional;

@Service
public class GroupDisciplineServiceImpl extends CrudServiceAbstract<GroupDiscipline, GroupDisciplineRepository> implements GroupDisciplineService {
    @Autowired
    public GroupDisciplineServiceImpl(GroupDisciplineRepository repository, ApplicationContext context) {
        super(repository, "Groups disciplines", context);
    }

    @Override
    protected void objectFormation(InsertRequest request) {
        var insertRequest = (GroupDisciplineRequest) request;
        insertRequest.setGroup(super.getValue(insertRequest.getGroup(), GroupService.class));
        insertRequest.setDiscipline(super.getValue(insertRequest.getDiscipline(), AcademicDisciplineService.class));
    }

    @Override
    protected GroupDiscipline createEntity(InsertRequest request) {
        var insertRequest = (GroupDisciplineRequest) request;

        return GroupDiscipline.builder()
                .group(insertRequest.getGroup())
                .discipline(insertRequest.getDiscipline())
                .amountLecture(insertRequest.getAmountLecture())
                .amountPractical(insertRequest.getAmountPractical())
                .build();
    }

    @Override
    public GroupDiscipline updateValue(@NonNull UUID id, @NonNull UpdateRequest request) throws DuplicateException, NotFindEntityInDataBaseException {
        var entity = findValueById(id);
        return updateValue(entity, request);
    }

    @Override
    protected Optional<GroupDiscipline> findEntityByRequest(@NonNull FindRequest request) {
        var findRequest = (GroupDisciplineFindRequest) request;
        return repository.findByGroupAndDiscipline(
                findRequest.getGroup(),
                findRequest.getDiscipline()
        );
    }

    @Override
    protected void updateEntity(GroupDiscipline entity, UpdateRequest request) {
        var updateRequest = (GroupDisciplineRequest) request;

        if(!updateRequest.amountLectureIsEmpty()) {
            entity.setAmountLecture(updateRequest.getAmountLecture());
        }
        if(!updateRequest.amountPracticalIsEmpty()) {
            entity.setAmountPractical(updateRequest.getAmountPractical());
        }
    }

    @Override
    protected void selectedForDeactivateChild(UUID id) { }
}

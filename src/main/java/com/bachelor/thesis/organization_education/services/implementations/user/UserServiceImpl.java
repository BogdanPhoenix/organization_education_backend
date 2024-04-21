package com.bachelor.thesis.organization_education.services.implementations.user;

import lombok.NonNull;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.keycloak.representations.idm.UserRepresentation;
import com.bachelor.thesis.organization_education.enums.Role;
import org.keycloak.representations.idm.CredentialRepresentation;
import com.bachelor.thesis.organization_education.exceptions.UserCreatingException;
import com.bachelor.thesis.organization_education.requests.general.user.AuthRequest;
import com.bachelor.thesis.organization_education.services.interfaces.user.UserService;
import com.bachelor.thesis.organization_education.requests.find.user.LectureFindRequest;
import com.bachelor.thesis.organization_education.requests.update.user.UserUpdateRequest;
import com.bachelor.thesis.organization_education.services.interfaces.user.LecturerService;
import com.bachelor.thesis.organization_education.requests.insert.abstracts.RegistrationRequest;
import com.bachelor.thesis.organization_education.services.interfaces.university.UniversityService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Keycloak keycloak;
    private final RestTemplate keycloakRestTemplate;

    private final UniversityService universityService;
    private final LecturerService lecturerService;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jwtIssuerURI;

    @Value("${keycloak.client}")
    private String clientId;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public UserRepresentation registration(@NonNull RegistrationRequest request) throws UserCreatingException {
        return registerAccountForAnotherUser(request, Role.UNIVERSITY_ADMIN);
    }

    @Override
    public ResponseEntity<String> authorization(@NonNull AuthRequest authRequest) throws RestClientException {
        var requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("grant_type", "password");
        requestBody.add("username", authRequest.getUsername());
        requestBody.add("password", authRequest.getPassword());
        requestBody.add("client_id", clientId);

        return keycloakRestTemplate.postForEntity(
                jwtIssuerURI + "/protocol/openid-connect/token",
                requestBody,
                String.class
        );
    }

    @Override
    public UserRepresentation registerAccountForAnotherUser(@NonNull RegistrationRequest request, Role role) throws UserCreatingException {
        var user = getUserRepresentation(request, role);
        var userId = createUser(user);

        if(role == Role.LECTURER) {
            lecturerService.registration(request, userId);
        }

        assignRole(userId, role.name());
        emailVerification(userId);

        return getUserById(userId);
    }

    private static @NonNull UserRepresentation getUserRepresentation(RegistrationRequest request, Role role) {
        var user = initializeUser(request);
        var credentialRepresentation = createCredentialRepresentation(request, role);
        var list = new ArrayList<CredentialRepresentation>();

        list.add(credentialRepresentation);
        user.setCredentials(list);

        return user;
    }

    private static @NonNull UserRepresentation initializeUser(RegistrationRequest request) {
        var user = new UserRepresentation();

        user.setEnabled(true);
        user.setUsername(request.getUsername());
        user.setEmail(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(false);

        return user;
    }

    private static @NonNull CredentialRepresentation createCredentialRepresentation(RegistrationRequest request, Role role) {
        var credentialRepresentation = new CredentialRepresentation();

        credentialRepresentation.setValue(request.getPassword());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(role != Role.UNIVERSITY_ADMIN);

        return credentialRepresentation;
    }

    private String createUser(UserRepresentation user) throws UserCreatingException {
        var usersResource = getUsersResource();

        try (Response response = usersResource.create(user)) {
            if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
                var errorMessage = response.readEntity(String.class);
                var message = String.format("The Identity Management server could not successfully create a user on its side. \n" +
                        "It returned an HTTP code: %d. The body of the error: %s.", response.getStatus(), errorMessage);
                throw new UserCreatingException(message);
            }

            return getUserId(response);
        }
    }

    private String getUserId(Response response) {
        var headers = response.getHeaders();
        var headerLocation = headers.get("Location");
        var valueLocation = (String) headerLocation.get(0);
        var parts = valueLocation.split("/");
        return parts[parts.length - 1];
    }

    private void assignRole(String userId, String roleName) {
        var userResource = getUserResource(userId);
        var rolesResource = getRolesResource();
        var representation = rolesResource
                .get(roleName)
                .toRepresentation();

        userResource.roles()
                .realmLevel()
                .add(Collections.singletonList(representation));
    }

    private RolesResource getRolesResource(){
        return keycloak
                .realm(realm)
                .roles();
    }

    private void emailVerification(String userId) {
        var usersResource = getUsersResource();
        usersResource.get(userId).sendVerifyEmail();
    }

    @Override
    public void deleteUserById(@NotBlank String userId) {
        getUsersResource().delete(userId);
        lecturerService.deleteValue(new LectureFindRequest(UUID.fromString(userId)));
    }

    @Override
    public void deactivateUserById(String userId) {
        universityService.deactivateUserEntity(userId);
        lecturerService.disable(new LectureFindRequest(UUID.fromString(userId)));
        updateEnable(userId, false);
    }

    @Override
    public void activate(@NotBlank String userId) {
        lecturerService.enable(new LectureFindRequest(UUID.fromString(userId)));
        updateEnable(userId, true);
    }

    private void updateEnable(String userId, boolean value) {
        var users = getUsersResource();
        var representation = users.get(userId).toRepresentation();
        representation.setEnabled(value);
        users.get(userId).update(representation);
    }

    @Override
    public UserRepresentation getUserById(String userId) throws NotFoundException {
        return getUsersResource()
                .get(userId)
                .toRepresentation();
    }

    private UsersResource getUsersResource() {
        return keycloak
                .realm(realm)
                .users();
    }

    @Override
    public void updatePassword(String userId) {
        var userResource = getUserResource(userId);
        var actions= new ArrayList<String>();
        actions.add("UPDATE_PASSWORD");
        userResource.executeActionsEmail(actions);
    }

    @Override
    public void updateData(@NonNull String userId, @NonNull UserUpdateRequest request) {
        var users = getUsersResource();
        var representation = users.get(userId).toRepresentation();

        if(!request.firstNameIsEmpty()) {
            representation.setFirstName(request.getFirstName());
        }
        if(!request.lastNameIsEmpty()) {
            representation.setLastName(request.getLastName());
        }

        users.get(userId).update(representation);
    }

    private UserResource getUserResource(String userId) {
        return getUsersResource()
                .get(userId);
    }
}

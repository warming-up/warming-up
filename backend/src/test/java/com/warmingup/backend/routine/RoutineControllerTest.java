package com.warmingup.backend.routine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.warmingup.backend.auth.dto.LoginRequest;
import com.warmingup.backend.domain.User;
import com.warmingup.backend.routine.dto.RoutineCreateRequest;
import com.warmingup.backend.routine.dto.RoutineResponse;
import com.warmingup.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RoutineControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void createsRoutineWithStepsAndChecklist() throws Exception {
        User user = saveLoginUser("routine-controller-create-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);

        HttpResponse<String> response = client.send(
                jsonPost("/api/routines", interviewRoutineRequest())
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        RoutineResponse body = objectMapper.readValue(response.body(), RoutineResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(201),
                () -> assertThat(body.id()).isNotNull(),
                () -> assertThat(body.name()).isEqualTo("면접 준비"),
                () -> assertThat(body.steps()).extracting("name").containsExactly("씻기", "옷 입기"),
                () -> assertThat(body.checklist()).extracting("name").containsExactly("신분증")
        );
    }

    @Test
    void findsAllRoutinesForCurrentUser() throws Exception {
        User user = saveLoginUser("routine-controller-findall-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);
        client.send(
                jsonPost("/api/routines", interviewRoutineRequest()).header("Cookie", cookie).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines"))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        List<RoutineResponse> body = objectMapper.readValue(response.body(), objectMapper.getTypeFactory()
                .constructCollectionType(List.class, RoutineResponse.class));

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(200),
                () -> assertThat(body).hasSize(1),
                () -> assertThat(body.get(0).name()).isEqualTo("면접 준비")
        );
    }

    @Test
    void findsRoutineById() throws Exception {
        User user = saveLoginUser("routine-controller-findbyid-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);
        RoutineResponse created = objectMapper.readValue(client.send(
                jsonPost("/api/routines", interviewRoutineRequest()).header("Cookie", cookie).build(),
                HttpResponse.BodyHandlers.ofString()
        ).body(), RoutineResponse.class);

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines/" + created.id()))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        RoutineResponse found = objectMapper.readValue(response.body(), RoutineResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(200),
                () -> assertThat(found.id()).isEqualTo(created.id()),
                () -> assertThat(found.steps()).extracting("name").containsExactly("씻기", "옷 입기")
        );
    }

    @Test
    void updatesRoutine() throws Exception {
        User user = saveLoginUser("routine-controller-update-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);
        RoutineResponse created = objectMapper.readValue(client.send(
                jsonPost("/api/routines", interviewRoutineRequest()).header("Cookie", cookie).build(),
                HttpResponse.BodyHandlers.ofString()
        ).body(), RoutineResponse.class);

        RoutineCreateRequest updateRequest = new RoutineCreateRequest(
                "면접 준비 수정",
                List.of(new RoutineCreateRequest.StepRequest("머리 말리기", 10, 1)),
                List.of("지갑")
        );
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines/" + created.id()))
                        .header("Cookie", cookie)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updateRequest)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        RoutineResponse updated = objectMapper.readValue(response.body(), RoutineResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(200),
                () -> assertThat(updated.name()).isEqualTo("면접 준비 수정"),
                () -> assertThat(updated.steps()).extracting("name").containsExactly("머리 말리기"),
                () -> assertThat(updated.checklist()).extracting("name").containsExactly("지갑")
        );
    }

    @Test
    void deletesRoutineAndHidesItFromFurtherAccess() throws Exception {
        User user = saveLoginUser("routine-controller-delete-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);
        RoutineResponse created = objectMapper.readValue(client.send(
                jsonPost("/api/routines", interviewRoutineRequest()).header("Cookie", cookie).build(),
                HttpResponse.BodyHandlers.ofString()
        ).body(), RoutineResponse.class);

        HttpResponse<String> deleteResponse = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines/" + created.id()))
                        .header("Cookie", cookie)
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> getAfterDelete = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines/" + created.id()))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertAll(
                () -> assertThat(deleteResponse.statusCode()).isEqualTo(204),
                () -> assertThat(getAfterDelete.statusCode()).isEqualTo(404)
        );
    }

    @Test
    void rejectsUnauthenticatedRequestsAsUnauthorized() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(
                jsonPost("/api/routines", interviewRoutineRequest()).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void rejectsInvalidCreateRequestAsBadRequest() throws Exception {
        User user = saveLoginUser("routine-controller-invalid-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);

        HttpResponse<String> response = client.send(
                jsonPost("/api/routines", new RoutineCreateRequest(" ", List.of(), List.of()))
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void rejectsMissingRoutineAsNotFound() throws Exception {
        User user = saveLoginUser("routine-controller-missing-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines/999999"))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(404);
    }

    // RoutineService.getRoutine throws IllegalArgumentException (400) for cross-user access,
    // unlike Appointment which hides other users' resources behind 404 — this pins current behavior.
    @Test
    void rejectsOtherUsersRoutineAccessAsBadRequest() throws Exception {
        User owner = saveLoginUser("routine-controller-owner@example.com");
        User other = saveLoginUser("routine-controller-other@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String ownerCookie = loginCookie(client, owner);
        String otherCookie = loginCookie(client, other);
        RoutineResponse created = objectMapper.readValue(client.send(
                jsonPost("/api/routines", interviewRoutineRequest()).header("Cookie", ownerCookie).build(),
                HttpResponse.BodyHandlers.ofString()
        ).body(), RoutineResponse.class);

        HttpResponse<String> getOtherUsersRoutine = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines/" + created.id()))
                        .header("Cookie", otherCookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> deleteOtherUsersRoutine = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines/" + created.id()))
                        .header("Cookie", otherCookie)
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertAll(
                () -> assertThat(getOtherUsersRoutine.statusCode()).isEqualTo(400),
                () -> assertThat(deleteOtherUsersRoutine.statusCode()).isEqualTo(400)
        );
    }

    private RoutineCreateRequest interviewRoutineRequest() {
        return new RoutineCreateRequest(
                "면접 준비",
                List.of(
                        new RoutineCreateRequest.StepRequest("씻기", 20, 1),
                        new RoutineCreateRequest.StepRequest("옷 입기", 30, 2)
                ),
                List.of("신분증")
        );
    }

    private HttpRequest.Builder jsonPost(String path, Object body) throws Exception {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
    }

    private User saveLoginUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("password"))
                .build());
    }

    private String loginCookie(HttpClient client, User user) throws Exception {
        return client.send(
                        jsonPost("/api/auth/login", new LoginRequest(user.getEmail(), "password")).build(),
                        HttpResponse.BodyHandlers.ofString()
                )
                .headers()
                .firstValue("set-cookie")
                .orElseThrow();
    }
}

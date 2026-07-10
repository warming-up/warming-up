package com.warmingup.backend.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.warmingup.backend.appointment.dto.AppointmentCreateRequest;
import com.warmingup.backend.appointment.dto.AppointmentResponse;
import com.warmingup.backend.auth.dto.LoginRequest;
import com.warmingup.backend.domain.ItemType;
import com.warmingup.backend.domain.Routine;
import com.warmingup.backend.domain.RoutineItem;
import com.warmingup.backend.domain.User;
import com.warmingup.backend.routine.RoutineRepository;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AppointmentControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @LocalServerPort
    private int port;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void createsAppointmentWithoutUserIdInRequestBody() throws Exception {
        User user = userRepository.save(User.builder()
                .email("appointment-controller-user@example.com")
                .password(passwordEncoder.encode("password"))
                .build());
        Routine routine = saveRoutineWithItems(user);

        AppointmentCreateRequest request = new AppointmentCreateRequest(
                routine.getId(),
                "토스 면접",
                LocalDateTime.of(2026, 7, 10, 18, 0),
                40,
                10
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> loginResponse = client.send(
                jsonPost("/api/auth/login", new LoginRequest(user.getEmail(), "password")).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        String cookie = loginResponse.headers()
                .firstValue("set-cookie")
                .orElseThrow();
        HttpResponse<String> response = client.send(
                jsonPost("/api/appointments", request)
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        AppointmentResponse body = objectMapper.readValue(response.body(), AppointmentResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(201),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.id()).isNotNull(),
                () -> assertThat(body.name()).isEqualTo("토스 면접"),
                () -> assertThat(body.preparationStartTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 16, 20)),
                () -> assertThat(body.departureTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 17, 10)),
                () -> assertThat(body.arrivalTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 18, 0)),
                () -> assertThat(body.steps()).hasSize(2),
                () -> assertThat(body.steps().get(0).id()).isNotNull(),
                () -> assertThat(body.steps().get(0).completed()).isFalse(),
                () -> assertThat(body.checklist()).hasSize(1),
                () -> assertThat(body.checklist().get(0).id()).isNotNull(),
                () -> assertThat(body.checklist().get(0).completed()).isFalse()
        );
    }

    @Test
    void getsCreatedAppointmentByCurrentUser() throws Exception {
        User user = userRepository.save(User.builder()
                .email("appointment-controller-get-user@example.com")
                .password(passwordEncoder.encode("password"))
                .build());
        Routine routine = saveRoutineWithItems(user);
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                routine.getId(),
                "토스 면접",
                LocalDateTime.of(2026, 7, 10, 18, 0),
                40,
                10
        );
        HttpClient client = HttpClient.newHttpClient();
        String cookie = client.send(
                        jsonPost("/api/auth/login", new LoginRequest(user.getEmail(), "password")).build(),
                        HttpResponse.BodyHandlers.ofString()
                )
                .headers()
                .firstValue("set-cookie")
                .orElseThrow();
        AppointmentResponse created = objectMapper.readValue(client.send(
                jsonPost("/api/appointments", request)
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        ).body(), AppointmentResponse.class);

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/appointments/" + created.id()))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        AppointmentResponse found = objectMapper.readValue(response.body(), AppointmentResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(200),
                () -> assertThat(found.id()).isEqualTo(created.id()),
                () -> assertThat(found.name()).isEqualTo(created.name()),
                () -> assertThat(found.preparationStartTime()).isEqualTo(created.preparationStartTime()),
                () -> assertThat(found.departureTime()).isEqualTo(created.departureTime()),
                () -> assertThat(found.steps())
                        .extracting("name")
                        .containsExactly("씻기", "옷 입기"),
                () -> assertThat(found.checklist())
                        .extracting("name")
                .containsExactly("신분증")
        );
    }

    @Test
    void completesItemAndReturnsUpdatedStateOnGet() throws Exception {
        User user = userRepository.save(User.builder()
                .email("appointment-controller-complete-user@example.com")
                .password(passwordEncoder.encode("password"))
                .build());
        Routine routine = saveRoutineWithItems(user);
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                routine.getId(),
                "토스 면접",
                LocalDateTime.of(2026, 7, 10, 18, 0),
                40,
                10
        );
        HttpClient client = HttpClient.newHttpClient();
        String cookie = client.send(
                        jsonPost("/api/auth/login", new LoginRequest(user.getEmail(), "password")).build(),
                        HttpResponse.BodyHandlers.ofString()
                )
                .headers()
                .firstValue("set-cookie")
                .orElseThrow();
        AppointmentResponse created = objectMapper.readValue(client.send(
                jsonPost("/api/appointments", request)
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        ).body(), AppointmentResponse.class);

        HttpResponse<String> completeResponse = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port
                                + "/api/appointments/" + created.id()
                                + "/items/" + created.steps().get(0).id()
                                + "/complete"))
                        .header("Cookie", cookie)
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> getResponse = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/appointments/" + created.id()))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        AppointmentResponse found = objectMapper.readValue(getResponse.body(), AppointmentResponse.class);

        assertAll(
                () -> assertThat(completeResponse.statusCode()).isEqualTo(200),
                () -> assertThat(getResponse.statusCode()).isEqualTo(200),
                () -> assertThat(found.steps().get(0).completed()).isTrue(),
                () -> assertThat(found.checklist().get(0).completed()).isFalse()
        );
    }

    @Test
    void rejectsUnauthenticatedCreateAsUnauthorized() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                null,
                "약속",
                LocalDateTime.of(2026, 7, 10, 18, 0),
                40,
                10
        );

        HttpResponse<String> response = client.send(
                jsonPost("/api/appointments", request).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void rejectsInvalidCreateRequestsAsBadRequest() throws Exception {
        User user = saveLoginUser("appointment-controller-invalid-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);

        HttpResponse<String> blankName = client.send(
                jsonPost("/api/appointments", new AppointmentCreateRequest(
                        null,
                        " ",
                        LocalDateTime.of(2026, 7, 10, 18, 0),
                        40,
                        10
                ))
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> nullArrivalTime = client.send(
                jsonPost("/api/appointments", new AppointmentCreateRequest(
                        null,
                        "약속",
                        null,
                        40,
                        10
                ))
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> negativeTravelMinutes = client.send(
                jsonPost("/api/appointments", new AppointmentCreateRequest(
                        null,
                        "약속",
                        LocalDateTime.of(2026, 7, 10, 18, 0),
                        -1,
                        10
                ))
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> negativeBufferMinutes = client.send(
                jsonPost("/api/appointments", new AppointmentCreateRequest(
                        null,
                        "약속",
                        LocalDateTime.of(2026, 7, 10, 18, 0),
                        40,
                        -1
                ))
                        .header("Cookie", cookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertAll(
                () -> assertThat(blankName.statusCode()).isEqualTo(400),
                () -> assertThat(nullArrivalTime.statusCode()).isEqualTo(400),
                () -> assertThat(negativeTravelMinutes.statusCode()).isEqualTo(400),
                () -> assertThat(negativeBufferMinutes.statusCode()).isEqualTo(400)
        );
    }

    @Test
    void hidesOtherUsersResourcesAsNotFound() throws Exception {
        User owner = saveLoginUser("appointment-controller-owner@example.com");
        User other = saveLoginUser("appointment-controller-other@example.com");
        Routine ownerRoutine = saveRoutineWithItems(owner);
        HttpClient client = HttpClient.newHttpClient();
        String ownerCookie = loginCookie(client, owner);
        String otherCookie = loginCookie(client, other);
        AppointmentCreateRequest ownerRequest = new AppointmentCreateRequest(
                ownerRoutine.getId(),
                "토스 면접",
                LocalDateTime.of(2026, 7, 10, 18, 0),
                40,
                10
        );
        AppointmentResponse created = objectMapper.readValue(client.send(
                jsonPost("/api/appointments", ownerRequest)
                        .header("Cookie", ownerCookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        ).body(), AppointmentResponse.class);

        HttpResponse<String> createWithOtherUsersRoutine = client.send(
                jsonPost("/api/appointments", ownerRequest)
                        .header("Cookie", otherCookie)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> getOtherUsersAppointment = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/appointments/" + created.id()))
                        .header("Cookie", otherCookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> completeOtherUsersItem = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port
                                + "/api/appointments/" + created.id()
                                + "/items/" + created.steps().get(0).id()
                                + "/complete"))
                        .header("Cookie", otherCookie)
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertAll(
                () -> assertThat(createWithOtherUsersRoutine.statusCode()).isEqualTo(404),
                () -> assertThat(getOtherUsersAppointment.statusCode()).isEqualTo(404),
                () -> assertThat(completeOtherUsersItem.statusCode()).isEqualTo(404)
        );
    }

    @Test
    void rejectsMissingAppointmentAndItemAsNotFound() throws Exception {
        User user = saveLoginUser("appointment-controller-missing-user@example.com");
        HttpClient client = HttpClient.newHttpClient();
        String cookie = loginCookie(client, user);

        HttpResponse<String> missingAppointment = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/appointments/999999"))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> missingItem = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port
                                + "/api/appointments/999999/items/999999/complete"))
                        .header("Cookie", cookie)
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertAll(
                () -> assertThat(missingAppointment.statusCode()).isEqualTo(404),
                () -> assertThat(missingItem.statusCode()).isEqualTo(404)
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

    private Routine saveRoutineWithItems(User user) {
        Routine routine = Routine.builder()
                .user(user)
                .name("면접 준비")
                .build();
        routine.getItems().add(RoutineItem.builder()
                .routine(routine)
                .name("씻기")
                .itemType(ItemType.STEP)
                .durationMinutes(20)
                .itemOrder(1)
                .build());
        routine.getItems().add(RoutineItem.builder()
                .routine(routine)
                .name("옷 입기")
                .itemType(ItemType.STEP)
                .durationMinutes(30)
                .itemOrder(2)
                .build());
        routine.getItems().add(RoutineItem.builder()
                .routine(routine)
                .name("신분증")
                .itemType(ItemType.CHECKLIST)
                .durationMinutes(0)
                .itemOrder(1)
                .build());
        return routineRepository.saveAndFlush(routine);
    }
}

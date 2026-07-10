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

    private HttpRequest.Builder jsonPost(String path, Object body) throws Exception {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
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

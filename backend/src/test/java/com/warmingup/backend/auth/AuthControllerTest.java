package com.warmingup.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warmingup.backend.auth.dto.JoinRequest;
import com.warmingup.backend.auth.dto.LoginRequest;
import com.warmingup.backend.auth.dto.UserResponse;
import com.warmingup.backend.domain.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void joinsNewUser() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        JoinRequest request = new JoinRequest("auth-controller-join-user@example.com", "password1");

        HttpResponse<String> response = client.send(
                jsonPost("/api/auth/join", request).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        UserResponse body = objectMapper.readValue(response.body(), UserResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(201),
                () -> assertThat(body.id()).isNotNull(),
                () -> assertThat(body.email()).isEqualTo("auth-controller-join-user@example.com")
        );
    }

    @Test
    void rejectsDuplicateEmailJoinAsBadRequest() throws Exception {
        userRepository.save(User.builder()
                .email("auth-controller-dup-user@example.com")
                .password(passwordEncoder.encode("password"))
                .build());
        HttpClient client = HttpClient.newHttpClient();
        JoinRequest request = new JoinRequest("auth-controller-dup-user@example.com", "password1");

        HttpResponse<String> response = client.send(
                jsonPost("/api/auth/join", request).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void rejectsInvalidJoinRequestsAsBadRequest() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> blankEmail = client.send(
                jsonPost("/api/auth/join", new JoinRequest(" ", "password1")).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> malformedEmail = client.send(
                jsonPost("/api/auth/join", new JoinRequest("not-an-email", "password1")).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> shortPassword = client.send(
                jsonPost("/api/auth/join", new JoinRequest("auth-controller-short-pw@example.com", "short")).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertAll(
                () -> assertThat(blankEmail.statusCode()).isEqualTo(400),
                () -> assertThat(malformedEmail.statusCode()).isEqualTo(400),
                () -> assertThat(shortPassword.statusCode()).isEqualTo(400)
        );
    }

    @Test
    void loginsWithValidCredentialsAndIssuesSessionCookie() throws Exception {
        User user = userRepository.save(User.builder()
                .email("auth-controller-login-user@example.com")
                .password(passwordEncoder.encode("password"))
                .build());
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(
                jsonPost("/api/auth/login", new LoginRequest(user.getEmail(), "password")).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        UserResponse body = objectMapper.readValue(response.body(), UserResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(200),
                () -> assertThat(body.id()).isEqualTo(user.getId()),
                () -> assertThat(body.email()).isEqualTo(user.getEmail()),
                () -> assertThat(response.headers().firstValue("set-cookie")).isPresent()
        );
    }

    @Test
    void rejectsLoginWithWrongPasswordAsBadRequest() throws Exception {
        User user = userRepository.save(User.builder()
                .email("auth-controller-wrong-pw-user@example.com")
                .password(passwordEncoder.encode("password"))
                .build());
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(
                jsonPost("/api/auth/login", new LoginRequest(user.getEmail(), "wrong-password")).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void rejectsLoginWithUnknownEmailAsBadRequest() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(
                jsonPost("/api/auth/login", new LoginRequest("auth-controller-unknown-user@example.com", "password")).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void logoutInvalidatesSessionSoAuthenticatedRequestFailsAfterward() throws Exception {
        User user = userRepository.save(User.builder()
                .email("auth-controller-logout-user@example.com")
                .password(passwordEncoder.encode("password"))
                .build());
        HttpClient client = HttpClient.newHttpClient();
        String cookie = client.send(
                        jsonPost("/api/auth/login", new LoginRequest(user.getEmail(), "password")).build(),
                        HttpResponse.BodyHandlers.ofString()
                )
                .headers()
                .firstValue("set-cookie")
                .orElseThrow();

        HttpResponse<String> logoutResponse = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/logout"))
                        .header("Cookie", cookie)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> afterLogout = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/routines"))
                        .header("Cookie", cookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertAll(
                () -> assertThat(logoutResponse.statusCode()).isEqualTo(204),
                () -> assertThat(afterLogout.statusCode()).isEqualTo(401)
        );
    }

    private HttpRequest.Builder jsonPost(String path, Object body) throws Exception {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
    }
}

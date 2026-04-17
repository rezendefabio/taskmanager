package com.elotech.taskmanager.integration;

import com.elotech.taskmanager.application.auth.AuthResponse;
import com.elotech.taskmanager.application.auth.RegisterRequest;
import com.elotech.taskmanager.application.project.dto.CreateProjectRequest;
import com.elotech.taskmanager.application.project.dto.ProjectResponse;
import com.elotech.taskmanager.application.task.dto.CreateTaskRequest;
import com.elotech.taskmanager.application.task.dto.TaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class TaskManagerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private AuthResponse admin;
    private AuthResponse member;

    @BeforeEach
    void setUp() {
        admin = registerFull("Admin", "admin@test.com", "123456");
        member = registerFull("Membro", "membro@test.com", "123456");
    }

    @Test
    @DisplayName("Fluxo completo: projeto -> membro -> tarefa -> status -> relatorio")
    void fullFlow() {
        // 1. Criar projeto
        var projectRequest = new CreateProjectRequest("Projeto E2E", "Teste integracao");
        var projectResponse = post("/projects", projectRequest, ProjectResponse.class, admin.token());

        assertEquals("Projeto E2E", projectResponse.name());
        assertEquals("Admin", projectResponse.ownerName());
        Long projectId = projectResponse.id();

        // 2. Adicionar membro
        var addMemberResponse = post(
                "/projects/" + projectId + "/members/" + member.userId(),
                null, ProjectResponse.class, admin.token());

        assertTrue(addMemberResponse.members().contains("Membro"));

        // 3. Criar tarefas
        var task1 = post("/projects/" + projectId + "/tasks",
                new CreateTaskRequest("Tarefa 1", "Desc 1", "HIGH", null, null),
                TaskResponse.class, admin.token());

        var task2 = post("/projects/" + projectId + "/tasks",
                new CreateTaskRequest("Tarefa 2", "Desc 2", "LOW", null, member.userId()),
                TaskResponse.class, member.token());

        var task3 = post("/projects/" + projectId + "/tasks",
                new CreateTaskRequest("Tarefa Critica", "Urgente", "CRITICAL", null, null),
                TaskResponse.class, admin.token());

        assertEquals("TODO", task1.status());
        assertEquals("Membro", task2.assigneeName());
        assertEquals("CRITICAL", task3.priority());

        // 4. Mudar status: TODO -> IN_PROGRESS
        var updated = patch("/projects/" + projectId + "/tasks/" + task1.id() + "/status",
                "IN_PROGRESS", TaskResponse.class, admin.token());

        assertEquals("IN_PROGRESS", updated.status());

        // 5. Mudar status: IN_PROGRESS -> DONE
        var done = patch("/projects/" + projectId + "/tasks/" + task1.id() + "/status",
                "DONE", TaskResponse.class, admin.token());

        assertEquals("DONE", done.status());

        // 6. Testar regra: DONE -> TODO bloqueado
        var forbidden = patchExpectError("/projects/" + projectId + "/tasks/" + task1.id() + "/status",
                "TODO", admin.token());

        assertEquals(400, forbidden.getStatusCode().value());

        // 7. Testar regra: MEMBER nao pode fechar CRITICAL
        patch("/projects/" + projectId + "/tasks/" + task3.id() + "/status",
                "IN_PROGRESS", admin.token());

        var criticalForbidden = patchExpectError("/projects/" + projectId + "/tasks/" + task3.id() + "/status",
                "DONE", member.token());

        assertEquals(400, criticalForbidden.getStatusCode().value());

        // 8. ADMIN pode fechar CRITICAL
        var criticalDone = patch("/projects/" + projectId + "/tasks/" + task3.id() + "/status",
                "DONE", TaskResponse.class, admin.token());

        assertEquals("DONE", criticalDone.status());

        // 9. Relatorio
        var report = get("/projects/" + projectId + "/tasks/report", Map.class, admin.token());

        assertNotNull(report.get("byStatus"));
        assertNotNull(report.get("byPriority"));
    }

    // --- Metodos auxiliares ---

    private AuthResponse registerFull(String name, String email, String password) {
        var request = new RegisterRequest(name, email, password, null);
        var response = restTemplate.postForEntity("/auth/register", request, AuthResponse.class);
        assertEquals(201, response.getStatusCode().value());
        return response.getBody();
    }

    private <T> T post(String url, Object body, Class<T> responseType, String token) {
        var headers = authHeaders(token);
        var entity = new HttpEntity<>(body, headers);
        var response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
        assertTrue(response.getStatusCode().is2xxSuccessful(),
                "POST " + url + " falhou com " + response.getStatusCode());
        return response.getBody();
    }

    private <T> T get(String url, Class<T> responseType, String token) {
        var headers = authHeaders(token);
        var entity = new HttpEntity<>(null, headers);
        var response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        return response.getBody();
    }

    private <T> T patch(String url, String status, Class<T> responseType, String token) {
        var headers = authHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>("\"" + status + "\"", headers);
        var response = restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
        assertTrue(response.getStatusCode().is2xxSuccessful(),
                "PATCH " + url + " falhou com " + response.getStatusCode());
        return response.getBody();
    }

    private void patch(String url, String status, String token) {
        patch(url, status, String.class, token);
    }

    private ResponseEntity<String> patchExpectError(String url, String status, String token) {
        var headers = authHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>("\"" + status + "\"", headers);
        return restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
    }

    private HttpHeaders authHeaders(String token) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

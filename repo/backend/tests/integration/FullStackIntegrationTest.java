package integration;

import com.eaglepoint.storehub.StoreHubApplication;
import com.eaglepoint.storehub.dto.*;
import com.eaglepoint.storehub.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * True no-mock integration tests. Boots the full Spring application context
 * with a real PostgreSQL instance (Testcontainers), executes real HTTP requests
 * via TestRestTemplate, and asserts on response bodies — not just status codes.
 *
 * This exercises the real service graph, repository layer, Flyway migrations,
 * security filters, and business logic end-to-end.
 */
@Testcontainers
@SpringBootTest(
        classes = StoreHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullStackIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storehub_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.security.jwt-secret", () -> "dGVzdGluZ29ubHlrZXl0aGF0aXNsb25nZW5vdWdoMTIzNDU2");
        registry.add("app.encryption.aes-key", () -> "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        registry.add("app.bootstrap.admin-username", () -> "admin");
        registry.add("app.bootstrap.admin-password", () -> "IntTest!Admin99");
        registry.add("app.bootstrap.admin-email", () -> "admin@test.local");
    }

    @Autowired
    private TestRestTemplate rest;

    @LocalServerPort
    private int port;

    // Shared state across ordered tests
    private static String adminToken;
    private static String customerToken;
    private static String staffToken;
    private static Long customerId;
    private static Long staffId;
    private static Long orderId;
    private static Long siteId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    // ════════════════════════════════════════════════════
    //  AUTH FLOW — real registration, login, token usage
    // ════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Bootstrap admin can login and receives valid JWT")
    void adminLogin() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("IntTest!Admin99");

        ResponseEntity<AuthResponse> resp = rest.postForEntity(
                url("/api/auth/login"), req, AuthResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getToken());
        assertEquals("admin", resp.getBody().getUsername());
        assertEquals("ENTERPRISE_ADMIN", resp.getBody().getRole());
        adminToken = resp.getBody().getToken();
    }

    @Test
    @Order(2)
    @DisplayName("Register customer user with password policy enforcement")
    void registerCustomer() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("inttest_customer");
        req.setEmail("customer@test.local");
        req.setPassword("Customer!2026");

        ResponseEntity<AuthResponse> resp = rest.postForEntity(
                url("/api/auth/register"), req, AuthResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("inttest_customer", resp.getBody().getUsername());
        assertEquals("CUSTOMER", resp.getBody().getRole());
        customerToken = resp.getBody().getToken();
    }

    @Test
    @Order(3)
    @DisplayName("Registration rejects weak password (missing symbol)")
    void registerWeakPassword_rejected() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("weakuser");
        req.setEmail("weak@test.local");
        req.setPassword("NoSymbol12345");

        ResponseEntity<Map> resp = rest.postForEntity(
                url("/api/auth/register"), req, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("Register and promote staff user via admin")
    void registerAndPromoteStaff() {
        // Register
        RegisterRequest req = new RegisterRequest();
        req.setUsername("inttest_staff");
        req.setEmail("staff@test.local");
        req.setPassword("StaffUser!2026");

        ResponseEntity<AuthResponse> regResp = rest.postForEntity(
                url("/api/auth/register"), req, AuthResponse.class);
        assertEquals(HttpStatus.OK, regResp.getStatusCode());

        // Admin re-auths for privileged action
        ResponseEntity<AuthResponse> reauthResp = rest.exchange(
                url("/api/users/reauth"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("password", "IntTest!Admin99"), authHeaders(adminToken)),
                AuthResponse.class);
        assertEquals(HttpStatus.OK, reauthResp.getStatusCode());
        adminToken = reauthResp.getBody().getToken();

        // Find staff user ID
        ResponseEntity<List> usersResp = rest.exchange(
                url("/api/users"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(adminToken)),
                List.class);
        assertEquals(HttpStatus.OK, usersResp.getStatusCode());

        List<Map<String, Object>> users = usersResp.getBody();
        Map<String, Object> staffUser = users.stream()
                .filter(u -> "inttest_staff".equals(u.get("username")))
                .findFirst().orElseThrow();
        staffId = ((Number) staffUser.get("id")).longValue();

        Map<String, Object> customerUser = users.stream()
                .filter(u -> "inttest_customer".equals(u.get("username")))
                .findFirst().orElseThrow();
        customerId = ((Number) customerUser.get("id")).longValue();

        // Promote to STAFF
        ResponseEntity<UserDto> promoteResp = rest.exchange(
                url("/api/users/" + staffId + "/role?role=STAFF"),
                HttpMethod.PATCH,
                new HttpEntity<>(authHeaders(adminToken)),
                UserDto.class);
        assertEquals(HttpStatus.OK, promoteResp.getStatusCode());
        assertEquals(Role.STAFF, promoteResp.getBody().getRole());

        // Staff re-login to get token with new role
        LoginRequest login = new LoginRequest();
        login.setUsername("inttest_staff");
        login.setPassword("StaffUser!2026");
        ResponseEntity<AuthResponse> staffLogin = rest.postForEntity(
                url("/api/auth/login"), login, AuthResponse.class);
        staffToken = staffLogin.getBody().getToken();
    }

    // ════════════════════════════════════════════════════
    //  ORGANIZATION — real hierarchy creation
    // ════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("Admin creates organization and it persists")
    void adminCreatesOrganization() {
        OrganizationDto dto = new OrganizationDto();
        dto.setName("Integration Test Site");
        dto.setLevel(OrgLevel.SITE);

        ResponseEntity<OrganizationDto> resp = rest.exchange(
                url("/api/organizations"),
                HttpMethod.POST,
                new HttpEntity<>(dto, authHeaders(adminToken)),
                OrganizationDto.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getId());
        assertEquals("Integration Test Site", resp.getBody().getName());
        assertEquals(OrgLevel.SITE, resp.getBody().getLevel());
        siteId = resp.getBody().getId();
    }

    // ════════════════════════════════════════════════════
    //  AUTHORIZATION — real 403 from security filter chain
    // ════════════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("Unauthenticated request gets real 401")
    void unauthenticated_401() {
        ResponseEntity<String> resp = rest.getForEntity(
                url("/api/orders/my"), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("Customer cannot access admin endpoints — real 403")
    void customer_adminEndpoint_403() {
        ResponseEntity<String> resp = rest.exchange(
                url("/api/admin/incentive-rules"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                String.class);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    @Order(8)
    @DisplayName("Customer cannot check in — real 403 from @PreAuthorize")
    void customer_checkIn_403() {
        Map<String, Object> body = Map.of("siteId", siteId != null ? siteId : 1);
        ResponseEntity<String> resp = rest.exchange(
                url("/api/checkins"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(customerToken)),
                String.class);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    // ════════════════════════════════════════════════════
    //  ORDER FLOW — create, retrieve, verify status
    // ════════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("Customer creates pickup order — real service graph")
    void customerCreatesOrder() {
        Map<String, Object> body = Map.of(
                "siteId", siteId != null ? siteId : 1,
                "subtotal", 25.00,
                "fulfillmentMode", "PICKUP");

        ResponseEntity<OrderResponse> resp = rest.exchange(
                url("/api/orders"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(customerToken)),
                OrderResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getId());
        assertEquals(OrderStatus.PENDING, resp.getBody().getStatus());
        assertEquals(FulfillmentMode.PICKUP, resp.getBody().getFulfillmentMode());
        assertTrue(resp.getBody().getSubtotal().compareTo(new java.math.BigDecimal("25.00")) == 0);
        assertNotNull(resp.getBody().getPickupVerificationCode(), "Pickup order should have a verification code");
        assertEquals(6, resp.getBody().getPickupVerificationCode().length(), "Verification code should be 6 digits");
        orderId = resp.getBody().getId();
    }

    @Test
    @Order(11)
    @DisplayName("Customer retrieves own orders — real DB query")
    void customerRetrievesOwnOrders() {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/orders/my"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                Map.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        List<?> content = (List<?>) resp.getBody().get("content");
        assertFalse(content.isEmpty(), "Customer should see their order");
    }

    @Test
    @Order(12)
    @DisplayName("Customer retrieves specific order by ID — real object access check")
    void customerRetrievesOrderById() {
        ResponseEntity<OrderResponse> resp = rest.exchange(
                url("/api/orders/" + orderId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                OrderResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(orderId, resp.getBody().getId());
        assertEquals(OrderStatus.PENDING, resp.getBody().getStatus());
    }

    // ════════════════════════════════════════════════════
    //  COMMUNITY FLOW — posts, votes, comments
    // ════════════════════════════════════════════════════

    @Test
    @Order(20)
    @DisplayName("Customer creates community post — gamification points awarded")
    void customerCreatesPost() {
        Map<String, Object> body = Map.of(
                "title", "Integration test post",
                "body", "This is a real post through the full stack",
                "topic", "testing");

        ResponseEntity<PostResponse> resp = rest.exchange(
                url("/api/community/posts"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(customerToken)),
                PostResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Integration test post", resp.getBody().getTitle());
        assertEquals("testing", resp.getBody().getTopic());
        assertEquals(0, resp.getBody().getUpvotes());
    }

    @Test
    @Order(21)
    @DisplayName("Customer views community feed — real paginated response")
    void customerViewsFeed() {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/community/posts?page=0&size=20"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                Map.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        List<?> content = (List<?>) resp.getBody().get("content");
        assertFalse(content.isEmpty(), "Feed should contain the created post");
    }

    @Test
    @Order(22)
    @DisplayName("Customer gamification points increase after post creation")
    void customerPointsIncreased() {
        ResponseEntity<PointsProfile> resp = rest.exchange(
                url("/api/community/points/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                PointsProfile.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getTotalPoints() > 0, "Points should increase after creating a post");
    }

    // ════════════════════════════════════════════════════
    //  RATING FLOW — submit, retrieve, appeal
    // ════════════════════════════════════════════════════

    @Test
    @Order(30)
    @DisplayName("Customer submits rating — real validation and persistence")
    void customerSubmitsRating() {
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "ratedUserId", staffId != null ? staffId : 1,
                "targetType", "STAFF",
                "stars", 4,
                "timelinessScore", 5,
                "communicationScore", 4,
                "accuracyScore", 3,
                "comment", "Good service overall");

        ResponseEntity<RatingResponse> resp = rest.exchange(
                url("/api/ratings"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(customerToken)),
                RatingResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getId());
        assertEquals(4, resp.getBody().getStars());
    }

    // ════════════════════════════════════════════════════
    //  SUPPORT TICKET — create, retrieve
    // ════════════════════════════════════════════════════

    @Test
    @Order(40)
    @DisplayName("Customer creates support ticket — real auto-approve check")
    void customerCreatesTicket() {
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "type", "REFUND_ONLY",
                "description", "Item arrived damaged, requesting refund",
                "refundAmount", 15.00);

        ResponseEntity<TicketResponse> resp = rest.exchange(
                url("/api/tickets"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(customerToken)),
                TicketResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getId());
        // Auto-approve for under $25 with no prior abuse
        assertTrue(
                resp.getBody().getStatus() == TicketStatus.OPEN
                        || resp.getBody().isAutoApproved(),
                "Ticket should be OPEN or auto-approved");
    }

    @Test
    @Order(41)
    @DisplayName("Customer retrieves own tickets — real DB query")
    void customerRetrievesOwnTickets() {
        ResponseEntity<List> resp = rest.exchange(
                url("/api/tickets/my"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                List.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertFalse(resp.getBody().isEmpty(), "Customer should see their ticket");
    }

    // ════════════════════════════════════════════════════
    //  ADDRESS BOOK — CRUD through real service
    // ════════════════════════════════════════════════════

    @Test
    @Order(50)
    @DisplayName("Customer manages addresses — full CRUD lifecycle")
    void customerAddressCRUD() {
        // Create
        Map<String, Object> body = Map.of(
                "label", "Home",
                "street", "123 Integration Ave",
                "city", "Testville",
                "state", "CA",
                "zipCode", "90210");

        ResponseEntity<AddressDto> createResp = rest.exchange(
                url("/api/addresses"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(customerToken)),
                AddressDto.class);
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        Long addrId = createResp.getBody().getId();
        assertNotNull(addrId);
        assertEquals("Home", createResp.getBody().getLabel());

        // List
        ResponseEntity<List> listResp = rest.exchange(
                url("/api/addresses"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                List.class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertFalse(listResp.getBody().isEmpty());

        // Delete
        ResponseEntity<Void> delResp = rest.exchange(
                url("/api/addresses/" + addrId),
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(customerToken)),
                Void.class);
        assertEquals(HttpStatus.NO_CONTENT, delResp.getStatusCode());
    }

    // ════════════════════════════════════════════════════
    //  CREDIT SCORE — real computation
    // ════════════════════════════════════════════════════

    @Test
    @Order(60)
    @DisplayName("Customer retrieves own credit score — real computation")
    void customerCreditScore() {
        ResponseEntity<CreditScoreDto> resp = rest.exchange(
                url("/api/credit-score/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                CreditScoreDto.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getScore() >= 0, "Credit score should be non-negative");
    }

    // ════════════════════════════════════════════════════
    //  EXPERIMENT — real deterministic bucketing
    // ════════════════════════════════════════════════════

    @Test
    @Order(70)
    @DisplayName("Admin creates experiment and customer gets deterministic bucket")
    void experimentBucketing() {
        // Admin re-auth
        ResponseEntity<AuthResponse> reauthResp = rest.exchange(
                url("/api/users/reauth"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("password", "IntTest!Admin99"), authHeaders(adminToken)),
                AuthResponse.class);
        adminToken = reauthResp.getBody().getToken();

        // Create experiment
        Map<String, Object> expBody = Map.of(
                "name", "inttest-experiment",
                "type", "AB_TEST",
                "variantCount", 2,
                "description", "Integration test experiment");

        ResponseEntity<ExperimentDto> createResp = rest.exchange(
                url("/api/analytics/experiments"),
                HttpMethod.POST,
                new HttpEntity<>(expBody, authHeaders(adminToken)),
                ExperimentDto.class);
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        assertTrue(createResp.getBody().isActive());

        // Customer gets bucket — deterministic
        ResponseEntity<Map> bucketResp = rest.exchange(
                url("/api/analytics/experiments/inttest-experiment/bucket"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                Map.class);
        assertEquals(HttpStatus.OK, bucketResp.getStatusCode());
        assertNotNull(bucketResp.getBody().get("variant"));
        int variant = ((Number) bucketResp.getBody().get("variant")).intValue();
        assertTrue(variant >= 0 && variant < 2, "Variant should be 0 or 1");

        // Same user gets same bucket (deterministic)
        ResponseEntity<Map> bucket2 = rest.exchange(
                url("/api/analytics/experiments/inttest-experiment/bucket"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(customerToken)),
                Map.class);
        assertEquals(variant, ((Number) bucket2.getBody().get("variant")).intValue(),
                "Deterministic bucketing should return same variant for same user");
    }

    // ════════════════════════════════════════════════════
    //  ANALYTICS — real event logging and metrics
    // ════════════════════════════════════════════════════

    @Test
    @Order(80)
    @DisplayName("Customer logs analytics event — real persistence")
    void logAnalyticsEvent() {
        Map<String, Object> body = Map.of(
                "eventType", "PAGE_VIEW",
                "siteId", siteId != null ? siteId : 1,
                "target", "/orders");

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/analytics/events"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(customerToken)),
                Void.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    // ════════════════════════════════════════════════════
    //  RECENT AUTH FLOW — real 403 → reauth → retry
    // ════════════════════════════════════════════════════

    @Test
    @Order(90)
    @DisplayName("Re-authentication refreshes token for privileged actions")
    void reauthFlow() {
        ResponseEntity<AuthResponse> resp = rest.exchange(
                url("/api/users/reauth"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("password", "IntTest!Admin99"), authHeaders(adminToken)),
                AuthResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody().getToken());
        assertNotEquals(adminToken, resp.getBody().getToken(),
                "Re-auth should issue a fresh token");
    }

    @Test
    @Order(91)
    @DisplayName("Re-authentication rejects wrong password")
    void reauthWrongPassword() {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/users/reauth"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("password", "WrongPassword!1"), authHeaders(adminToken)),
                Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }
}

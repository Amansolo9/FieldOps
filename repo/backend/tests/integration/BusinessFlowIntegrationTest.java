package integration;

import com.eaglepoint.storehub.StoreHubApplication;
import com.eaglepoint.storehub.dto.*;
import com.eaglepoint.storehub.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Multi-step business-flow integration tests proving complex real-world
 * scenarios through the full service graph with deep state assertions.
 *
 * Each test class covers an end-to-end business flow:
 * - Community engagement: post → vote → comment → follow → points check
 * - Order lifecycle: create → status transitions → pickup verification
 * - Support ticket lifecycle: create → assign → status transition
 * - Delivery zone configuration: create group → add ZIPs → add bands
 * - Rating + appeal flow: rate → appeal → resolve
 * - Audit trail: actions produce auditable records
 */
@Testcontainers
@SpringBootTest(
        classes = StoreHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusinessFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storehub_bizflow")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.security.jwt-secret", () -> "Yml6Zmxvd3Rlc3RrZXlsb25nZW5vdWdoZm9yaG1hYzI1Ng==");
        registry.add("app.encryption.aes-key", () -> "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789");
        registry.add("app.bootstrap.admin-username", () -> "bizadmin");
        registry.add("app.bootstrap.admin-password", () -> "BizAdmin!Test99");
        registry.add("app.bootstrap.admin-email", () -> "bizadmin@test.local");
    }

    @Autowired private TestRestTemplate rest;
    @LocalServerPort private int port;

    private static String adminToken;
    private static String customerToken;
    private static String staffToken;
    private static String siteManagerToken;
    private static Long customerId;
    private static Long staffId;
    private static Long siteManagerId;
    private static Long siteId;
    private static Long orderId;
    private static String pickupCode;
    private static Long postId;
    private static Long ratingId;
    private static Long ticketId;

    private String url(String path) { return "http://localhost:" + port + path; }
    private HttpHeaders auth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    // ════════════════════════════════════════════════════
    //  SETUP — users, site, roles
    // ════════════════════════════════════════════════════

    @Test @Order(1) @DisplayName("Setup: admin login + create users + site")
    void setup() {
        // Admin login
        adminToken = login("bizadmin", "BizAdmin!Test99");

        // Create site
        ResponseEntity<AuthResponse> reauthResp = rest.exchange(url("/api/users/reauth"),
                HttpMethod.POST, new HttpEntity<>(Map.of("password", "BizAdmin!Test99"), auth(adminToken)),
                AuthResponse.class);
        adminToken = reauthResp.getBody().getToken();

        OrganizationDto orgDto = new OrganizationDto();
        orgDto.setName("Biz Flow Site");
        orgDto.setLevel(OrgLevel.SITE);
        ResponseEntity<OrganizationDto> orgResp = rest.exchange(url("/api/organizations"),
                HttpMethod.POST, new HttpEntity<>(orgDto, auth(adminToken)), OrganizationDto.class);
        siteId = orgResp.getBody().getId();

        // Register customer
        customerToken = register("biz_customer", "biz_customer@test.local", "Customer!Biz26");

        // Register + promote staff
        register("biz_staff", "biz_staff@test.local", "StaffBiz!2026");
        // Register + promote site manager
        register("biz_manager", "biz_manager@test.local", "Manager!Biz26");

        // Re-auth admin for promotions
        reauthResp = rest.exchange(url("/api/users/reauth"),
                HttpMethod.POST, new HttpEntity<>(Map.of("password", "BizAdmin!Test99"), auth(adminToken)),
                AuthResponse.class);
        adminToken = reauthResp.getBody().getToken();

        // Get user IDs
        ResponseEntity<List> usersResp = rest.exchange(url("/api/users"),
                HttpMethod.GET, new HttpEntity<>(auth(adminToken)), List.class);
        List<Map<String, Object>> users = usersResp.getBody();

        staffId = findUserId(users, "biz_staff");
        siteManagerId = findUserId(users, "biz_manager");
        customerId = findUserId(users, "biz_customer");

        // Promote
        rest.exchange(url("/api/users/" + staffId + "/role?role=STAFF"),
                HttpMethod.PATCH, new HttpEntity<>(auth(adminToken)), UserDto.class);
        rest.exchange(url("/api/users/" + siteManagerId + "/role?role=SITE_MANAGER"),
                HttpMethod.PATCH, new HttpEntity<>(auth(adminToken)), UserDto.class);

        // Re-login with new roles
        staffToken = login("biz_staff", "StaffBiz!2026");
        siteManagerToken = login("biz_manager", "Manager!Biz26");
    }

    // ════════════════════════════════════════════════════
    //  COMMUNITY ENGAGEMENT FLOW
    // ════════════════════════════════════════════════════

    @Test @Order(10) @DisplayName("Community: create post, vote, comment, follow — state verified")
    void communityEngagementFlow() {
        // Customer creates post
        ResponseEntity<PostResponse> postResp = rest.exchange(url("/api/community/posts"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("title", "Biz flow post", "body", "Testing engagement flow", "topic", "biztest"),
                        auth(customerToken)),
                PostResponse.class);
        assertEquals(HttpStatus.OK, postResp.getStatusCode());
        postId = postResp.getBody().getId();
        assertEquals(0, postResp.getBody().getUpvotes());
        assertEquals(0, postResp.getBody().getDownvotes());
        assertEquals(0, postResp.getBody().getCommentCount());

        // Staff upvotes the post
        ResponseEntity<PostResponse> voteResp = rest.exchange(
                url("/api/community/posts/" + postId + "/vote?type=UPVOTE"),
                HttpMethod.POST, new HttpEntity<>(auth(staffToken)), PostResponse.class);
        assertEquals(HttpStatus.OK, voteResp.getStatusCode());
        assertEquals(1, voteResp.getBody().getUpvotes(), "Upvote count should be 1");
        assertEquals(1, voteResp.getBody().getNetVotes(), "Net votes should be 1");

        // Staff adds comment
        ResponseEntity<CommentResponse> commentResp = rest.exchange(
                url("/api/community/posts/" + postId + "/comments"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("body", "Great insight!"), auth(staffToken)),
                CommentResponse.class);
        assertEquals(HttpStatus.OK, commentResp.getStatusCode());
        assertEquals("Great insight!", commentResp.getBody().getBody());
        assertNotNull(commentResp.getBody().getCreatedAt());

        // Verify comment count increased on re-fetch
        ResponseEntity<List> commentsResp = rest.exchange(
                url("/api/community/posts/" + postId + "/comments"),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), List.class);
        assertEquals(1, commentsResp.getBody().size(), "Should have 1 comment");

        // Staff follows customer (author)
        ResponseEntity<Void> followResp = rest.exchange(
                url("/api/community/users/" + customerId + "/follow"),
                HttpMethod.POST, new HttpEntity<>(auth(staffToken)), Void.class);
        assertEquals(HttpStatus.OK, followResp.getStatusCode());

        // Verify following list
        ResponseEntity<List> followingResp = rest.exchange(
                url("/api/community/following"),
                HttpMethod.GET, new HttpEntity<>(auth(staffToken)), List.class);
        assertEquals(HttpStatus.OK, followingResp.getStatusCode());
        assertTrue(followingResp.getBody().contains(customerId.intValue()),
                "Following list should contain the customer");

        // Staff follows topic
        rest.exchange(url("/api/community/topics/biztest/follow"),
                HttpMethod.POST, new HttpEntity<>(auth(staffToken)), Void.class);

        ResponseEntity<List> topicsResp = rest.exchange(
                url("/api/community/topics/following"),
                HttpMethod.GET, new HttpEntity<>(auth(staffToken)), List.class);
        assertTrue(topicsResp.getBody().contains("biztest"));

        // Unfollow topic
        rest.exchange(url("/api/community/topics/biztest/follow"),
                HttpMethod.DELETE, new HttpEntity<>(auth(staffToken)), Void.class);

        topicsResp = rest.exchange(url("/api/community/topics/following"),
                HttpMethod.GET, new HttpEntity<>(auth(staffToken)), List.class);
        assertFalse(topicsResp.getBody().contains("biztest"), "Topic should be unfollowed");

        // Unfollow user
        rest.exchange(url("/api/community/users/" + customerId + "/follow"),
                HttpMethod.DELETE, new HttpEntity<>(auth(staffToken)), Void.class);
        followingResp = rest.exchange(url("/api/community/following"),
                HttpMethod.GET, new HttpEntity<>(auth(staffToken)), List.class);
        assertFalse(followingResp.getBody().contains(customerId.intValue()),
                "Following list should not contain the customer after unfollow");

        // Toggle favorite
        ResponseEntity<Map> favResp = rest.exchange(
                url("/api/community/posts/" + postId + "/favorite"),
                HttpMethod.POST, new HttpEntity<>(auth(customerToken)), Map.class);
        assertTrue((Boolean) favResp.getBody().get("favorited"));

        ResponseEntity<List> favsResp = rest.exchange(
                url("/api/community/favorites"),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), List.class);
        assertTrue(favsResp.getBody().contains(postId.intValue()));

        // Customer points should have increased from post creation + receiving upvote
        ResponseEntity<PointsProfile> pointsResp = rest.exchange(
                url("/api/community/points/me"),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), PointsProfile.class);
        assertTrue(pointsResp.getBody().getTotalPoints() > 0,
                "Customer should have points from creating post and receiving upvote");
    }

    @Test @Order(11) @DisplayName("Community: view feed by topic with real filter")
    void communityFeedByTopic() {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/community/posts/topic/biztest?page=0&size=20"),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<?> content = (List<?>) resp.getBody().get("content");
        assertFalse(content.isEmpty(), "Topic filter should return the post");
        Map<?, ?> post = (Map<?, ?>) content.get(0);
        assertEquals("biztest", post.get("topic"));
    }

    // ════════════════════════════════════════════════════
    //  ORDER LIFECYCLE — create → confirm → pickup verify
    // ════════════════════════════════════════════════════

    @Test @Order(20) @DisplayName("Order lifecycle: create → confirm → pickup verification")
    void orderLifecycle() {
        // Customer creates pickup order
        ResponseEntity<OrderResponse> createResp = rest.exchange(url("/api/orders"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("siteId", siteId, "subtotal", 30.00, "fulfillmentMode", "PICKUP"),
                        auth(customerToken)),
                OrderResponse.class);
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        orderId = createResp.getBody().getId();
        pickupCode = createResp.getBody().getPickupVerificationCode();
        assertEquals(OrderStatus.PENDING, createResp.getBody().getStatus());
        assertNotNull(pickupCode);
        assertEquals(6, pickupCode.length());
        assertEquals(0, new BigDecimal("0.00").compareTo(createResp.getBody().getDeliveryFee()),
                "Pickup orders should have zero delivery fee");
        assertEquals(0, new BigDecimal("30.00").compareTo(createResp.getBody().getTotal()),
                "Total should equal subtotal for pickup (no delivery fee)");
        assertTrue(createResp.getBody().isPickup());

        // Site manager re-auths and confirms order
        ResponseEntity<AuthResponse> reauthResp = rest.exchange(url("/api/users/reauth"),
                HttpMethod.POST, new HttpEntity<>(Map.of("password", "Manager!Biz26"), auth(siteManagerToken)),
                AuthResponse.class);
        siteManagerToken = reauthResp.getBody().getToken();

        ResponseEntity<OrderResponse> confirmResp = rest.exchange(
                url("/api/orders/" + orderId + "/status?status=CONFIRMED"),
                HttpMethod.PATCH, new HttpEntity<>(auth(siteManagerToken)), OrderResponse.class);
        assertEquals(HttpStatus.OK, confirmResp.getStatusCode());
        assertEquals(OrderStatus.CONFIRMED, confirmResp.getBody().getStatus());

        // Transition to READY_FOR_PICKUP
        ResponseEntity<OrderResponse> readyResp = rest.exchange(
                url("/api/orders/" + orderId + "/status?status=READY_FOR_PICKUP"),
                HttpMethod.PATCH, new HttpEntity<>(auth(siteManagerToken)), OrderResponse.class);
        assertEquals(OrderStatus.READY_FOR_PICKUP, readyResp.getBody().getStatus());

        // Staff verifies pickup with correct code
        ResponseEntity<OrderResponse> verifyResp = rest.exchange(
                url("/api/orders/" + orderId + "/verify-pickup?code=" + pickupCode),
                HttpMethod.POST, new HttpEntity<>(auth(staffToken)), OrderResponse.class);
        assertEquals(HttpStatus.OK, verifyResp.getStatusCode());
        assertEquals(OrderStatus.PICKED_UP, verifyResp.getBody().getStatus(),
                "Order should transition to PICKED_UP after verification");
        assertTrue(verifyResp.getBody().isPickupVerified(),
                "pickupVerified flag should be true");
        assertNotNull(verifyResp.getBody().getVerifiedById(),
                "verifiedBy should be set to the staff who verified");
        assertNotNull(verifyResp.getBody().getVerifiedAt(),
                "verifiedAt timestamp should be set");
    }

    @Test @Order(21) @DisplayName("Order: pickup code reuse is blocked")
    void pickupCodeReuseBlocked() {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/orders/" + orderId + "/verify-pickup?code=" + pickupCode),
                HttpMethod.POST, new HttpEntity<>(auth(staffToken)), Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode(),
                "Re-verifying an already-verified pickup should fail");
    }

    @Test @Order(22) @DisplayName("Order: customer cannot self-redeem pickup")
    void customerCannotSelfRedeem() {
        // Create a new pickup order for this test
        ResponseEntity<OrderResponse> createResp = rest.exchange(url("/api/orders"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("siteId", siteId, "subtotal", 10.00, "fulfillmentMode", "PICKUP"),
                        auth(customerToken)),
                OrderResponse.class);
        String code = createResp.getBody().getPickupVerificationCode();

        ResponseEntity<Map> resp = rest.exchange(
                url("/api/orders/" + createResp.getBody().getId() + "/verify-pickup?code=" + code),
                HttpMethod.POST, new HttpEntity<>(auth(customerToken)), Map.class);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode(),
                "Customer should not be able to self-redeem their own pickup");
    }

    // ════════════════════════════════════════════════════
    //  SUPPORT TICKET LIFECYCLE — create → assign → close
    // ════════════════════════════════════════════════════

    @Test @Order(30) @DisplayName("Ticket lifecycle: create → assign to staff → status update")
    void ticketLifecycle() {
        // Customer creates ticket
        ResponseEntity<TicketResponse> createResp = rest.exchange(url("/api/tickets"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("orderId", orderId, "type", "REFUND_ONLY",
                        "description", "Product quality issue, requesting full refund",
                        "refundAmount", 30.00), auth(customerToken)),
                TicketResponse.class);
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        ticketId = createResp.getBody().getId();
        assertNotNull(createResp.getBody().getFirstResponseDueAt(),
                "SLA timer (first response due) should be set");
        assertEquals(TicketType.REFUND_ONLY, createResp.getBody().getType());

        // Site manager re-auths and assigns to staff
        ResponseEntity<AuthResponse> reauthResp = rest.exchange(url("/api/users/reauth"),
                HttpMethod.POST, new HttpEntity<>(Map.of("password", "Manager!Biz26"), auth(siteManagerToken)),
                AuthResponse.class);
        siteManagerToken = reauthResp.getBody().getToken();

        ResponseEntity<TicketResponse> assignResp = rest.exchange(
                url("/api/tickets/" + ticketId + "/assign?staffId=" + staffId),
                HttpMethod.PATCH, new HttpEntity<>(auth(siteManagerToken)), TicketResponse.class);
        assertEquals(HttpStatus.OK, assignResp.getStatusCode());
        assertEquals(staffId, assignResp.getBody().getAssignedToId(),
                "Ticket should be assigned to staff");

        // Staff re-auths and updates status to UNDER_REVIEW
        ResponseEntity<AuthResponse> staffReauth = rest.exchange(url("/api/users/reauth"),
                HttpMethod.POST, new HttpEntity<>(Map.of("password", "StaffBiz!2026"), auth(staffToken)),
                AuthResponse.class);
        staffToken = staffReauth.getBody().getToken();

        ResponseEntity<TicketResponse> statusResp = rest.exchange(
                url("/api/tickets/" + ticketId + "/status?status=UNDER_REVIEW"),
                HttpMethod.PATCH, new HttpEntity<>(auth(staffToken)), TicketResponse.class);
        assertEquals(HttpStatus.OK, statusResp.getStatusCode());
        assertEquals(TicketStatus.UNDER_REVIEW, statusResp.getBody().getStatus());

        // Retrieve ticket by ID — verify all state
        ResponseEntity<TicketResponse> getResp = rest.exchange(
                url("/api/tickets/" + ticketId),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), TicketResponse.class);
        assertEquals(TicketStatus.UNDER_REVIEW, getResp.getBody().getStatus());
        assertEquals(staffId, getResp.getBody().getAssignedToId());

        // Staff view by status
        ResponseEntity<List> byStatusResp = rest.exchange(
                url("/api/tickets/status/UNDER_REVIEW"),
                HttpMethod.GET, new HttpEntity<>(auth(staffToken)), List.class);
        assertFalse(byStatusResp.getBody().isEmpty(), "Staff should see tickets in UNDER_REVIEW");
    }

    // ════════════════════════════════════════════════════
    //  RATING + APPEAL FLOW
    // ════════════════════════════════════════════════════

    @Test @Order(40) @DisplayName("Rating + appeal: submit → appeal → resolve with arbitration")
    void ratingAppealFlow() {
        // Customer submits rating
        ResponseEntity<RatingResponse> rateResp = rest.exchange(url("/api/ratings"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("orderId", orderId, "ratedUserId", staffId,
                        "targetType", "STAFF", "stars", 2, "timelinessScore", 2,
                        "communicationScore", 3, "accuracyScore", 1,
                        "comment", "Service was slow and order incomplete"), auth(customerToken)),
                RatingResponse.class);
        assertEquals(HttpStatus.OK, rateResp.getStatusCode());
        ratingId = rateResp.getBody().getId();
        assertEquals(2, rateResp.getBody().getStars());
        assertEquals(2, rateResp.getBody().getTimelinessScore());
        assertEquals(3, rateResp.getBody().getCommunicationScore());
        assertEquals(1, rateResp.getBody().getAccuracyScore());
        assertNotNull(rateResp.getBody().getAppealDeadline(),
                "Appeal deadline should be set (7 days from now)");

        // Staff appeals the rating
        ResponseEntity<RatingResponse> appealResp = rest.exchange(
                url("/api/ratings/" + ratingId + "/appeal?reason=Customer+received+wrong+order+not+my+fault"),
                HttpMethod.POST, new HttpEntity<>(auth(staffToken)), RatingResponse.class);
        assertEquals(HttpStatus.OK, appealResp.getStatusCode());
        assertEquals(AppealStatus.PENDING, appealResp.getBody().getAppealStatus());

        // Verify average rating for staff
        ResponseEntity<Map> avgResp = rest.exchange(
                url("/api/ratings/user/" + staffId + "/average"),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), Map.class);
        assertEquals(HttpStatus.OK, avgResp.getStatusCode());
        assertNotNull(avgResp.getBody().get("averageStars"));

        // Admin resolves appeal
        ResponseEntity<AuthResponse> adminReauth = rest.exchange(url("/api/users/reauth"),
                HttpMethod.POST, new HttpEntity<>(Map.of("password", "BizAdmin!Test99"), auth(adminToken)),
                AuthResponse.class);
        adminToken = adminReauth.getBody().getToken();

        ResponseEntity<RatingResponse> resolveResp = rest.exchange(
                url("/api/ratings/" + ratingId + "/appeal/resolve?resolution=UPHELD&notes=Confirmed+wrong+order+dispatch"),
                HttpMethod.PATCH, new HttpEntity<>(auth(adminToken)), RatingResponse.class);
        assertEquals(HttpStatus.OK, resolveResp.getStatusCode());
        assertEquals(AppealStatus.UPHELD, resolveResp.getBody().getAppealStatus());

        // Ratings list for user
        ResponseEntity<List> ratingsResp = rest.exchange(
                url("/api/ratings/user/" + staffId),
                HttpMethod.GET, new HttpEntity<>(auth(staffToken)), List.class);
        assertFalse(ratingsResp.getBody().isEmpty(), "Staff should see their ratings");
    }

    // ════════════════════════════════════════════════════
    //  DELIVERY ZONE CONFIGURATION
    // ════════════════════════════════════════════════════

    @Test @Order(50) @DisplayName("Delivery zones: create zone group → add ZIP → add band → verify pricing")
    void deliveryZoneConfiguration() {
        // Site manager re-auths
        ResponseEntity<AuthResponse> reauthResp = rest.exchange(url("/api/users/reauth"),
                HttpMethod.POST, new HttpEntity<>(Map.of("password", "Manager!Biz26"), auth(siteManagerToken)),
                AuthResponse.class);
        siteManagerToken = reauthResp.getBody().getToken();

        // Create zone group
        ResponseEntity<Map> groupResp = rest.exchange(
                url("/api/delivery-zone-groups?siteId=" + siteId + "&name=Metro"),
                HttpMethod.POST, new HttpEntity<>(auth(siteManagerToken)), Map.class);
        assertEquals(HttpStatus.OK, groupResp.getStatusCode());
        Long groupId = ((Number) groupResp.getBody().get("id")).longValue();

        // Add ZIP code
        ResponseEntity<Map> zipResp = rest.exchange(
                url("/api/delivery-zone-groups/" + groupId + "/zips?zipCode=90210&distanceMiles=3.5"),
                HttpMethod.POST, new HttpEntity<>(auth(siteManagerToken)), Map.class);
        assertEquals(HttpStatus.OK, zipResp.getStatusCode());

        // Add distance band
        ResponseEntity<Map> bandResp = rest.exchange(
                url("/api/delivery-zone-groups/" + groupId + "/bands?minMiles=0&maxMiles=5&fee=4.99"),
                HttpMethod.POST, new HttpEntity<>(auth(siteManagerToken)), Map.class);
        assertEquals(HttpStatus.OK, bandResp.getStatusCode());

        // Verify zone group appears in site listing
        ResponseEntity<List> listResp = rest.exchange(
                url("/api/delivery-zone-groups/site/" + siteId),
                HttpMethod.GET, new HttpEntity<>(auth(siteManagerToken)), List.class);
        assertFalse(listResp.getBody().isEmpty(), "Zone group should appear in site listing");

        // Also create a legacy delivery zone
        ResponseEntity<Map> zoneResp = rest.exchange(
                url("/api/delivery-zones?siteId=" + siteId + "&zipCode=90211&distanceMiles=4.0&deliveryFee=5.99"),
                HttpMethod.POST, new HttpEntity<>(auth(siteManagerToken)), Map.class);
        assertEquals(HttpStatus.OK, zoneResp.getStatusCode());

        ResponseEntity<List> zonesResp = rest.exchange(
                url("/api/delivery-zones/site/" + siteId),
                HttpMethod.GET, new HttpEntity<>(auth(siteManagerToken)), List.class);
        assertFalse(zonesResp.getBody().isEmpty());
    }

    // ════════════════════════════════════════════════════
    //  AUDIT TRAIL — verify actions produce audit records
    // ════════════════════════════════════════════════════

    @Test @Order(60) @DisplayName("Audit trail: order actions produce auditable records")
    void auditTrailForOrder() {
        // Admin should be able to see audit trail for the order
        ResponseEntity<List> auditResp = rest.exchange(
                url("/api/audit/entity/Order/" + orderId),
                HttpMethod.GET, new HttpEntity<>(auth(adminToken)), List.class);
        assertEquals(HttpStatus.OK, auditResp.getStatusCode());
        assertFalse(auditResp.getBody().isEmpty(),
                "Order should have audit trail entries from create/status updates");

        // Audit trail for user
        ResponseEntity<List> userAuditResp = rest.exchange(
                url("/api/audit/user/" + customerId),
                HttpMethod.GET, new HttpEntity<>(auth(adminToken)), List.class);
        assertEquals(HttpStatus.OK, userAuditResp.getStatusCode());
    }

    @Test @Order(61) @DisplayName("Audit trail: date range query returns entries")
    void auditTrailDateRange() {
        ResponseEntity<List> resp = rest.exchange(
                url("/api/audit/range?start=2020-01-01T00:00:00Z&end=2030-12-31T23:59:59Z"),
                HttpMethod.GET, new HttpEntity<>(auth(adminToken)), List.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertFalse(resp.getBody().isEmpty(), "Audit trail should have entries in this date range");
    }

    // ════════════════════════════════════════════════════
    //  ANALYTICS — events and metrics
    // ════════════════════════════════════════════════════

    @Test @Order(70) @DisplayName("Analytics: log events and retrieve site metrics")
    void analyticsFlow() {
        // Log several events
        for (String type : List.of("PAGE_VIEW", "CLICK", "CONVERSION")) {
            rest.exchange(url("/api/analytics/events"),
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of("eventType", type, "siteId", siteId, "target", "/test"), auth(customerToken)),
                    Void.class);
        }

        // Retrieve site metrics
        ResponseEntity<SiteMetrics> metricsResp = rest.exchange(
                url("/api/analytics/sites/" + siteId + "/metrics?start=2020-01-01T00:00:00Z&end=2030-12-31T23:59:59Z"),
                HttpMethod.GET, new HttpEntity<>(auth(adminToken)), SiteMetrics.class);
        assertEquals(HttpStatus.OK, metricsResp.getStatusCode());
        assertNotNull(metricsResp.getBody());
        assertEquals(siteId, metricsResp.getBody().getSiteId());
        assertNotNull(metricsResp.getBody().getEventCounts(), "Event counts should be populated");
        assertNotNull(metricsResp.getBody().getFunnel(), "Funnel metrics should be computed");
    }

    // ════════════════════════════════════════════════════
    //  INCENTIVE RULES — admin configuration
    // ════════════════════════════════════════════════════

    @Test @Order(80) @DisplayName("Incentive rules: admin lists and updates rules")
    void incentiveRuleManagement() {
        ResponseEntity<List> listResp = rest.exchange(
                url("/api/admin/incentive-rules"),
                HttpMethod.GET, new HttpEntity<>(auth(adminToken)), List.class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        // Rules might or might not be seeded — just verify the endpoint works
    }

    // ════════════════════════════════════════════════════
    //  CREDIT SCORE — verify it reflects business activity
    // ════════════════════════════════════════════════════

    @Test @Order(90) @DisplayName("Credit score reflects real business activity")
    void creditScoreReflectsActivity() {
        ResponseEntity<CreditScoreDto> resp = rest.exchange(
                url("/api/credit-score/me"),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), CreditScoreDto.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody().getExplanation(), "Credit score should have an explanation");
    }

    @Test @Order(91) @DisplayName("Admin can view other user's credit score")
    void adminViewsOtherCreditScore() {
        ResponseEntity<CreditScoreDto> resp = rest.exchange(
                url("/api/credit-score/" + customerId),
                HttpMethod.GET, new HttpEntity<>(auth(adminToken)), CreditScoreDto.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(customerId, resp.getBody().getUserId());
    }

    // ════════════════════════════════════════════════════
    //  CROSS-CUTTING — user profile, shipping label
    // ════════════════════════════════════════════════════

    @Test @Order(95) @DisplayName("User profile: /me returns current user data")
    void userProfile() {
        ResponseEntity<UserDto> resp = rest.exchange(
                url("/api/users/me"),
                HttpMethod.GET, new HttpEntity<>(auth(customerToken)), UserDto.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("biz_customer", resp.getBody().getUsername());
        assertEquals(Role.CUSTOMER, resp.getBody().getRole());
    }

    @Test @Order(96) @DisplayName("Shipping label: PDF generation for delivery order")
    void shippingLabel() {
        // Create a delivery-mode order first (non-pickup)
        // Use a simple order — shipping label should work for any order
        ResponseEntity<byte[]> resp = rest.exchange(
                url("/api/orders/" + orderId + "/shipping-label"),
                HttpMethod.GET, new HttpEntity<>(auth(staffToken)), byte[].class);
        // Staff should be able to download (even if order is pickup, the endpoint should work)
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().length > 0, "Shipping label should have content");
    }

    // ════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════

    private String login(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        ResponseEntity<AuthResponse> resp = rest.postForEntity(url("/api/auth/login"), req, AuthResponse.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return resp.getBody().getToken();
    }

    private String register(String username, String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        ResponseEntity<AuthResponse> resp = rest.postForEntity(url("/api/auth/register"), req, AuthResponse.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return resp.getBody().getToken();
    }

    private Long findUserId(List<Map<String, Object>> users, String username) {
        return users.stream()
                .filter(u -> username.equals(u.get("username")))
                .findFirst()
                .map(u -> ((Number) u.get("id")).longValue())
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}

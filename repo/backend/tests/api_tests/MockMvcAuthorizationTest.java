package api_tests;

import com.eaglepoint.storehub.StoreHubApplication;
import com.eaglepoint.storehub.controller.*;
import com.eaglepoint.storehub.config.GlobalExceptionHandler;
import com.eaglepoint.storehub.dto.*;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.enums.*;
import com.eaglepoint.storehub.repository.*;
import com.eaglepoint.storehub.security.UserPrincipal;
import com.eaglepoint.storehub.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real HTTP-level authorization integration tests using MockMvc.
 *
 * These tests exercise the actual Spring Security filter chain, route-level rules,
 * method-level @PreAuthorize annotations, and controller mapping. They hit real URLs
 * and validate that the security pipeline produces correct HTTP status codes.
 *
 * Authorization Matrix:
 * +------------------------------------+----------+----------+-------+----------+-----------+
 * | Endpoint                           | Unauthed | CUSTOMER | STAFF | SITE_MGR | ENT_ADMIN |
 * +------------------------------------+----------+----------+-------+----------+-----------+
 * | POST   /api/orders                 | 401      | 200      | 200   | 200      | 200       |
 * | GET    /api/orders/my              | 401      | 200      | 200   | 200      | 200       |
 * | GET    /api/orders/site/{siteId}   | 401      | 403      | 200   | 200      | 200       |
 * | PATCH  /api/orders/{id}/status     | 401      | 403      | 200   | 200      | 200       |
 * | POST   /api/checkins              | 401      | 403      | 200   | 200      | 200       |
 * | GET    /api/checkins/fraud-alerts  | 401      | 403      | 403   | 200      | 200       |
 * | POST   /api/tickets               | 401      | 200      | 200   | 200      | 200       |
 * | PATCH  /api/tickets/{id}/status   | 401      | 403      | 200   | 200      | 200       |
 * | POST   /api/ratings               | 401      | 200      | 200   | 200      | 200       |
 * | PATCH  /api/ratings/{id}/appeal/resolve | 401 | 403     | 403   | 200      | 200       |
 * | GET    /api/audit/range           | 401      | 403      | 403   | 403      | 200       |
 * | GET    /api/admin/incentive-rules | 401      | 403      | 403   | 403      | 200       |
 * | PATCH  /api/users/{id}/role       | 401      | 403      | 403   | 403      | 200       |
 * | DELETE /api/users/{id}            | 401      | 403      | 403   | 403      | 200       |
 * +------------------------------------+----------+----------+-------+----------+-----------+
 */
@ContextConfiguration(classes = StoreHubApplication.class)
@WebMvcTest(
    controllers = {
        OrderController.class,
        CheckInController.class,
        SupportTicketController.class,
        RatingController.class,
        UserController.class,
        AuditController.class,
        IncentiveRuleController.class,
        CommunityController.class,
        CreditScoreController.class,
        AnalyticsController.class,
        AddressController.class,
        DeliveryZoneController.class,
        DeliveryZoneGroupController.class,
        OrganizationController.class,
        AuthController.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.eaglepoint.storehub.security.JwtAuthenticationFilter.class,
            com.eaglepoint.storehub.config.RateLimitFilter.class,
            com.eaglepoint.storehub.config.IdempotencyFilter.class
        }
    )
)
@Import({MockMvcAuthorizationTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.security.recent-auth-window-ms=600000"
})
class MockMvcAuthorizationTest {

    /**
     * Test security config that mirrors the real SecurityConfig's route rules
     * without requiring custom filter beans (JWT, Rate Limit, Idempotency).
     */
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> response.sendError(401)))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ENTERPRISE_ADMIN")
                    .requestMatchers("/api/audit/**").hasAnyRole("ENTERPRISE_ADMIN", "SITE_MANAGER")
                    .requestMatchers("/api/credit-score/me").authenticated()
                    .requestMatchers("/api/credit-score/{userId}").hasAnyRole("ENTERPRISE_ADMIN", "SITE_MANAGER")
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    // ─── Service mocks ───
    @MockitoBean private OrderService orderService;
    @MockitoBean private ShippingLabelService shippingLabelService;
    @MockitoBean private SiteAuthorizationService siteAuth;
    @MockitoBean private CheckInService checkInService;
    @MockitoBean private SupportTicketService ticketService;
    @MockitoBean private EvidenceService evidenceService;
    @MockitoBean private RatingService ratingService;
    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private AuditService auditService;
    @MockitoBean private IncentiveRuleService incentiveRuleService;
    @MockitoBean private CommunityService communityService;
    @MockitoBean private GamificationService gamificationService;
    @MockitoBean private FavoriteService favoriteService;
    @MockitoBean private UserFollowService userFollowService;
    @MockitoBean private CreditScoreService creditScoreService;
    @MockitoBean private AnalyticsService analyticsService;
    @MockitoBean private ExperimentService experimentService;
    @MockitoBean private AddressService addressService;
    @MockitoBean private OrganizationService organizationService;
    @MockitoBean private DeliveryZoneService deliveryZoneService;
    @MockitoBean private DeliveryZoneGroupService deliveryZoneGroupService;

    // ─── Repository mocks (needed by some controllers directly) ───
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private OrderRepository orderRepository;
    @MockitoBean private SupportTicketRepository supportTicketRepository;
    @MockitoBean private CheckInRepository checkInRepository;

    // ════════════════════════════════════════════════════════════════
    //  UNAUTHENTICATED → 401
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Unauthenticated requests → 401")
    class UnauthenticatedTests {

        @Test void createOrder_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"siteId\":1,\"subtotal\":10.00,\"fulfillmentMode\":\"PICKUP\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void getMyOrders_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/api/orders/my"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void checkIn_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/checkins")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"siteId\":1}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void createTicket_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"orderId\":1,\"type\":\"REFUND_ONLY\",\"description\":\"test\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void submitRating_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/ratings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"orderId\":1,\"ratedUserId\":2,\"targetType\":\"STAFF\",\"stars\":5,\"timelinessScore\":5,\"communicationScore\":5,\"accuracyScore\":5}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void auditRange_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/api/audit/range")
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void adminIncentiveRules_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/api/admin/incentive-rules"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void updateUserRole_unauthenticated_401() throws Exception {
            mockMvc.perform(patch("/api/users/1/role").param("role", "STAFF"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void communityPost_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/community/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"test\",\"body\":\"test body\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void addresses_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/api/addresses"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  WRONG ROLE → 403
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Wrong role → 403")
    class WrongRoleTests {

        @Test void customer_cannotCheckIn_403() throws Exception {
            mockMvc.perform(post("/api/checkins")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"siteId\":1}"))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotViewOrdersBySite_403() throws Exception {
            mockMvc.perform(get("/api/orders/site/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotUpdateOrderStatus_403() throws Exception {
            mockMvc.perform(patch("/api/orders/1/status")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("status", "CONFIRMED"))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotViewFraudAlerts_403() throws Exception {
            mockMvc.perform(get("/api/checkins/fraud-alerts")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void staff_cannotViewFraudAlerts_403() throws Exception {
            mockMvc.perform(get("/api/checkins/fraud-alerts")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotUpdateTicketStatus_403() throws Exception {
            mockMvc.perform(patch("/api/tickets/1/status")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("status", "UNDER_REVIEW"))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotResolveAppeal_403() throws Exception {
            mockMvc.perform(patch("/api/ratings/1/appeal/resolve")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("resolution", "UPHELD"))
                    .andExpect(status().isForbidden());
        }

        @Test void staff_cannotResolveAppeal_403() throws Exception {
            mockMvc.perform(patch("/api/ratings/1/appeal/resolve")
                    .with(authentication(authFor(Role.STAFF, 10L)))
                    .param("resolution", "UPHELD"))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotAccessAudit_403() throws Exception {
            mockMvc.perform(get("/api/audit/range")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isForbidden());
        }

        @Test void staff_cannotAccessAudit_403() throws Exception {
            mockMvc.perform(get("/api/audit/range")
                    .with(authentication(authFor(Role.STAFF, 10L)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isForbidden());
        }

        @Test void siteManager_cannotAccessAuditRange_403() throws Exception {
            // Only ENTERPRISE_ADMIN can access /audit/range
            mockMvc.perform(get("/api/audit/range")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotAccessAdminEndpoints_403() throws Exception {
            mockMvc.perform(get("/api/admin/incentive-rules")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void staff_cannotAccessAdminEndpoints_403() throws Exception {
            mockMvc.perform(get("/api/admin/incentive-rules")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void siteManager_cannotAccessAdminEndpoints_403() throws Exception {
            mockMvc.perform(get("/api/admin/incentive-rules")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotUpdateUserRole_403() throws Exception {
            mockMvc.perform(patch("/api/users/1/role")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("role", "STAFF"))
                    .andExpect(status().isForbidden());
        }

        @Test void staff_cannotUpdateUserRole_403() throws Exception {
            mockMvc.perform(patch("/api/users/1/role")
                    .with(authentication(authFor(Role.STAFF, 10L)))
                    .param("role", "STAFF"))
                    .andExpect(status().isForbidden());
        }

        @Test void siteManager_cannotUpdateUserRole_403() throws Exception {
            mockMvc.perform(patch("/api/users/1/role")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("role", "STAFF"))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotDeleteUser_403() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotReviewQuarantine_403() throws Exception {
            mockMvc.perform(patch("/api/community/quarantine/1/review")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("legitimate", "true"))
                    .andExpect(status().isForbidden());
        }

        @Test void customer_cannotRemovePost_403() throws Exception {
            mockMvc.perform(delete("/api/community/posts/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CORRECT ROLE → Success (2xx)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Correct role → success")
    class CorrectRoleTests {

        @Test void customer_canCreateOrder() throws Exception {
            when(orderService.createOrder(anyLong(), any())).thenReturn(new OrderResponse());
            mockMvc.perform(post("/api/orders")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"siteId\":1,\"subtotal\":10.00,\"fulfillmentMode\":\"PICKUP\"}"))
                    .andExpect(status().isOk());
        }

        @Test void customer_canGetMyOrders() throws Exception {
            when(orderService.getOrdersByCustomer(anyLong(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            mockMvc.perform(get("/api/orders/my")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void staff_canCheckIn() throws Exception {
            when(checkInService.checkIn(anyLong(), any())).thenReturn(
                    CheckInResponse.builder().id(1L).status(CheckInStatus.VALID).build());
            mockMvc.perform(post("/api/checkins")
                    .with(authentication(authFor(Role.STAFF, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"siteId\":1}"))
                    .andExpect(status().isOk());
        }

        @Test void staff_canViewOrdersBySite() throws Exception {
            when(orderService.getOrdersBySite(anyLong(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            mockMvc.perform(get("/api/orders/site/1")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void siteManager_canViewFraudAlerts() throws Exception {
            when(checkInService.getUnresolvedAlerts()).thenReturn(List.of());
            mockMvc.perform(get("/api/checkins/fraud-alerts")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void customer_canCreateTicket() throws Exception {
            when(ticketService.createTicket(anyLong(), any())).thenReturn(new TicketResponse());
            mockMvc.perform(post("/api/tickets")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"orderId\":1,\"type\":\"REFUND_ONLY\",\"description\":\"test\"}"))
                    .andExpect(status().isOk());
        }

        @Test void customer_canSubmitRating() throws Exception {
            when(ratingService.submitRating(anyLong(), any())).thenReturn(new RatingResponse());
            mockMvc.perform(post("/api/ratings")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"orderId\":1,\"ratedUserId\":2,\"targetType\":\"STAFF\",\"stars\":5,\"timelinessScore\":5,\"communicationScore\":5,\"accuracyScore\":5}"))
                    .andExpect(status().isOk());
        }

        @Test void siteManager_canResolveAppeal() throws Exception {
            when(ratingService.resolveAppeal(anyLong(), any(), anyLong(), any()))
                    .thenReturn(new RatingResponse());
            mockMvc.perform(patch("/api/ratings/1/appeal/resolve")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("resolution", "UPHELD"))
                    .andExpect(status().isOk());
        }

        @Test void enterpriseAdmin_canAccessAuditRange() throws Exception {
            when(auditService.getAuditTrailByDateRange(any(), any())).thenReturn(List.of());
            mockMvc.perform(get("/api/audit/range")
                    .with(authentication(authFor(Role.ENTERPRISE_ADMIN, null)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isOk());
        }

        @Test void enterpriseAdmin_canAccessAdminEndpoints() throws Exception {
            when(incentiveRuleService.getAllRules()).thenReturn(List.of());
            mockMvc.perform(get("/api/admin/incentive-rules")
                    .with(authentication(authFor(Role.ENTERPRISE_ADMIN, null))))
                    .andExpect(status().isOk());
        }

        @Test void enterpriseAdmin_canUpdateUserRole() throws Exception {
            when(userService.updateRole(anyLong(), any())).thenReturn(new UserDto());
            mockMvc.perform(patch("/api/users/1/role")
                    .with(authentication(authFor(Role.ENTERPRISE_ADMIN, null)))
                    .param("role", "STAFF"))
                    .andExpect(status().isOk());
        }

        @Test void enterpriseAdmin_canDeleteUser() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                    .with(authentication(authFor(Role.ENTERPRISE_ADMIN, null))))
                    .andExpect(status().isNoContent());
        }

        @Test void customer_canSubmitAppeal() throws Exception {
            when(ratingService.submitAppeal(anyLong(), anyLong(), anyString()))
                    .thenReturn(new RatingResponse());
            mockMvc.perform(post("/api/ratings/1/appeal")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("reason", "unfair rating"))
                    .andExpect(status().isOk());
        }

        @Test void customer_canCreatePost() throws Exception {
            when(communityService.createPost(anyLong(), any())).thenReturn(new PostResponse());
            mockMvc.perform(post("/api/community/posts")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"test\",\"body\":\"test body\",\"topic\":\"general\"}"))
                    .andExpect(status().isOk());
        }

        @Test void customer_canManageAddresses() throws Exception {
            when(addressService.getByUser(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/addresses")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void siteManager_canAccessCreditScore() throws Exception {
            var targetUser = User.builder().id(99L).username("u").email("u@t.com")
                    .passwordHash("x").role(Role.CUSTOMER).enabled(true)
                    .site(Organization.builder().id(10L).name("Site").build()).build();
            when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(targetUser));
            when(creditScoreService.getScore(99L)).thenReturn(new CreditScoreDto(99L, 500, 0, 0, 0, 0, "Good"));
            mockMvc.perform(get("/api/credit-score/99")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CROSS-SITE DENIAL (checked via service, but proves route wiring)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cross-site denial → 403 via service")
    class CrossSiteTests {

        @Test void staff_cannotCheckInToCrossSite() throws Exception {
            when(checkInService.checkIn(anyLong(), any()))
                    .thenThrow(new com.eaglepoint.storehub.config.AccessDeniedException("Access denied: resource belongs to a different site"));
            mockMvc.perform(post("/api/checkins")
                    .with(authentication(authFor(Role.STAFF, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"siteId\":99}"))
                    .andExpect(status().isForbidden());
        }

        @Test void staff_cannotViewCrossSiteOrders() throws Exception {
            when(orderService.getOrdersBySite(anyLong(), any(Pageable.class)))
                    .thenThrow(new com.eaglepoint.storehub.config.AccessDeniedException("Access denied"));
            mockMvc.perform(get("/api/orders/site/99")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  EXPANDED COVERAGE — previously uncovered endpoints
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Analytics endpoints")
    class AnalyticsTests {

        @Test void logEvent_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/analytics/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"eventType\":\"PAGE_VIEW\",\"siteId\":1}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void logEvent_authenticated_200() throws Exception {
            mockMvc.perform(post("/api/analytics/events")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"eventType\":\"PAGE_VIEW\",\"siteId\":1}"))
                    .andExpect(status().isOk());
        }

        @Test void siteMetrics_customer_403() throws Exception {
            mockMvc.perform(get("/api/analytics/sites/1/metrics")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isForbidden());
        }

        @Test void siteMetrics_siteManager_200() throws Exception {
            when(analyticsService.getSiteMetrics(anyLong(), any(), any())).thenReturn(
                    new SiteMetrics(1L, java.util.Map.of(), java.util.Map.of(), null, null, null, "On Time"));
            mockMvc.perform(get("/api/analytics/sites/1/metrics")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isOk());
        }

        @Test void retention_customer_403() throws Exception {
            mockMvc.perform(get("/api/analytics/sites/1/retention")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("cohortDate", "2024-01-01T00:00:00Z"))
                    .andExpect(status().isForbidden());
        }

        @Test void createExperiment_customer_403() throws Exception {
            mockMvc.perform(post("/api/analytics/experiments")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"test\",\"type\":\"AB_TEST\",\"variantCount\":2}"))
                    .andExpect(status().isForbidden());
        }

        @Test void createExperiment_siteManager_200() throws Exception {
            when(experimentService.createExperiment(any())).thenReturn(new ExperimentDto());
            mockMvc.perform(post("/api/analytics/experiments")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"test\",\"type\":\"AB_TEST\",\"variantCount\":2}"))
                    .andExpect(status().isOk());
        }

        @Test void getActiveExperiments_authenticated_200() throws Exception {
            when(experimentService.getActiveExperiments()).thenReturn(List.of());
            mockMvc.perform(get("/api/analytics/experiments")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getBucket_authenticated_200() throws Exception {
            when(experimentService.getBucket(anyLong(), anyString())).thenReturn(java.util.Map.of("variant", 0));
            mockMvc.perform(get("/api/analytics/experiments/test-exp/bucket")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void recordOutcome_authenticated_200() throws Exception {
            mockMvc.perform(post("/api/analytics/experiments/test-exp/outcome")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("variant", "0")
                    .param("reward", "1.0"))
                    .andExpect(status().isOk());
        }

        @Test void updateExperiment_customer_403() throws Exception {
            mockMvc.perform(put("/api/analytics/experiments/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"test\",\"type\":\"AB_TEST\",\"variantCount\":3}"))
                    .andExpect(status().isForbidden());
        }

        @Test void deactivateExperiment_customer_403() throws Exception {
            mockMvc.perform(patch("/api/analytics/experiments/1/deactivate")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void rollbackExperiment_customer_403() throws Exception {
            mockMvc.perform(post("/api/analytics/experiments/1/rollback")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Audit entity/user endpoints")
    class AuditDetailTests {

        @Test void auditEntity_customer_403() throws Exception {
            mockMvc.perform(get("/api/audit/entity/Order/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void auditEntity_staff_403() throws Exception {
            mockMvc.perform(get("/api/audit/entity/Order/1")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void auditEntity_siteManager_200() throws Exception {
            var order = com.eaglepoint.storehub.entity.Order.builder().id(1L)
                    .site(Organization.builder().id(10L).name("S").build()).build();
            when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
            when(auditService.getAuditTrail(anyString(), anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/audit/entity/Order/1")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void auditUser_customer_403() throws Exception {
            mockMvc.perform(get("/api/audit/user/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void auditUser_siteManager_200() throws Exception {
            var targetUser = User.builder().id(1L).username("u").email("u@t.com")
                    .passwordHash("x").role(Role.CUSTOMER).enabled(true)
                    .site(Organization.builder().id(10L).name("S").build()).build();
            when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(targetUser));
            when(auditService.getAuditTrailByUser(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/audit/user/1")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("User endpoints")
    class UserTests {

        @Test void getUsers_customer_403() throws Exception {
            mockMvc.perform(get("/api/users")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void getUsers_siteManager_200() throws Exception {
            when(userService.findAllUsers()).thenReturn(List.of());
            mockMvc.perform(get("/api/users")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getMe_authenticated_200() throws Exception {
            when(userService.findById(anyLong())).thenReturn(new UserDto());
            mockMvc.perform(get("/api/users/me")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getUserById_customer_403() throws Exception {
            mockMvc.perform(get("/api/users/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void reauth_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/users/reauth")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"test\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void reauth_authenticated_200() throws Exception {
            when(authService.reauthenticate(anyLong(), anyString()))
                    .thenReturn(new AuthResponse("token", "user", "CUSTOMER", 10L));
            mockMvc.perform(post("/api/users/reauth")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"test\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Checkin site endpoint")
    class CheckInSiteTests {

        @Test void getCheckInsBySite_customer_403() throws Exception {
            mockMvc.perform(get("/api/checkins/site/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isForbidden());
        }

        @Test void getCheckInsBySite_teamLead_200() throws Exception {
            when(checkInService.getCheckInsBySite(anyLong(), any(), any())).thenReturn(List.of());
            mockMvc.perform(get("/api/checkins/site/1")
                    .with(authentication(authFor(Role.TEAM_LEAD, 10L)))
                    .param("start", "2024-01-01T00:00:00Z")
                    .param("end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Order detail endpoints")
    class OrderDetailTests {

        @Test void getOrderById_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void getOrderById_authenticated_200() throws Exception {
            when(orderService.getOrder(anyLong(), anyLong(), anyString())).thenReturn(new OrderResponse());
            mockMvc.perform(get("/api/orders/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void verifyPickup_customer_403() throws Exception {
            mockMvc.perform(post("/api/orders/1/verify-pickup")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("code", "123456"))
                    .andExpect(status().isForbidden());
        }

        @Test void verifyPickup_staff_200() throws Exception {
            when(orderService.verifyPickup(anyLong(), anyString(), anyLong(), anyString())).thenReturn(new OrderResponse());
            mockMvc.perform(post("/api/orders/1/verify-pickup")
                    .with(authentication(authFor(Role.STAFF, 10L)))
                    .param("code", "123456"))
                    .andExpect(status().isOk());
        }

        @Test void shippingLabel_customer_403() throws Exception {
            mockMvc.perform(get("/api/orders/1/shipping-label")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void shippingLabel_staff_200() throws Exception {
            when(shippingLabelService.generateLabel(anyLong())).thenReturn(new byte[]{0x25, 0x50});
            mockMvc.perform(get("/api/orders/1/shipping-label")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Ticket detail endpoints")
    class TicketDetailTests {

        @Test void getTicketById_unauthenticated_401() throws Exception {
            mockMvc.perform(get("/api/tickets/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void getTicketById_authenticated_200() throws Exception {
            when(ticketService.getTicket(anyLong(), anyLong(), anyString())).thenReturn(new TicketResponse());
            mockMvc.perform(get("/api/tickets/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getMyTickets_authenticated_200() throws Exception {
            when(ticketService.getMyTickets(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/tickets/my")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getTicketsByStatus_customer_403() throws Exception {
            mockMvc.perform(get("/api/tickets/status/OPEN")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void getTicketsByStatus_staff_200() throws Exception {
            when(ticketService.getTicketsByStatus(any())).thenReturn(List.of());
            mockMvc.perform(get("/api/tickets/status/OPEN")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void assignTicket_customer_403() throws Exception {
            mockMvc.perform(patch("/api/tickets/1/assign")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("staffId", "5"))
                    .andExpect(status().isForbidden());
        }

        @Test void assignTicket_siteManager_200() throws Exception {
            when(ticketService.assignTicket(anyLong(), anyLong())).thenReturn(new TicketResponse());
            mockMvc.perform(patch("/api/tickets/1/assign")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("staffId", "5"))
                    .andExpect(status().isOk());
        }

        @Test void getEvidence_authenticated_200() throws Exception {
            when(ticketService.getTicket(anyLong(), anyLong(), anyString())).thenReturn(new TicketResponse());
            when(evidenceService.getEvidenceForTicket(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/tickets/1/evidence")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void verifyEvidence_customer_403() throws Exception {
            mockMvc.perform(get("/api/tickets/evidence/1/verify")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void verifyEvidence_siteManager_200() throws Exception {
            when(evidenceService.verifyIntegrity(anyLong())).thenReturn(true);
            mockMvc.perform(get("/api/tickets/evidence/1/verify")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Rating detail endpoints")
    class RatingDetailTests {

        @Test void getRatingsForUser_authenticated_200() throws Exception {
            when(ratingService.getRatingsForUser(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/ratings/user/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getAverageRating_authenticated_200() throws Exception {
            when(ratingService.getAverageRating(anyLong())).thenReturn(4.5);
            mockMvc.perform(get("/api/ratings/user/1/average")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getPendingAppeals_customer_403() throws Exception {
            mockMvc.perform(get("/api/ratings/appeals/pending")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void getPendingAppeals_siteManager_200() throws Exception {
            when(ratingService.getPendingAppeals()).thenReturn(List.of());
            mockMvc.perform(get("/api/ratings/appeals/pending")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Community detail endpoints")
    class CommunityDetailTests {

        @Test void getFeed_authenticated_200() throws Exception {
            when(communityService.getFeed(anyLong(), any())).thenReturn(new PageImpl<>(List.of()));
            mockMvc.perform(get("/api/community/posts")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getByTopic_authenticated_200() throws Exception {
            when(communityService.getFeedByTopic(anyString(), anyLong(), any())).thenReturn(new PageImpl<>(List.of()));
            mockMvc.perform(get("/api/community/posts/topic/general")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getFollowedFeed_authenticated_200() throws Exception {
            when(communityService.getFollowedFeed(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/community/posts/following")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void addComment_authenticated_200() throws Exception {
            when(communityService.addComment(anyLong(), anyLong(), any())).thenReturn(new CommentResponse());
            mockMvc.perform(post("/api/community/posts/1/comments")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"body\":\"nice post\"}"))
                    .andExpect(status().isOk());
        }

        @Test void getComments_authenticated_200() throws Exception {
            when(communityService.getComments(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/community/posts/1/comments")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void vote_authenticated_200() throws Exception {
            when(communityService.vote(anyLong(), anyLong(), any())).thenReturn(new PostResponse());
            mockMvc.perform(post("/api/community/posts/1/vote")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("type", "UPVOTE"))
                    .andExpect(status().isOk());
        }

        @Test void followTopic_authenticated_200() throws Exception {
            mockMvc.perform(post("/api/community/topics/java/follow")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void unfollowTopic_authenticated_204() throws Exception {
            mockMvc.perform(delete("/api/community/topics/java/follow")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isNoContent());
        }

        @Test void getFollowedTopics_authenticated_200() throws Exception {
            when(communityService.getFollowedTopics(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/community/topics/following")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getMyPoints_authenticated_200() throws Exception {
            when(gamificationService.getProfile(anyLong())).thenReturn(
                    new PointsProfile(1L, 0, com.eaglepoint.storehub.enums.CommunityLevel.NEWCOMER, 100, "Newcomer"));
            mockMvc.perform(get("/api/community/points/me")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void toggleFavorite_authenticated_200() throws Exception {
            when(favoriteService.toggleFavorite(anyLong(), anyLong())).thenReturn(true);
            mockMvc.perform(post("/api/community/posts/1/favorite")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getFavorites_authenticated_200() throws Exception {
            when(favoriteService.getFavorites(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/community/favorites")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void followUser_authenticated_200() throws Exception {
            mockMvc.perform(post("/api/community/users/2/follow")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void unfollowUser_authenticated_204() throws Exception {
            mockMvc.perform(delete("/api/community/users/2/follow")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isNoContent());
        }

        @Test void getFollowing_authenticated_200() throws Exception {
            when(userFollowService.getFollowing(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/community/following")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getPendingQuarantines_customer_403() throws Exception {
            mockMvc.perform(get("/api/community/quarantine/pending")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void getPendingQuarantines_siteManager_200() throws Exception {
            when(communityService.getPendingQuarantines()).thenReturn(List.of());
            mockMvc.perform(get("/api/community/quarantine/pending")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Credit score endpoints")
    class CreditScoreTests {

        @Test void getOwnCreditScore_authenticated_200() throws Exception {
            when(creditScoreService.getScore(anyLong())).thenReturn(new CreditScoreDto(1L, 500, 0, 0, 0, 0, "Good"));
            mockMvc.perform(get("/api/credit-score/me")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getOtherCreditScore_customer_403() throws Exception {
            mockMvc.perform(get("/api/credit-score/99")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Address CRUD endpoints")
    class AddressCrudTests {

        @Test void createAddress_unauthenticated_401() throws Exception {
            mockMvc.perform(post("/api/addresses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"label\":\"Home\",\"street\":\"123 Main\",\"city\":\"Town\",\"state\":\"CA\",\"zipCode\":\"90001\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test void createAddress_authenticated_200() throws Exception {
            when(addressService.create(anyLong(), any())).thenReturn(new AddressDto());
            mockMvc.perform(post("/api/addresses")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"label\":\"Home\",\"street\":\"123 Main\",\"city\":\"Town\",\"state\":\"CA\",\"zipCode\":\"90001\"}"))
                    .andExpect(status().isOk());
        }

        @Test void updateAddress_authenticated_200() throws Exception {
            when(addressService.update(anyLong(), anyLong(), any())).thenReturn(new AddressDto());
            mockMvc.perform(put("/api/addresses/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"label\":\"Work\",\"street\":\"456 Oak\",\"city\":\"Town\",\"state\":\"CA\",\"zipCode\":\"90002\"}"))
                    .andExpect(status().isOk());
        }

        @Test void deleteAddress_authenticated_204() throws Exception {
            mockMvc.perform(delete("/api/addresses/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Delivery zone endpoints")
    class DeliveryZoneTests {

        @Test void getDeliveryZones_customer_403() throws Exception {
            mockMvc.perform(get("/api/delivery-zones/site/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void getDeliveryZones_siteManager_200() throws Exception {
            when(deliveryZoneService.getBySite(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/delivery-zones/site/1")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void createDeliveryZone_customer_403() throws Exception {
            mockMvc.perform(post("/api/delivery-zones")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("siteId", "1").param("zipCode", "90001")
                    .param("distanceMiles", "3.0").param("deliveryFee", "4.99"))
                    .andExpect(status().isForbidden());
        }

        @Test void deleteDeliveryZone_siteManager_403() throws Exception {
            // DELETE requires ENTERPRISE_ADMIN only
            mockMvc.perform(delete("/api/delivery-zones/1")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Delivery zone group endpoints")
    class DeliveryZoneGroupTests {

        @Test void getZoneGroups_customer_403() throws Exception {
            mockMvc.perform(get("/api/delivery-zone-groups/site/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void getZoneGroups_siteManager_200() throws Exception {
            when(deliveryZoneGroupService.getBySite(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/delivery-zone-groups/site/1")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void createZoneGroup_customer_403() throws Exception {
            mockMvc.perform(post("/api/delivery-zone-groups")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("siteId", "1").param("name", "Metro"))
                    .andExpect(status().isForbidden());
        }

        @Test void deactivateZoneGroup_customer_403() throws Exception {
            mockMvc.perform(patch("/api/delivery-zone-groups/1/deactivate")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Organization endpoints")
    class OrganizationTests {

        @Test void getOrganizations_customer_403() throws Exception {
            mockMvc.perform(get("/api/organizations")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void getOrganizations_staff_200() throws Exception {
            when(organizationService.findAll()).thenReturn(List.of());
            mockMvc.perform(get("/api/organizations")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getByLevel_staff_200() throws Exception {
            when(organizationService.findByLevel(any())).thenReturn(List.of());
            mockMvc.perform(get("/api/organizations/level/SITE")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void getChildren_staff_200() throws Exception {
            when(organizationService.findChildren(anyLong())).thenReturn(List.of());
            mockMvc.perform(get("/api/organizations/1/children")
                    .with(authentication(authFor(Role.STAFF, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void createOrganization_staff_403() throws Exception {
            mockMvc.perform(post("/api/organizations")
                    .with(authentication(authFor(Role.STAFF, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"New Site\",\"level\":\"SITE\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test void createOrganization_admin_200() throws Exception {
            when(organizationService.create(any())).thenReturn(new OrganizationDto());
            mockMvc.perform(post("/api/organizations")
                    .with(authentication(authFor(Role.ENTERPRISE_ADMIN, null)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"New Site\",\"level\":\"SITE\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Incentive rule mutation endpoints")
    class IncentiveRuleMutationTests {

        @Test void updateRule_siteManager_403() throws Exception {
            mockMvc.perform(put("/api/admin/incentive-rules/POST_CREATED")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"points\":10}"))
                    .andExpect(status().isForbidden());
        }

        @Test void toggleRule_siteManager_403() throws Exception {
            mockMvc.perform(patch("/api/admin/incentive-rules/POST_CREATED/toggle")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  REMAINING COVERAGE — last 10 uncovered endpoints
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Auth public endpoints")
    class AuthTests {

        @Test void login_public_200() throws Exception {
            when(authService.login(any())).thenReturn(new AuthResponse("tok", "admin", "ENTERPRISE_ADMIN", null));
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"admin\",\"password\":\"Dev!Storehub99\"}"))
                    .andExpect(status().isOk());
        }

        @Test void register_public_200() throws Exception {
            when(authService.register(any())).thenReturn(new AuthResponse("tok", "newuser", "CUSTOMER", null));
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"newuser\",\"password\":\"StrongP@ss1\",\"email\":\"new@test.com\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Fraud alert resolve endpoint")
    class FraudAlertResolveTests {

        @Test void resolveFraudAlert_customer_403() throws Exception {
            mockMvc.perform(patch("/api/checkins/fraud-alerts/1/resolve")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("note", "Resolved after review of evidence"))
                    .andExpect(status().isForbidden());
        }

        @Test void resolveFraudAlert_siteManager_200() throws Exception {
            when(checkInService.resolveFraudAlert(anyLong(), anyLong(), anyString()))
                    .thenReturn(com.eaglepoint.storehub.entity.FraudAlert.builder().id(1L).resolved(true).build());
            mockMvc.perform(patch("/api/checkins/fraud-alerts/1/resolve")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("note", "Resolved after review of evidence"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Community points by userId endpoint")
    class CommunityPointsByUserIdTests {

        @Test void getPointsByUserId_authenticated_200() throws Exception {
            // Request own points (userId matches principal.id) — no extra auth check
            when(gamificationService.getProfile(anyLong())).thenReturn(
                    new PointsProfile(1L, 50, com.eaglepoint.storehub.enums.CommunityLevel.NEWCOMER, 50, "Newcomer"));
            mockMvc.perform(get("/api/community/points/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Delivery zone update endpoint")
    class DeliveryZoneUpdateTests {

        @Test void updateDeliveryZone_customer_403() throws Exception {
            mockMvc.perform(put("/api/delivery-zones/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("zipCode", "90001")
                    .param("distanceMiles", "3.0")
                    .param("deliveryFee", "4.99")
                    .param("active", "true"))
                    .andExpect(status().isForbidden());
        }

        @Test void updateDeliveryZone_siteManager_200() throws Exception {
            com.eaglepoint.storehub.entity.DeliveryZone zone = com.eaglepoint.storehub.entity.DeliveryZone.builder()
                    .id(1L).site(Organization.builder().id(10L).name("S").build())
                    .zipCode("90001").distanceMiles(3.0).active(true).build();
            when(deliveryZoneService.getById(1L)).thenReturn(zone);
            when(deliveryZoneService.update(anyLong(), anyString(), anyDouble(), any(), anyBoolean())).thenReturn(zone);
            mockMvc.perform(put("/api/delivery-zones/1")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("zipCode", "90001")
                    .param("distanceMiles", "3.0")
                    .param("deliveryFee", "4.99")
                    .param("active", "true"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Delivery zone group ZIP and band CRUD endpoints")
    class DeliveryZoneGroupCrudTests {

        private com.eaglepoint.storehub.entity.DeliveryZoneGroup mockGroup() {
            return com.eaglepoint.storehub.entity.DeliveryZoneGroup.builder()
                    .id(1L).site(Organization.builder().id(10L).name("S").build())
                    .name("Metro").active(true).build();
        }

        @Test void addZipCode_customer_403() throws Exception {
            mockMvc.perform(post("/api/delivery-zone-groups/1/zips")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("zipCode", "90001")
                    .param("distanceMiles", "3.0"))
                    .andExpect(status().isForbidden());
        }

        @Test void addZipCode_siteManager_200() throws Exception {
            var group = mockGroup();
            when(deliveryZoneGroupService.getById(1L)).thenReturn(group);
            when(deliveryZoneGroupService.addZipCode(anyLong(), anyString(), anyDouble())).thenReturn(group);
            mockMvc.perform(post("/api/delivery-zone-groups/1/zips")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("zipCode", "90001")
                    .param("distanceMiles", "3.0"))
                    .andExpect(status().isOk());
        }

        @Test void removeZipCode_customer_403() throws Exception {
            mockMvc.perform(delete("/api/delivery-zone-groups/1/zips/90001")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void removeZipCode_siteManager_200() throws Exception {
            var group = mockGroup();
            when(deliveryZoneGroupService.getById(1L)).thenReturn(group);
            when(deliveryZoneGroupService.removeZipCode(anyLong(), anyString())).thenReturn(group);
            mockMvc.perform(delete("/api/delivery-zone-groups/1/zips/90001")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }

        @Test void addBand_customer_403() throws Exception {
            mockMvc.perform(post("/api/delivery-zone-groups/1/bands")
                    .with(authentication(authFor(Role.CUSTOMER, 10L)))
                    .param("minMiles", "0")
                    .param("maxMiles", "5")
                    .param("fee", "4.99"))
                    .andExpect(status().isForbidden());
        }

        @Test void addBand_siteManager_200() throws Exception {
            var group = mockGroup();
            when(deliveryZoneGroupService.getById(1L)).thenReturn(group);
            when(deliveryZoneGroupService.addDistanceBand(anyLong(), anyDouble(), anyDouble(), any())).thenReturn(group);
            mockMvc.perform(post("/api/delivery-zone-groups/1/bands")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L)))
                    .param("minMiles", "0")
                    .param("maxMiles", "5")
                    .param("fee", "4.99"))
                    .andExpect(status().isOk());
        }

        @Test void removeBand_customer_403() throws Exception {
            mockMvc.perform(delete("/api/delivery-zone-groups/1/bands/1")
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test void removeBand_siteManager_200() throws Exception {
            var group = mockGroup();
            when(deliveryZoneGroupService.getById(1L)).thenReturn(group);
            when(deliveryZoneGroupService.removeBand(anyLong(), anyLong())).thenReturn(group);
            mockMvc.perform(delete("/api/delivery-zone-groups/1/bands/1")
                    .with(authentication(authFor(Role.SITE_MANAGER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Ticket evidence upload endpoint")
    class TicketEvidenceUploadTests {

        @Test void uploadEvidence_unauthenticated_401() throws Exception {
            mockMvc.perform(multipart("/api/tickets/1/evidence")
                    .file(new org.springframework.mock.web.MockMultipartFile(
                            "file", "receipt.pdf", "application/pdf", new byte[]{0x25, 0x50})))
                    .andExpect(status().isUnauthorized());
        }

        @Test void uploadEvidence_authenticated_200() throws Exception {
            when(evidenceService.uploadEvidence(anyLong(), anyLong(), any()))
                    .thenReturn(new EvidenceDto());
            mockMvc.perform(multipart("/api/tickets/1/evidence")
                    .file(new org.springframework.mock.web.MockMultipartFile(
                            "file", "receipt.pdf", "application/pdf", new byte[]{0x25, 0x50}))
                    .with(authentication(authFor(Role.CUSTOMER, 10L))))
                    .andExpect(status().isOk());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════

    private static Authentication authFor(Role role, Long siteId) {
        User.UserBuilder builder = User.builder()
                .id(1L).username("testuser").email("test@test.com")
                .passwordHash("hashed").role(role).enabled(true).tokenVersion(0);
        if (siteId != null) {
            builder.site(Organization.builder().id(siteId).name("TestSite").build());
        }
        UserPrincipal principal = new UserPrincipal(builder.build());
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}

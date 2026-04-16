package unit_tests;

import com.eaglepoint.storehub.config.AccessDeniedException;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.enums.Role;
import com.eaglepoint.storehub.repository.OrganizationRepository;
import com.eaglepoint.storehub.security.UserPrincipal;
import com.eaglepoint.storehub.service.SiteAuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteAuthorizationServiceTest {

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private SiteAuthorizationService create() {
        return new SiteAuthorizationService(mock(OrganizationRepository.class));
    }

    private void setPrincipal(Long userId, Role role, Long siteId) {
        User.UserBuilder b = User.builder().id(userId).username("u").email("u@t.com")
                .passwordHash("x").role(role).enabled(true);
        if (siteId != null) b.site(Organization.builder().id(siteId).name("S").build());
        UserPrincipal p = new UserPrincipal(b.build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
    }

    @Test @DisplayName("ENTERPRISE_ADMIN can access any site")
    void admin_canAccessAnySite() {
        setPrincipal(1L, Role.ENTERPRISE_ADMIN, null);
        assertTrue(create().canAccessSite(10L));
        assertTrue(create().canAccessSite(null));
    }

    @Test @DisplayName("STAFF can access own site only")
    void staff_ownSiteOnly() {
        setPrincipal(1L, Role.STAFF, 10L);
        var svc = create();
        assertTrue(svc.canAccessSite(10L));
        assertFalse(svc.canAccessSite(20L));
    }

    @Test @DisplayName("requireSiteAccess throws for cross-site")
    void requireSiteAccess_crossSite_throws() {
        setPrincipal(1L, Role.STAFF, 10L);
        assertThrows(AccessDeniedException.class, () -> create().requireSiteAccess(20L));
    }

    @Test @DisplayName("requireOwnerOrSiteAccess allows self-access")
    void ownerAccess_allowed() {
        setPrincipal(5L, Role.CUSTOMER, 10L);
        assertDoesNotThrow(() -> create().requireOwnerOrSiteAccess(5L, 20L));
    }

    @Test @DisplayName("requireDeviceMatch throws on mismatch")
    void deviceMismatch_throws() {
        assertThrows(AccessDeniedException.class,
                () -> create().requireDeviceMatch("expected", "actual"));
    }

    @Test @DisplayName("requireDeviceMatch allows match")
    void deviceMatch_allowed() {
        assertDoesNotThrow(() -> create().requireDeviceMatch("same", "same"));
    }

    @Test @DisplayName("requireWorkOrderScope throws when no assignment")
    void workOrderScope_noAssignment_throws() {
        assertThrows(AccessDeniedException.class,
                () -> create().requireWorkOrderScope(false));
    }
}

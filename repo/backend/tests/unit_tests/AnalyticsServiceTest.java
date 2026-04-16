package unit_tests;

import com.eaglepoint.storehub.dto.EventRequest;
import com.eaglepoint.storehub.dto.SiteMetrics;
import com.eaglepoint.storehub.entity.AnalyticsEvent;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.enums.EventType;
import com.eaglepoint.storehub.repository.AnalyticsEventRepository;
import com.eaglepoint.storehub.repository.OrganizationRepository;
import com.eaglepoint.storehub.repository.SupportTicketRepository;
import com.eaglepoint.storehub.repository.UserRepository;
import com.eaglepoint.storehub.service.AnalyticsService;
import com.eaglepoint.storehub.service.SiteAuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private AnalyticsEventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private SupportTicketRepository ticketRepository;
    @Mock private SiteAuthorizationService siteAuth;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("logEvent persists analytics event")
    void logEvent_persistsEvent() {
        EventRequest req = new EventRequest();
        req.setEventType(EventType.PAGE_VIEW);
        req.setSiteId(1L);
        req.setTarget("/home");

        when(organizationRepository.findById(1L)).thenReturn(
                Optional.of(Organization.builder().id(1L).name("S").build()));
        when(eventRepository.save(any())).thenAnswer(inv -> {
            AnalyticsEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        analyticsService.logEvent(1L, req);
        verify(eventRepository).save(any(AnalyticsEvent.class));
    }

    @Test
    @DisplayName("getSiteMetrics returns metrics for date range")
    void getSiteMetrics_returnsMetrics() {
        Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = Instant.now();

        when(eventRepository.getEventSummary(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(ticketRepository.findOverdueSlaTickets(any()))
                .thenReturn(List.of());

        SiteMetrics metrics = analyticsService.getSiteMetrics(1L, start, end);
        assertNotNull(metrics);
        assertEquals(1L, metrics.getSiteId());
        assertEquals("On Time", metrics.getPerformanceStatus());
    }
}

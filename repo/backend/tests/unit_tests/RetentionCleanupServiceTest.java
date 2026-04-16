package unit_tests;

import com.eaglepoint.storehub.entity.SupportTicket;
import com.eaglepoint.storehub.enums.TicketStatus;
import com.eaglepoint.storehub.repository.EvidenceFileRepository;
import com.eaglepoint.storehub.repository.SupportTicketRepository;
import com.eaglepoint.storehub.service.AuditService;
import com.eaglepoint.storehub.service.RetentionCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetentionCleanupServiceTest {

    @Mock private SupportTicketRepository ticketRepository;
    @Mock private EvidenceFileRepository evidenceFileRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private RetentionCleanupService retentionCleanupService;

    @Test @DisplayName("cleanupExpiredTickets removes tickets beyond retention period")
    void cleanupExpiredTickets_removesOldTickets() {
        SupportTicket ticket = SupportTicket.builder()
                .id(1L).status(TicketStatus.CLOSED)
                .createdAt(Instant.now().minus(25 * 30, ChronoUnit.DAYS))
                .build();

        when(ticketRepository.findExpiredRetentionTickets(any()))
                .thenReturn(List.of(ticket));

        retentionCleanupService.cleanupExpiredTickets();

        verify(ticketRepository).delete(ticket);
    }

    @Test @DisplayName("cleanupExpiredTickets does nothing when no expired tickets")
    void cleanupExpiredTickets_noneExpired() {
        when(ticketRepository.findExpiredRetentionTickets(any()))
                .thenReturn(List.of());

        retentionCleanupService.cleanupExpiredTickets();
        verify(ticketRepository, never()).delete(any(SupportTicket.class));
    }
}

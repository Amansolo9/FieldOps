package unit_tests;

import com.eaglepoint.storehub.entity.SupportTicket;
import com.eaglepoint.storehub.enums.TicketStatus;
import com.eaglepoint.storehub.repository.SupportTicketRepository;
import com.eaglepoint.storehub.service.AuditService;
import com.eaglepoint.storehub.service.SlaTimerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eaglepoint.storehub.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaTimerServiceTest {

    @Mock private SupportTicketRepository ticketRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private SlaTimerService slaTimerService;

    @Test @DisplayName("checkOverdueTickets marks breached tickets")
    void checkOverdueTickets_marksBreached() {
        SupportTicket ticket = SupportTicket.builder()
                .id(1L).status(TicketStatus.OPEN).slaBreached(false)
                .customer(User.builder().id(1L).username("u").build())
                .firstResponseDueAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(ticketRepository.findOverdueSlaTickets(any()))
                .thenReturn(List.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        slaTimerService.checkOverdueTickets();

        verify(ticketRepository).save(argThat(t -> t.isSlaBreached()));
    }

    @Test @DisplayName("checkOverdueTickets does nothing when no overdue tickets")
    void checkOverdueTickets_noneOverdue() {
        when(ticketRepository.findOverdueSlaTickets(any()))
                .thenReturn(List.of());

        slaTimerService.checkOverdueTickets();
        verify(ticketRepository, never()).save(any());
    }
}

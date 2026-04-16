package unit_tests;

import com.eaglepoint.storehub.entity.AuditLog;
import com.eaglepoint.storehub.repository.AuditLogRepository;
import com.eaglepoint.storehub.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditService auditService;

    @Test @DisplayName("logAction persists audit log entry")
    void logAction_persists() {
        when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        auditService.logAction("CREATE", "Order", 1L, null, "{ \"id\": 1 }");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test @DisplayName("logSystemAction persists with system actor")
    void logSystemAction_persists() {
        when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        auditService.logSystemAction("BOOTSTRAP_ADMIN", "User", 1L, "Admin created");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test @DisplayName("getAuditTrail returns logs for entity")
    void getAuditTrail_returnsList() {
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("Order", 1L))
                .thenReturn(List.of());
        List<AuditLog> result = auditService.getAuditTrail("Order", 1L);
        assertNotNull(result);
    }

    @Test @DisplayName("getAuditTrailByUser returns logs for user")
    void getAuditTrailByUser_returnsList() {
        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        List<AuditLog> result = auditService.getAuditTrailByUser(1L);
        assertNotNull(result);
    }

    @Test @DisplayName("getAuditTrailByDateRange returns filtered logs")
    void getAuditTrailByDateRange_returnsList() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-12-31T23:59:59Z");
        when(auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end))
                .thenReturn(List.of());
        List<AuditLog> result = auditService.getAuditTrailByDateRange(start, end);
        assertNotNull(result);
    }
}

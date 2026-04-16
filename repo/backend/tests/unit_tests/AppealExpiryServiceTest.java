package unit_tests;

import com.eaglepoint.storehub.entity.Rating;
import com.eaglepoint.storehub.enums.AppealStatus;
import com.eaglepoint.storehub.repository.RatingRepository;
import com.eaglepoint.storehub.service.AppealExpiryService;
import com.eaglepoint.storehub.service.AuditService;
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
class AppealExpiryServiceTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private AppealExpiryService appealExpiryService;

    @Test @DisplayName("processExpiredAppeals expires old pending appeals")
    void processExpiredAppeals_expiresOld() {
        Rating rating = Rating.builder()
                .id(1L).appealStatus(AppealStatus.PENDING)
                .build();

        when(ratingRepository.findExpiredAppeals())
                .thenReturn(List.of(rating));
        when(ratingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        appealExpiryService.processExpiredAppeals();

        verify(ratingRepository).save(argThat(r -> r.getAppealStatus() == AppealStatus.EXPIRED));
    }

    @Test @DisplayName("processExpiredAppeals does nothing when no expired appeals")
    void processExpiredAppeals_noneExpired() {
        when(ratingRepository.findExpiredAppeals())
                .thenReturn(List.of());

        appealExpiryService.processExpiredAppeals();
        verify(ratingRepository, never()).save(any());
    }
}

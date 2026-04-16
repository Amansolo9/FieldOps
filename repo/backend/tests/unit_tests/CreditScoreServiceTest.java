package unit_tests;

import com.eaglepoint.storehub.dto.CreditScoreDto;
import com.eaglepoint.storehub.entity.CreditScore;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.enums.Role;
import com.eaglepoint.storehub.repository.CreditScoreRepository;
import com.eaglepoint.storehub.repository.UserRepository;
import com.eaglepoint.storehub.service.CreditScoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditScoreServiceTest {

    @Mock private CreditScoreRepository creditScoreRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CreditScoreService creditScoreService;

    @Test
    @DisplayName("getScore returns DTO for existing credit score")
    void getScore_existingScore_returnsDto() {
        User user = User.builder().id(1L).username("u").email("u@t.com")
                .passwordHash("x").role(Role.CUSTOMER).enabled(true).build();
        CreditScore cs = CreditScore.builder()
                .id(1L).user(user).scoreEncrypted("500")
                .ratingImpact(0).communityImpact(0).orderImpact(0).disputeImpact(0)
                .build();

        when(creditScoreRepository.findByUserId(1L)).thenReturn(Optional.of(cs));

        CreditScoreDto dto = creditScoreService.getScore(1L);
        assertNotNull(dto);
        assertEquals(1L, dto.getUserId());
    }

    @Test
    @DisplayName("getOrCreate creates new score when none exists")
    void getOrCreate_newScore() {
        User user = User.builder().id(1L).username("u").email("u@t.com")
                .passwordHash("x").role(Role.CUSTOMER).enabled(true).build();

        when(creditScoreRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(creditScoreRepository.save(any())).thenAnswer(inv -> {
            CreditScore cs = inv.getArgument(0);
            cs.setId(1L);
            return cs;
        });

        CreditScore result = creditScoreService.getOrCreate(1L);
        assertNotNull(result);
        verify(creditScoreRepository).save(any());
    }

    @Test
    @DisplayName("updateFromRating adjusts score based on average stars")
    void updateFromRating_adjustsScore() {
        User user = User.builder().id(1L).username("u").email("u@t.com")
                .passwordHash("x").role(Role.CUSTOMER).enabled(true).build();
        CreditScore cs = CreditScore.builder()
                .id(1L).user(user).scoreEncrypted("500")
                .ratingImpact(0).communityImpact(0).orderImpact(0).disputeImpact(0)
                .build();

        when(creditScoreRepository.findByUserId(1L)).thenReturn(Optional.of(cs));
        when(creditScoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreditScore result = creditScoreService.updateFromRating(1L, 5);
        assertNotNull(result);
        verify(creditScoreRepository).save(any());
    }
}

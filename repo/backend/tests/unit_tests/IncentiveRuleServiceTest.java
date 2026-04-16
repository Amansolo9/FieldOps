package unit_tests;

import com.eaglepoint.storehub.entity.IncentiveRule;
import com.eaglepoint.storehub.repository.IncentiveRuleRepository;
import com.eaglepoint.storehub.service.IncentiveRuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncentiveRuleServiceTest {

    @Mock private IncentiveRuleRepository incentiveRuleRepository;

    @InjectMocks
    private IncentiveRuleService incentiveRuleService;

    @Test @DisplayName("getAllRules returns all rules")
    void getAllRules_returnsList() {
        when(incentiveRuleRepository.findAll()).thenReturn(List.of(
                IncentiveRule.builder().actionKey("POST_CREATED").points(5).active(true).build()));
        List<IncentiveRule> result = incentiveRuleService.getAllRules();
        assertEquals(1, result.size());
        assertEquals("POST_CREATED", result.get(0).getActionKey());
    }

    @Test @DisplayName("getPoints returns configured points for active rule")
    void getPoints_activeRule_returnsPoints() {
        when(incentiveRuleRepository.findByActionKey("POST_CREATED"))
                .thenReturn(Optional.of(IncentiveRule.builder()
                        .actionKey("POST_CREATED").points(5).active(true).build()));
        assertEquals(5, incentiveRuleService.getPoints("POST_CREATED"));
    }

    @Test @DisplayName("getPoints returns 0 for inactive rule")
    void getPoints_inactiveRule_returnsZero() {
        when(incentiveRuleRepository.findByActionKey("DISABLED_ACTION"))
                .thenReturn(Optional.of(IncentiveRule.builder()
                        .actionKey("DISABLED_ACTION").points(10).active(false).build()));
        assertEquals(0, incentiveRuleService.getPoints("DISABLED_ACTION"));
    }

    @Test @DisplayName("updateRule modifies point value")
    void updateRule_modifiesPoints() {
        IncentiveRule rule = IncentiveRule.builder()
                .actionKey("POST_CREATED").points(5).active(true).build();
        when(incentiveRuleRepository.findByActionKey("POST_CREATED")).thenReturn(Optional.of(rule));
        when(incentiveRuleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IncentiveRule result = incentiveRuleService.updateRule("POST_CREATED", 10);
        assertEquals(10, result.getPoints());
    }

    @Test @DisplayName("toggleRule changes active state")
    void toggleRule_changesActive() {
        IncentiveRule rule = IncentiveRule.builder()
                .actionKey("POST_CREATED").points(5).active(true).build();
        when(incentiveRuleRepository.findByActionKey("POST_CREATED")).thenReturn(Optional.of(rule));
        when(incentiveRuleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IncentiveRule result = incentiveRuleService.toggleRule("POST_CREATED", false);
        assertFalse(result.isActive());
    }
}

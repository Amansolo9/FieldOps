package unit_tests;

import com.eaglepoint.storehub.entity.DeliveryZoneGroup;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.repository.DeliveryZoneGroupRepository;
import com.eaglepoint.storehub.repository.OrganizationRepository;
import com.eaglepoint.storehub.service.DeliveryZoneGroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryZoneGroupServiceTest {

    @Mock private DeliveryZoneGroupRepository groupRepository;
    @Mock private OrganizationRepository organizationRepository;

    @InjectMocks
    private DeliveryZoneGroupService deliveryZoneGroupService;

    private DeliveryZoneGroup buildGroup(Long id) {
        return DeliveryZoneGroup.builder()
                .id(id).site(Organization.builder().id(10L).name("Site").build())
                .name("Metro").active(true)
                .zips(new ArrayList<>()).bands(new ArrayList<>())
                .build();
    }

    @Test @DisplayName("getBySite returns active groups")
    void getBySite_returnsActiveGroups() {
        when(groupRepository.findBySiteIdAndActiveTrue(10L)).thenReturn(List.of(buildGroup(1L)));
        List<DeliveryZoneGroup> result = deliveryZoneGroupService.getBySite(10L);
        assertEquals(1, result.size());
    }

    @Test @DisplayName("createGroup persists new group")
    void createGroup_persists() {
        Organization site = Organization.builder().id(10L).name("Site").build();
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(site));
        when(groupRepository.save(any())).thenAnswer(inv -> {
            DeliveryZoneGroup g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });

        DeliveryZoneGroup result = deliveryZoneGroupService.createGroup(10L, "Metro");
        assertNotNull(result);
        assertEquals("Metro", result.getName());
    }

    @Test @DisplayName("getById returns group")
    void getById_returnsGroup() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(buildGroup(1L)));
        DeliveryZoneGroup result = deliveryZoneGroupService.getById(1L);
        assertNotNull(result);
    }

    @Test @DisplayName("getById throws for missing group")
    void getById_missing_throws() {
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> deliveryZoneGroupService.getById(999L));
    }

    @Test @DisplayName("deactivate sets active to false")
    void deactivate_setsActiveFalse() {
        DeliveryZoneGroup group = buildGroup(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeliveryZoneGroup result = deliveryZoneGroupService.deactivate(1L);
        assertFalse(result.isActive());
    }
}

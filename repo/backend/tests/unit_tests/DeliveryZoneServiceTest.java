package unit_tests;

import com.eaglepoint.storehub.entity.DeliveryZone;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.repository.DeliveryZoneRepository;
import com.eaglepoint.storehub.repository.OrganizationRepository;
import com.eaglepoint.storehub.service.DeliveryZoneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryZoneServiceTest {

    @Mock private DeliveryZoneRepository deliveryZoneRepository;
    @Mock private OrganizationRepository organizationRepository;

    @InjectMocks
    private DeliveryZoneService deliveryZoneService;

    @Test @DisplayName("getBySite returns active zones")
    void getBySite_returnsActiveZones() {
        when(deliveryZoneRepository.findBySiteIdAndActiveTrue(1L)).thenReturn(List.of());
        List<DeliveryZone> result = deliveryZoneService.getBySite(1L);
        assertNotNull(result);
    }

    @Test @DisplayName("create persists new delivery zone")
    void create_persistsZone() {
        Organization site = Organization.builder().id(1L).name("S").build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(site));
        when(deliveryZoneRepository.save(any())).thenAnswer(inv -> {
            DeliveryZone z = inv.getArgument(0);
            z.setId(1L);
            return z;
        });

        DeliveryZone result = deliveryZoneService.create(1L, "90001", 3.0, new BigDecimal("4.99"));
        assertNotNull(result);
        assertEquals("90001", result.getZipCode());
    }

    @Test @DisplayName("create rejects invalid ZIP format")
    void create_invalidZip_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> deliveryZoneService.create(1L, "invalid", 3.0, new BigDecimal("4.99")));
    }

    @Test @DisplayName("create rejects negative distance")
    void create_negativeDistance_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> deliveryZoneService.create(1L, "90001", -1.0, new BigDecimal("4.99")));
    }

    @Test @DisplayName("update modifies existing zone")
    void update_modifiesZone() {
        DeliveryZone zone = DeliveryZone.builder().id(1L)
                .site(Organization.builder().id(1L).name("S").build())
                .zipCode("90001").distanceMiles(3.0).active(true).build();
        when(deliveryZoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(deliveryZoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeliveryZone result = deliveryZoneService.update(1L, "90002", 5.0, new BigDecimal("7.99"), true);
        assertEquals("90002", result.getZipCode());
        assertEquals(5.0, result.getDistanceMiles());
    }

    @Test @DisplayName("delete removes zone")
    void delete_removesZone() {
        DeliveryZone zone = DeliveryZone.builder().id(1L)
                .site(Organization.builder().id(1L).name("S").build()).build();
        when(deliveryZoneRepository.findById(1L)).thenReturn(Optional.of(zone));

        deliveryZoneService.delete(1L);
        verify(deliveryZoneRepository).delete(zone);
    }
}

package unit_tests;

import com.eaglepoint.storehub.entity.Order;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.enums.FulfillmentMode;
import com.eaglepoint.storehub.enums.OrderStatus;
import com.eaglepoint.storehub.enums.Role;
import com.eaglepoint.storehub.repository.OrderRepository;
import com.eaglepoint.storehub.service.ShippingLabelService;
import com.eaglepoint.storehub.service.SiteAuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingLabelServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private SiteAuthorizationService siteAuth;

    @InjectMocks
    private ShippingLabelService shippingLabelService;

    @Test @DisplayName("generateLabel returns PDF bytes for valid delivery order")
    void generateLabel_validOrder_returnsPdfBytes() {
        User customer = User.builder().id(1L).username("cust").email("c@t.com")
                .passwordHash("x").role(Role.CUSTOMER).enabled(true).build();
        Organization site = Organization.builder().id(10L).name("Site").build();
        Order order = Order.builder().id(1L).customer(customer).site(site)
                .status(OrderStatus.CONFIRMED).fulfillmentMode(FulfillmentMode.DELIVERY)
                .pickup(false).createdAt(Instant.now())
                .subtotal(new BigDecimal("20.00")).deliveryFee(new BigDecimal("4.99"))
                .total(new BigDecimal("24.99")).deliveryZip("90001").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        byte[] result = shippingLabelService.generateLabel(1L);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test @DisplayName("generateLabel throws for missing order")
    void generateLabel_missingOrder_throws() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> shippingLabelService.generateLabel(999L));
    }
}

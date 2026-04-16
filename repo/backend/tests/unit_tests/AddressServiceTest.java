package unit_tests;

import com.eaglepoint.storehub.dto.AddressDto;
import com.eaglepoint.storehub.entity.Address;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.enums.Role;
import com.eaglepoint.storehub.repository.AddressRepository;
import com.eaglepoint.storehub.repository.UserRepository;
import com.eaglepoint.storehub.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
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
class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("u").email("u@t.com")
                .passwordHash("x").role(Role.CUSTOMER).enabled(true).build();
    }

    @Test
    @DisplayName("create returns AddressDto with generated ID")
    void create_returnsDto() {
        AddressDto dto = new AddressDto();
        dto.setLabel("Home");
        dto.setStreet("123 Main");
        dto.setCity("Town");
        dto.setState("CA");
        dto.setZipCode("90001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any())).thenAnswer(inv -> {
            Address a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AddressDto result = addressService.create(1L, dto);
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Home", result.getLabel());
    }

    @Test
    @DisplayName("getByUser returns list of addresses")
    void getByUser_returnsList() {
        Address addr = Address.builder().id(1L).user(testUser)
                .label("Work").street("456 Oak").city("City").state("CA").zipCode("90002")
                .build();
        when(addressRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(addr));

        List<AddressDto> result = addressService.getByUser(1L);
        assertEquals(1, result.size());
        assertEquals("Work", result.get(0).getLabel());
    }

    @Test
    @DisplayName("delete removes address owned by user")
    void delete_success() {
        Address addr = Address.builder().id(1L).user(testUser).build();
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(addr));

        addressService.delete(1L, 1L);
        verify(addressRepository).delete(addr);
    }

    @Test
    @DisplayName("delete throws when address belongs to another user")
    void delete_wrongUser_throws() {
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> addressService.delete(1L, 1L));
    }

    @Test
    @DisplayName("update modifies address fields")
    void update_modifiesFields() {
        Address addr = Address.builder().id(1L).user(testUser)
                .label("Old").street("Old St").city("Old City").state("CA").zipCode("90001")
                .build();
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(addr));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AddressDto dto = new AddressDto();
        dto.setLabel("Updated");
        dto.setStreet("New St");
        dto.setCity("New City");
        dto.setState("NY");
        dto.setZipCode("10001");

        AddressDto result = addressService.update(1L, 1L, dto);
        assertEquals("Updated", result.getLabel());
        assertEquals("New St", result.getStreet());
    }
}

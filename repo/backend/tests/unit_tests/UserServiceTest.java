package unit_tests;

import com.eaglepoint.storehub.dto.UserDto;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.enums.Role;
import com.eaglepoint.storehub.repository.UserRepository;
import com.eaglepoint.storehub.service.UserService;
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
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private com.eaglepoint.storehub.service.SiteAuthorizationService siteAuth;

    @InjectMocks
    private UserService userService;

    private User buildUser(Long id, String username, Role role) {
        return User.builder().id(id).username(username).email(username + "@t.com")
                .passwordHash("x").role(role).enabled(true)
                .site(Organization.builder().id(10L).name("Site").build())
                .build();
    }

    @Test
    @DisplayName("findAllUsers returns list of UserDtos")
    void findAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildUser(1L, "alice", Role.STAFF),
                buildUser(2L, "bob", Role.CUSTOMER)));

        List<UserDto> result = userService.findAllUsers();
        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getUsername());
    }

    @Test
    @DisplayName("findById returns UserDto for existing user")
    void findById_existingUser_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "alice", Role.STAFF)));

        UserDto result = userService.findById(1L);
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
    }

    @Test
    @DisplayName("findById throws for missing user")
    void findById_missingUser_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userService.findById(999L));
    }

    @Test
    @DisplayName("updateRole changes user role")
    void updateRole_changesRole() {
        User user = buildUser(1L, "alice", Role.CUSTOMER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.updateRole(1L, Role.SITE_MANAGER);
        assertEquals(Role.SITE_MANAGER, result.getRole());
    }

    @Test
    @DisplayName("disableUser sets enabled to false")
    void disableUser_setsEnabledFalse() {
        User user = buildUser(1L, "alice", Role.STAFF);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.disableUser(1L);
        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
    }
}

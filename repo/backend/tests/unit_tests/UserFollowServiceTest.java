package unit_tests;

import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.entity.UserFollow;
import com.eaglepoint.storehub.repository.UserFollowRepository;
import com.eaglepoint.storehub.repository.UserRepository;
import com.eaglepoint.storehub.service.UserFollowService;
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
class UserFollowServiceTest {

    @Mock private UserFollowRepository userFollowRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserFollowService userFollowService;

    @Test
    @DisplayName("follow creates new follow relationship")
    void follow_createsRelationship() {
        User follower = User.builder().id(1L).username("a").email("a@t.com")
                .passwordHash("x").role(com.eaglepoint.storehub.enums.Role.CUSTOMER).enabled(true).build();
        User following = User.builder().id(2L).username("b").email("b@t.com")
                .passwordHash("x").role(com.eaglepoint.storehub.enums.Role.CUSTOMER).enabled(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(userFollowRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.empty());
        when(userFollowRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userFollowService.follow(1L, 2L);
        verify(userFollowRepository).save(any(UserFollow.class));
    }

    @Test
    @DisplayName("follow self throws exception")
    void follow_self_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> userFollowService.follow(1L, 1L));
    }

    @Test
    @DisplayName("unfollow removes follow relationship")
    void unfollow_removesRelationship() {
        UserFollow existing = UserFollow.builder().id(1L).follower(User.builder().id(1L).build()).following(User.builder().id(2L).build()).build();
        when(userFollowRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.of(existing));

        userFollowService.unfollow(1L, 2L);
        verify(userFollowRepository).delete(existing);
    }

    @Test
    @DisplayName("getFollowing returns list of followed user IDs")
    void getFollowing_returnsList() {
        when(userFollowRepository.findFollowingIdsByFollowerId(1L))
                .thenReturn(List.of(2L, 3L));

        List<Long> following = userFollowService.getFollowing(1L);
        assertEquals(List.of(2L, 3L), following);
    }

    @Test
    @DisplayName("isFollowing returns true when follow exists")
    void isFollowing_true() {
        when(userFollowRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.of(UserFollow.builder().build()));

        assertTrue(userFollowService.isFollowing(1L, 2L));
    }

    @Test
    @DisplayName("isFollowing returns false when follow does not exist")
    void isFollowing_false() {
        when(userFollowRepository.findByFollowerIdAndFollowingId(1L, 99L))
                .thenReturn(Optional.empty());

        assertFalse(userFollowService.isFollowing(1L, 99L));
    }
}

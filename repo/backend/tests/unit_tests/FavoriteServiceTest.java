package unit_tests;

import com.eaglepoint.storehub.entity.Favorite;
import com.eaglepoint.storehub.entity.Post;
import com.eaglepoint.storehub.entity.User;
import com.eaglepoint.storehub.repository.FavoriteRepository;
import com.eaglepoint.storehub.service.FavoriteService;
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
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private com.eaglepoint.storehub.repository.UserRepository userRepository;
    @Mock private com.eaglepoint.storehub.repository.PostRepository postRepository;
    @Mock private com.eaglepoint.storehub.service.SiteAuthorizationService siteAuth;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    @DisplayName("toggleFavorite adds favorite when not present")
    void toggleFavorite_adds() {
        when(favoriteRepository.findByUserIdAndPostId(1L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(postRepository.findById(10L)).thenReturn(Optional.of(Post.builder().id(10L).build()));
        when(favoriteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = favoriteService.toggleFavorite(1L, 10L);
        assertTrue(result);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("toggleFavorite removes favorite when already present")
    void toggleFavorite_removes() {
        Favorite existing = Favorite.builder().id(1L).user(User.builder().id(1L).build()).post(Post.builder().id(10L).build()).build();
        when(favoriteRepository.findByUserIdAndPostId(1L, 10L)).thenReturn(Optional.of(existing));

        boolean result = favoriteService.toggleFavorite(1L, 10L);
        assertFalse(result);
        verify(favoriteRepository).delete(existing);
    }

    @Test
    @DisplayName("getFavorites returns list of post IDs")
    void getFavorites_returnsList() {
        Favorite f1 = Favorite.builder().id(1L).user(User.builder().id(1L).build()).post(Post.builder().id(10L).build()).build();
        Favorite f2 = Favorite.builder().id(2L).user(User.builder().id(1L).build()).post(Post.builder().id(20L).build()).build();
        Favorite f3 = Favorite.builder().id(3L).user(User.builder().id(1L).build()).post(Post.builder().id(30L).build()).build();
        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of(f1, f2, f3));

        List<Long> result = favoriteService.getFavorites(1L);
        assertEquals(List.of(10L, 20L, 30L), result);
    }
}

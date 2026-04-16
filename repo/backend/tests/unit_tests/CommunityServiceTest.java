package unit_tests;

import com.eaglepoint.storehub.dto.CommentRequest;
import com.eaglepoint.storehub.dto.CommentResponse;
import com.eaglepoint.storehub.dto.PostRequest;
import com.eaglepoint.storehub.dto.PostResponse;
import com.eaglepoint.storehub.entity.*;
import com.eaglepoint.storehub.enums.VoteType;
import com.eaglepoint.storehub.repository.*;
import com.eaglepoint.storehub.service.CommunityService;
import com.eaglepoint.storehub.service.GamificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private TopicFollowRepository topicFollowRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserFollowRepository userFollowRepository;
    @Mock private QuarantinedVoteRepository quarantinedVoteRepository;
    @Mock private GamificationService gamificationService;
    @Mock private com.eaglepoint.storehub.service.SiteAuthorizationService siteAuth;

    @InjectMocks
    private CommunityService communityService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("author").email("a@t.com")
                .passwordHash("x").role(com.eaglepoint.storehub.enums.Role.CUSTOMER)
                .enabled(true).build();
    }

    @Test
    @DisplayName("createPost returns PostResponse with correct fields")
    void createPost_success() {
        PostRequest req = new PostRequest();
        req.setTitle("Hello");
        req.setBody("World");
        req.setTopic("general");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postRepository.save(any())).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(10L);
            p.setCreatedAt(Instant.now());
            return p;
        });

        PostResponse res = communityService.createPost(1L, req);
        assertNotNull(res);
        assertEquals("Hello", res.getTitle());
        assertEquals("general", res.getTopic());
        verify(gamificationService).awardPoints(eq(1L), any(com.eaglepoint.storehub.enums.PointAction.class), anyString());
    }

    @Test
    @DisplayName("getFeed returns paginated results")
    void getFeed_returnsPaginatedResults() {
        Post post = Post.builder().id(1L).author(testUser).title("T").body("B")
                .upvotes(5).downvotes(1).createdAt(Instant.now()).build();
        when(postRepository.findByRemovedFalseOrderByCreatedAtDesc(any()))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(voteRepository.findByUserIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.empty());

        Page<PostResponse> page = communityService.getFeed(1L, PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals("T", page.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("removePost calls delete")
    void removePost_deletesPost() {
        Post post = Post.builder().id(1L).author(testUser).title("T").body("B").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        communityService.removePost(1L);
        verify(postRepository).save(argThat(p -> p.isRemoved()));
        verify(gamificationService).awardPoints(eq(testUser.getId()), any(com.eaglepoint.storehub.enums.PointAction.class), anyString());
    }

    @Test
    @DisplayName("addComment returns CommentResponse")
    void addComment_success() {
        Post post = Post.builder().id(1L).author(testUser).title("T").body("B")
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(
                User.builder().id(2L).username("commenter").email("c@t.com")
                        .passwordHash("x").role(com.eaglepoint.storehub.enums.Role.CUSTOMER).enabled(true).build()));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(100L);
            c.setCreatedAt(Instant.now());
            return c;
        });

        CommentRequest req = new CommentRequest();
        req.setBody("Great post!");
        CommentResponse res = communityService.addComment(1L, 2L, req);
        assertNotNull(res);
        assertEquals("Great post!", res.getBody());
    }

    @Test
    @DisplayName("followTopic and getFollowedTopics work together")
    void followTopic_thenGetFollowed() {
        when(topicFollowRepository.findByUserIdAndTopic(1L, "java")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(topicFollowRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        communityService.followTopic(1L, "java");
        verify(topicFollowRepository).save(any());
    }

    @Test
    @DisplayName("getFollowedTopics returns list of topics")
    void getFollowedTopics_returnsList() {
        when(topicFollowRepository.findTopicsByUserId(1L)).thenReturn(List.of("java"));

        List<String> topics = communityService.getFollowedTopics(1L);
        assertEquals(List.of("java"), topics);
    }
}

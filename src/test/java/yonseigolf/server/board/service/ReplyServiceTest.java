package yonseigolf.server.board.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yonseigolf.server.board.dto.request.PostReplyRequest;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Category;
import yonseigolf.server.board.entity.Reply;
import yonseigolf.server.board.repository.BoardRepository;
import yonseigolf.server.board.repository.ReplyRepository;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ReplyServiceTest {

    @Autowired
    private ReplyService replyService;
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        replyRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Test
    @DisplayName("댓글 생성 테스트")
    void postReplyTest() {
        // given
        User user = User.createUserForForeignKey(1L);

        User savedUser = userRepository.save(user);
        Board board = Board.builder()
                .writer(savedUser)
                .category(Category.FREE)
                .title("test")
                .content("test")
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        Board savedBoard = boardRepository.save(board);

        PostReplyRequest replyRequest = PostReplyRequest.builder()
                .content("test")
                .build();

        // when
        replyService.postReply(savedUser.getId(), savedBoard.getId(), replyRequest);

        List<Reply> all = replyRepository.findAll();

        // then
        assertAll(
                () -> assertThat(all.size()).isEqualTo(1),
                () -> assertThat(all.get(0).getContent()).isEqualTo("test")
        );
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteReplyTest() {
        // given
        User user = User.createUserForForeignKey(1L);
        User savedUser = userRepository.save(user);
        Board board = Board.builder()
                .writer(savedUser)
                .category(Category.FREE)
                .title("test")
                .content("test")
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
        Reply reply = Reply.builder()
                .content("test")
                .createdAt(LocalDateTime.now())
                .board(board)
                .user(savedUser)
                .build();
        boardRepository.save(board);

        Reply savedReply = replyRepository.save(reply);

        // when
        replyService.deleteReply(savedReply.getId());

        // then
        assertThat(replyRepository.findAll().size()).isEqualTo(0);
    }
}

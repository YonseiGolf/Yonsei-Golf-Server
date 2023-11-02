package yonseigolf.server.board.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Image;
import yonseigolf.server.board.entity.Reply;
import yonseigolf.server.board.repository.BoardRepository;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;
import yonseigolf.server.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@SpringBootTest
class BoardServiceTest {

    @Autowired
    private BoardService boardService;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUpd() {
        boardRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자는 모든 게시글을 조회할 수 있다.")
    void findAllBoardTest() {
        // given
        User savedUser = userRepository.save(createUser());
        Board board = createBoard(savedUser);
        Board savedBoard = boardRepository.save(board);

        // when
        Page<SingleBoardResponse> boards = boardService.findAllBoard(PageRequest.of(0, 10));
        List<SingleBoardResponse> content = boards.getContent();

        // then
        assertAll(
                () -> assertThat(content).hasSize(1),
                () -> assertThat(content.get(0)).isEqualTo(SingleBoardResponse.fromBoard(savedBoard))
        );
    }

    private Board createBoard(User user) {
        return Board.builder()
                .title("title")
                .content("content")
                .writer(user)
                .deleted(false)
                .build();
    }

    private User createUser() {

        return User.builder()
                .kakaoId(1L)
                .name("name")
                .phoneNumber("010-1234-5678")
                .studentId(1)
                .major("major")
                .semester(1)
                .role(UserRole.MEMBER)
                .userClass(UserClass.OB)
                .build();
    }
    private Image createImage(Board board) {

        return Image.builder()
                .url("url")
                .board(board)
                .build();
    }

    private Reply createReply(Board board, User user) {

        return Reply.builder()
                .content("content")
                .createdAt(LocalDateTime.now())
                .board(board)
                .user(user)
                .build();
    }
}

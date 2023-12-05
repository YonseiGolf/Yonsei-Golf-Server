package yonseigolf.server.board.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.request.UpdateBoardRequest;
import yonseigolf.server.board.dto.response.BoardDetailResponse;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Category;
import yonseigolf.server.board.entity.Image;
import yonseigolf.server.board.entity.Reply;
import yonseigolf.server.board.exception.BoardNotFoundException;
import yonseigolf.server.board.exception.DeletedBoardException;
import yonseigolf.server.board.repository.BoardRepository;
import yonseigolf.server.board.repository.ReplyRepository;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;
import yonseigolf.server.user.repository.UserRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;


@SpringBootTest
class BoardServiceTest {

    @Autowired
    private BoardService boardService;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private EntityManager em;

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
        Page<SingleBoardResponse> boards = boardService.findAllBoard(PageRequest.of(0, 10), null);
        List<SingleBoardResponse> content = boards.getContent();

        // then
        assertAll(
                () -> assertThat(content).hasSize(1),
                () -> assertThat(content.get(0)).isEqualTo(SingleBoardResponse.fromBoard(savedBoard))
        );
    }

    @Test
    @DisplayName("사용자는 게시글을 등록할 수 있다.")
    void postBoardTest() {
        // given
        User savedUser = userRepository.save(createUser());

        CreateBoardRequest request = CreateBoardRequest.builder()
                .category(Category.NOTICE)
                .title("title")
                .content("content")
                .build();

        // when
        boardService.postBoard(request, savedUser.getId());

        List<Board> all = boardRepository.findAll();
        Board board = all.get(0);

        // then
        assertAll(
                () -> assertThat(all).hasSize(1),
                () -> assertThat(board.getWriter().getId()).isEqualTo(savedUser.getId()),
                () -> assertThat(board.getTitle()).isEqualTo("title"),
                () -> assertThat(board.getContent()).isEqualTo("content"),
                () -> assertThat(board.getCategory()).isEqualTo(Category.NOTICE),
                () -> assertThat(board.isDeleted()).isFalse()
        );

    }

    @Test
    @DisplayName("게시글 업데이트 테스트")
    void boardUpdateTest() {
        // given
        User savedUser = userRepository.save(createUser());
        Board board = createBoard(savedUser);
        Board savedBoard = boardRepository.save(board);

        UpdateBoardRequest update = UpdateBoardRequest.builder()
                .category(Category.NOTICE)
                .title("title update")
                .content("content update")
                .build();

        // when
        boardService.updateBoard(savedBoard.getId(), update, savedUser.getId());

        Board updatedBoard = boardRepository.findById(savedBoard.getId())
                .orElseGet(() -> Board.builder().build());

        // then
        assertAll(
                () -> assertThat(updatedBoard.getTitle()).isEqualTo(update.getTitle()),
                () -> assertThat(updatedBoard.getContent()).isEqualTo(update.getContent()),
                () -> assertThat(updatedBoard.getCategory()).isEqualTo(update.getCategory())
        );
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 업데이트하면 예외가 발생한다.")
    void notExistErrorTest() {
        // given
        Long notExistId = 1L;
        UpdateBoardRequest request = UpdateBoardRequest.builder().build();

        // when & then
        assertThatThrownBy(() -> boardService.updateBoard(notExistId, request, 1L))
                .isInstanceOf(BoardNotFoundException.class)
                .hasMessage("해당 게시글이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deleteBoardTest() {
        // given
        User savedUser = userRepository.save(createUser());
        Board board = createBoard(savedUser);
        Board savedBoard = boardRepository.save(board);

        // when
        boardService.deleteBoard(savedBoard.getId(), savedUser.getId());

        Board deletedBoard = boardRepository.findById(savedBoard.getId())
                .orElseGet(() -> Board.builder().build());

        // then
        assertThat(deletedBoard.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("게시글 작성자와 삭제하는 사람이 다르면 예외가 발생한다.")
    void deleteErrorTest() {
        // given
        User savedUser = userRepository.save(createUser());
        Board board = createBoard(savedUser);
        Board savedBoard = boardRepository.save(board);

        // when & then
        assertThatThrownBy(() -> boardService.deleteBoard(savedBoard.getId(), 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자만 게시글을 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("게시글이 삭제되었는데 삭제하려고 하면 DeltedBoardException이 발생한다.")
    void deleteExceptionTest() {
        // given
        User user = userRepository.save(createUser());
        Board board = createBoard(user);
        Board saved = boardRepository.save(board);

        boardService.deleteBoard(saved.getId(), user.getId());

        // when & then
        assertThatThrownBy(() -> boardService.deleteBoard(saved.getId(), user.getId()))
                .isInstanceOf(DeletedBoardException.class)
                .hasMessage("이미 삭제된 게시글 입니다.");
    }

    @Test
    @DisplayName("게시글이 삭제되었는데 수정하려고 하면 DeltedBoardException이 발생한다.")
    void updateExceptionTest() {
        // given
        User user = userRepository.save(createUser());
        Board board = createBoard(user);
        Board saved = boardRepository.save(board);

        boardService.deleteBoard(saved.getId(), user.getId());


        // when & then
        assertThatThrownBy(() -> boardService.updateBoard(saved.getId(), UpdateBoardRequest.builder().build(), user.getId()))
                .isInstanceOf(DeletedBoardException.class)
                .hasMessage("이미 삭제된 게시글 입니다.");
    }

    @Test
    @DisplayName("게시글 작성자와 수정하려는 사람이 다를 경우 예외가 발생한다.")
    void updateBoardTest() {
        // given
        User user = userRepository.save(createUser());
        Board board = createBoard(user);
        Board saved = boardRepository.save(board);

        // then
        assertThatThrownBy(() -> boardService.updateBoard(saved.getId(), UpdateBoardRequest.builder().build(), 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자와 수정하려는 사람이 다릅니다.");
    }

    @Test
    @Transactional
    @DisplayName("게시글 상세 조회 테스트")
    void findBoardDetailTest() {
        // given
        User user = createUser();
        userRepository.save(user);

        Board board = createBoard(user);
        Board savedBoard = boardRepository.save(board);

        Reply reply = createReply(board, user);
        replyRepository.save(reply);
        em.flush();

        // when
        BoardDetailResponse boardDetail = boardService.findBoardDetail(board.getId());

        // then
        assertAll(
                () -> assertThat(boardDetail.getId()).isEqualTo(savedBoard.getId()),
                () -> assertThat(boardDetail.getTitle()).isEqualTo(savedBoard.getTitle()),
                () -> assertThat(boardDetail.getContent()).isEqualTo(savedBoard.getContent()),
                () -> assertThat(boardDetail.getCategory()).isEqualTo(savedBoard.getCategory()),
                () -> assertThat(boardDetail.getCreatedAt()).isEqualTo(savedBoard.getCreatedAt()),
                () -> assertThat(boardDetail.getReplies().getReplies()).hasSize(1)
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

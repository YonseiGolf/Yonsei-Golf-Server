package yonseigolf.server.board.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.repository.BoardRepository;
import yonseigolf.server.user.dto.response.SessionUser;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BoardIntegrateControllerTest {

    private MockMvc mockMvc;
    @Autowired
    private BoardController boardController;
    @Autowired
    private BoardExceptionController boardExceptionController;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(boardController)
                .setControllerAdvice(boardExceptionController)
                .build();
    }

    @Test
    @DisplayName("게시글이 존재하지 않는 경우 BoardNotFoundException 이 발생한다.")
    void boardNotFoundTest() throws Exception {
        // given
        long notExistBoardId = 1L;

        // then
        mockMvc.perform(get("/boards/" + notExistBoardId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글이 이미 삭제된 경우에는 DeletedBoardExcpetion이 발생한다.")
    void deletedBoardTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        SessionUser sessionUser = SessionUser.builder()
                .id(1L)
                .name("test")
                .build();
        session.setAttribute("user", sessionUser);
        User user = User.builder()
                .id(1L)
                .build();
        userRepository.save(user);
        Board board = Board.builder()
                .deleted(true)
                .writer(user)
                .build();
        Board saved = boardRepository.save(board);


        // when & then
        mockMvc.perform(delete("/boards/" + saved.getId())
                        .session(session))
                .andExpect(status().isNotFound());
    }
}

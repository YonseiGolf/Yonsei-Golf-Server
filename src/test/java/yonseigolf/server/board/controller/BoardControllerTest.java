package yonseigolf.server.board.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.restdocs.payload.JsonFieldType;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Category;
import yonseigolf.server.board.entity.Image;
import yonseigolf.server.board.entity.Reply;
import yonseigolf.server.board.service.BoardImageService;
import yonseigolf.server.board.service.BoardService;
import yonseigolf.server.board.service.ReplyService;
import yonseigolf.server.docs.utils.RestDocsSupport;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentRequest;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentResponse;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest extends RestDocsSupport {

    @Mock
    private BoardService boardService;
    @Mock
    private BoardImageService boardImageService;
    @Mock
    private ReplyService replyService;

    @Test
    @DisplayName("사용자는 전체 게시글 목록을 조회할 수 있다.")
    void findAllBoardTest() throws Exception {
        // given
        User savedUser = createUser();
        Board board = createBoard(savedUser);

        List<SingleBoardResponse> mockResults = List.of(SingleBoardResponse.fromBoard(board));
        Page<SingleBoardResponse> mockPage = new PageImpl<>(mockResults);

        given(boardService.findAllBoard(any())).willReturn(mockPage);


        // when & then
        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("board-findAll-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content[].id").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("content[].category").type(JsonFieldType.STRING)
                                        .description("게시글 카테고리"),
                                fieldWithPath("content[].title").type(JsonFieldType.STRING)
                                        .description("게시글 제목"),
                                fieldWithPath("content[].writer").type(JsonFieldType.STRING)
                                        .description("게시글 작성자"),
                                fieldWithPath("content[].date").type(JsonFieldType.STRING)
                                        .description("게시글 작성일"),
                                fieldWithPath("pageable").ignored(),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                        .description("마지막 페이지 여부"),
                                fieldWithPath("totalPages").type(JsonFieldType.NUMBER)
                                        .description("총 페이지 수"),
                                fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
                                        .description("총 요소 수"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("number").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 번호"),
                                fieldWithPath("sort.empty").type(JsonFieldType.BOOLEAN)
                                        .description("정렬 정보가 없는지 여부"),
                                fieldWithPath("sort.sorted").type(JsonFieldType.BOOLEAN)
                                        .description("정렬됐는지 여부"),
                                fieldWithPath("sort.unsorted").type(JsonFieldType.BOOLEAN)
                                        .description("미정렬됐는지 여부"),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                        .description("첫 페이지인지 여부"),
                                fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지의 요소 수"),
                                fieldWithPath("empty").type(JsonFieldType.BOOLEAN)
                                        .description("페이지가 비어있는지 여부")
                        )));
    }

    private Board createBoard(User user) {
        return Board.builder()
                .id(1L)
                .category(Category.NOTICE)
                .title("title")
                .content("content")
                .writer(user)
                .date(LocalDateTime.now())
                .build();
    }

    private User createUser() {

        return User.builder()
                .id(1L)
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
                .id(1L)
                .url("url")
                .board(board)
                .build();
    }

    private Reply createReply(Board board, User user) {

        return Reply.builder()
                .id(1L)
                .content("content")
                .date(LocalDateTime.now())
                .board(board)
                .user(user)
                .build();
    }

    @Override
    protected Object initController() {
        return new BoardController(boardService, boardImageService, replyService);
    }
}
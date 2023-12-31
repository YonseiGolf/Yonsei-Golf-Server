package yonseigolf.server.board.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import yonseigolf.server.TestInterceptor;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.request.CreateBoardTemplateRequest;
import yonseigolf.server.board.dto.request.PostReplyRequest;
import yonseigolf.server.board.dto.request.UpdateBoardRequest;
import yonseigolf.server.board.dto.response.*;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Category;
import yonseigolf.server.board.entity.Image;
import yonseigolf.server.board.entity.Reply;
import yonseigolf.server.board.service.BoardImageService;
import yonseigolf.server.board.service.BoardService;
import yonseigolf.server.board.service.BoardTemplateService;
import yonseigolf.server.board.service.ReplyService;
import yonseigolf.server.docs.utils.RestDocsSupport;
import yonseigolf.server.user.dto.response.LoggedInUser;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentRequest;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentResponse;

@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
class BoardControllerTest extends RestDocsSupport {

    @Mock
    private BoardService boardService;
    @Mock
    private BoardImageService boardImageService;
    @Mock
    private ReplyService replyService;
    @Mock
    private BoardTemplateService boardTemplateService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(initController())
                .addInterceptors(new TestInterceptor()) // TestInterceptor 추가
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .setControllerAdvice(new BoardExceptionController())
                .build();
    }

    @Test
    @DisplayName("사용자는 전체 게시글 목록을 조회할 수 있다.")
    void findAllBoardTest() throws Exception {
        // given
        User savedUser = createUser();
        Board board = createBoard(savedUser);

        List<SingleBoardResponse> mockResults = List.of(SingleBoardResponse.fromBoard(board));
        Page<SingleBoardResponse> mockPage = new PageImpl<>(mockResults);

        given(boardService.findAllBoard(any(), any())).willReturn(mockPage);


        // when & then
        mockMvc.perform(
                        get("/boards")
                                .param("page", "0")
                                .param("size", "10")
                )
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
                                fieldWithPath("content[].createdAt").type(JsonFieldType.STRING)
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

    @Test
    @DisplayName("사용자는 게시글을 작성할 수 있다.")
    void createBoardTest() throws Exception {
        // given
        CreateBoardRequest request = CreateBoardRequest.builder()
                .category(Category.NOTICE)
                .title("title")
                .content("content")
                .build();

        // when & then
        mockMvc.perform(
                        post("/boards")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType("application/json")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-create-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("category").type(JsonFieldType.STRING)
                                        .description("게시글 카테고리"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("게시글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("게시글 내용")
                        )));
    }

    @Test
    @DisplayName("사용자는 게시글을 수정할 수 있다.")
    void updateBoardTest() throws Exception {
        // given
        MockHttpSession httpSession = new MockHttpSession();
        LoggedInUser user = LoggedInUser.builder()
                .id(1L)
                .name("name")
                .build();
        httpSession.setAttribute("user", user);
        Long boardId = 1L;
        UpdateBoardRequest request = UpdateBoardRequest.builder()
                .category(Category.NOTICE)
                .title("title")
                .content("content")
                .build();

        // when & then
        mockMvc.perform(
                        patch("/boards/{boardId}", boardId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType("application/json")
                                .session(httpSession)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-update-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("boardId").description("게시글 ID")),
                        requestFields(
                                fieldWithPath("category").type(JsonFieldType.STRING)
                                        .description("게시글 카테고리"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("게시글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("게시글 내용")
                        )));

    }

    @Test
    @DisplayName("게시글을 삭제할 수 있다.")
    void deleteBoardTest() throws Exception {
        // given
        MockHttpSession httpSession = new MockHttpSession();
        LoggedInUser user = LoggedInUser.builder()
                .id(1L)
                .name("name")
                .build();
        httpSession.setAttribute("user", user);

        Long boardId = 1L;

        // when & then
        mockMvc.perform(
                        delete("/boards/{boardId}", boardId)
                                .session(httpSession)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-delete-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("boardId").description("게시글 ID"))
                ));
    }

    @Test
    @DisplayName("게시글을 상세조회할 수 있다.")
    void findBoardDetailTest() throws Exception {
        // given
        Long boardId = 1L;
        BoardDetailResponse response = BoardDetailResponse.builder()
                .id(1L)
                .writerId(1L)
                .writer("writer")
                .category(Category.NOTICE)
                .title("title")
                .content("content")
                .createdAt(LocalDateTime.now())
                .replies(
                        AllReplyResponse.fromReplies(
                                List.of(
                                        SingleReplyResponse.builder()
                                                .id(1L)
                                                .writerId(1L)
                                                .writer("reply writer")
                                                .content("reply content")
                                                .createdAt(LocalDateTime.now())
                                                .build()
                                )
                        )
                ).build();
        given(boardService.findBoardDetail(any())).willReturn(response);


        // when & then
        mockMvc.perform(
                        get("/boards/{boardId}", boardId)
                ).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-findDetail-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("boardId").description("게시글 ID")),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("writerId").type(JsonFieldType.NUMBER)
                                        .description("게시글 작성자 ID"),
                                fieldWithPath("writer").type(JsonFieldType.STRING)
                                        .description("게시글 작성자"),
                                fieldWithPath("category").type(JsonFieldType.STRING)
                                        .description("게시글 카테고리"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("게시글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("게시글 내용"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING)
                                        .description("게시글 작성일"),
                                fieldWithPath("replies").type(JsonFieldType.OBJECT)
                                        .description("게시글 댓글"),
                                fieldWithPath("replies.replies[].id").type(JsonFieldType.NUMBER)
                                        .description("댓글 ID"),
                                fieldWithPath("replies.replies[].writerId").type(JsonFieldType.NUMBER)
                                        .description("댓글 작성자 ID"),
                                fieldWithPath("replies.replies[].writer").type(JsonFieldType.STRING)
                                        .description("댓글 작성자"),
                                fieldWithPath("replies.replies[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("replies.replies[].createdAt").type(JsonFieldType.STRING)
                                        .description("댓글 작성일")
                        )));

    }

    @Test
    @DisplayName("사용자는 댓글을 작성할 수 있다.")
    void createReplyTest() throws Exception {
        // given
        PostReplyRequest replyRequest = PostReplyRequest.builder()
                .content("content")
                .build();
        long boardId = 1L;

        // when & then
        mockMvc.perform(
                        post("/boards/{boardId}/replies", boardId
                        )
                                .content(objectMapper.writeValueAsString(replyRequest))
                                .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("board-createReply-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("boardId").description("게시글 ID")),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("댓글 내용")
                        )));
    }

    @Test
    @DisplayName("사용자는 댓글을 삭제할 수 있다.")
    void deleteReplyTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        LoggedInUser user = LoggedInUser.builder()
                .id(1L)
                .name("name")
                .build();
        session.setAttribute("user", user);

        long replyId = 1L;

        // when & then
        mockMvc.perform(
                        delete("/replies/{replyId}", replyId)
                                .session(session)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-deleteReply-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("replyId").description("댓글 ID")
                        )));
    }

    @Test
    @DisplayName("사용자는 게시글 템플릿 목록을 조회할 수 있다.")
    void findAllTemplatesTest() throws Exception {
        // given
        AllBoardTemplatesResponse response = AllBoardTemplatesResponse.builder()
                .templates(
                        List.of(BoardTemplatesResponse.builder()
                                .id(1L)
                                .title("title")
                                .build()
                        )
                ).build();

        given(boardTemplateService.findBoardTemplates()).willReturn(response);

        // then
        mockMvc.perform(
                        get("/admin/boards/templates")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-findAllTemplates-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("templates[].id").type(JsonFieldType.NUMBER)
                                        .description("게시글 템플릿 ID"),
                                fieldWithPath("templates[].title").type(JsonFieldType.STRING)
                                        .description("게시글 템플릿 제목")
                        )));
    }

    @Test
    @DisplayName("사용자는 게시글 상세 목록을 조회할 수 있다.")
    void findTemplateDetailTest() throws Exception {
        // given
        SingleBoardTemplateResponse response = SingleBoardTemplateResponse.builder()
                .id(1L)
                .title("title")
                .contents("content")
                .build();

        // when
        given(boardTemplateService.findBoardTemplate(anyLong())).willReturn(response);

        // then
        mockMvc.perform(
                        get("/admin/boards/templates/{templateId}", 1L)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-findTemplateDetail-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("templateId").description("게시글 템플릿 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("게시글 템플릿 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("게시글 템플릿 제목"),
                                fieldWithPath("contents").type(JsonFieldType.STRING)
                                        .description("게시글 템플릿 내용")
                        )));
    }

    @Test
    @DisplayName("사용자는 게시글 템플릿을 등록할 수 있다.")
    void postBoardTemplateTest() throws Exception {
        // given
        doNothing().when(boardTemplateService).createBoardTemplate(any());

        // when & then
        mockMvc.perform(
                        post("/admin/boards/templates")
                                .content(objectMapper.writeValueAsString(CreateBoardTemplateRequest.builder()
                                        .title("title")
                                        .contents("contents")
                                        .build()))
                                .contentType("application/json")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-postBoardTemplate-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("게시글 템플릿 제목"),
                                fieldWithPath("contents").type(JsonFieldType.STRING)
                                        .description("게시글 템플릿 내용")
                        )));
    }

    @Test
    @DisplayName("사용자는 게시글 템플릿을 수정할 수 있다.")
    void updateBoardTemplateTest() throws Exception {
        // given
        Long templateId = 1L;
        CreateBoardTemplateRequest request = CreateBoardTemplateRequest.builder()
                .title("title")
                .contents("contents")
                .build();

        // when
        doNothing().when(boardTemplateService).updateBoardTemplate(anyLong(), any());

        // then
        mockMvc.perform(
                        patch("/admin/boards/templates/{templateId}", templateId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType("application/json")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-updateBoardTemplate-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("templateId").description("게시글 템플릿 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("게시글 템플릿 제목"),
                                fieldWithPath("contents").type(JsonFieldType.STRING)
                                        .description("게시글 템플릿 내용")
                        )));
    }

    @Test
    @DisplayName("사용자는 게시글 템플릿을 삭제할 수 있다.")
    void deleteBoardTemplateTest() throws Exception {
        // given
        long boardId = 1L;
        doNothing().when(boardTemplateService).deleteBoardTemplate(anyLong());

        // when & then
        mockMvc.perform(
                        delete("/admin/boards/templates/{templateId}", boardId)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("board-deleteBoardTemplate-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("templateId").description("게시글 템플릿 ID")
                        )));
    }

    private Board createBoard(User user) {
        return Board.builder()
                .id(1L)
                .category(Category.NOTICE)
                .title("title")
                .content("content")
                .writer(user)
                .createdAt(LocalDateTime.now())
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
                .createdAt(LocalDateTime.now())
                .board(board)
                .user(user)
                .build();
    }

    @Override
    protected Object initController() {
        return new BoardController(boardService, boardImageService, replyService, boardTemplateService);
    }
}

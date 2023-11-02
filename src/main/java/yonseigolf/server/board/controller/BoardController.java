package yonseigolf.server.board.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.service.BoardImageService;
import yonseigolf.server.board.service.BoardService;
import yonseigolf.server.board.service.ReplyService;
import yonseigolf.server.user.dto.response.SessionUser;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.util.CustomResponse;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class BoardController {

    private final BoardService boardService;
    private final BoardImageService boardImageService;
    private final ReplyService replyService;

    @Autowired
    public BoardController(BoardService boardService, BoardImageService boardImageService, ReplyService replyService) {

        this.boardService = boardService;
        this.boardImageService = boardImageService;
        this.replyService = replyService;
    }

    @GetMapping("/boards")
    public ResponseEntity<CustomResponse<Page<SingleBoardResponse>>> findAll(Pageable pageable) {

        Page<SingleBoardResponse> boards = boardService.findAllBoard(pageable);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("전체 게시글 조회 성공", boards));
    }

    @PostMapping("/boards")
    public ResponseEntity<CustomResponse<Void>> createBoard(@RequestBody CreateBoardRequest createBoardRequest, HttpSession session) {

//        SessionUser user = getSessionUser(session);
        User user = User.builder().id(2L).build();
        boardService.postBoard(createBoardRequest, user.getId());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 생성 성공"));
    }

    private SessionUser getSessionUser(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");

        if (user == null) {
            throw new IllegalArgumentException("세션에 유저 정보가 없습니다.");
        }
        return user;
    }
}

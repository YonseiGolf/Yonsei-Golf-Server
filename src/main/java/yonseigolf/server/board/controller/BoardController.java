package yonseigolf.server.board.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.request.PostReplyRequest;
import yonseigolf.server.board.dto.request.UpdateBoardRequest;
import yonseigolf.server.board.dto.response.BoardDetailResponse;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.entity.Category;
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
    public ResponseEntity<CustomResponse<Page<SingleBoardResponse>>> findAll(Pageable pageable, @RequestParam(required = false) Category category) {

        Page<SingleBoardResponse> boards = boardService.findAllBoard(pageable, category);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("전체 게시글 조회 성공", boards));
    }

    @PostMapping("/boards")
    public ResponseEntity<CustomResponse<Void>> createBoard(@RequestBody CreateBoardRequest createBoardRequest, HttpSession session) {

        SessionUser user = getSessionUser(session);
        boardService.postBoard(createBoardRequest, user.getId());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 생성 성공"));
    }

    @GetMapping("/boards/{boardId}")
    public ResponseEntity<CustomResponse<BoardDetailResponse>> findBoardDetail(@PathVariable Long boardId) {

        BoardDetailResponse board = boardService.findBoardDetail(boardId);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 조회 성공", board));
    }

    @PatchMapping("/boards/{boardId}")
    public ResponseEntity<CustomResponse<Void>> updateBoard(@PathVariable Long boardId, @RequestBody UpdateBoardRequest updateBoardRequest, HttpSession session) {

        SessionUser user = getSessionUser(session);
        boardService.updateBoard(boardId, updateBoardRequest, user.getId());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 수정 성공"));
    }

    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<CustomResponse<Void>> deleteBoard(@PathVariable Long boardId, HttpSession session) {

        SessionUser user = getSessionUser(session);
        boardService.deleteBoard(boardId, user.getId());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 삭제 성공"));
    }

    @PostMapping("/boards/{boardId}/replies")
    public ResponseEntity<CustomResponse<Void>> createReply(@PathVariable Long boardId, @RequestBody PostReplyRequest replyRequest, HttpSession session) {

        SessionUser user = getSessionUser(session);
        replyService.postReply(user.getId(), boardId, replyRequest);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("댓글 생성 성공"));
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<CustomResponse<Void>> deleteReply(@PathVariable Long replyId, HttpSession session) {

        SessionUser user = getSessionUser(session);

        replyService.deleteReply(replyId, user.getId());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("댓글 삭제 성공"));
    }

    private SessionUser getSessionUser(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");

        if (user == null) {
            throw new IllegalArgumentException("세션에 유저 정보가 없습니다.");
        }
        return user;
    }
}

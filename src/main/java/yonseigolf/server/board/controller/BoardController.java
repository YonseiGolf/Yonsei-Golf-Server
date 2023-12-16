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
import yonseigolf.server.board.dto.response.AllBoardTemplatesResponse;
import yonseigolf.server.board.dto.response.BoardDetailResponse;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.dto.response.SingleBoardTemplateResponse;
import yonseigolf.server.board.entity.Category;
import yonseigolf.server.board.service.BoardImageService;
import yonseigolf.server.board.service.BoardService;
import yonseigolf.server.board.service.BoardTemplateService;
import yonseigolf.server.board.service.ReplyService;
import yonseigolf.server.util.CustomResponse;


@Slf4j
@Controller
public class BoardController {

    private final BoardService boardService;
    private final BoardImageService boardImageService;
    private final ReplyService replyService;
    private final BoardTemplateService boardTemplateService;

    @Autowired
    public BoardController(BoardService boardService, BoardImageService boardImageService, ReplyService replyService, BoardTemplateService boardTemplateService) {

        this.boardService = boardService;
        this.boardImageService = boardImageService;
        this.replyService = replyService;
        this.boardTemplateService = boardTemplateService;
    }

    @GetMapping("/boards")
    public ResponseEntity<CustomResponse<Page<SingleBoardResponse>>> findAll(Pageable pageable, @RequestParam(required = false) Category category) {

        Page<SingleBoardResponse> boards = boardService.findAllBoard(pageable, category);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("전체 게시글 조회 성공", boards));
    }

    @PostMapping("/boards")
    public ResponseEntity<CustomResponse<Void>> createBoard(@RequestBody CreateBoardRequest createBoardRequest, @RequestAttribute Long userId) {

        boardService.postBoard(createBoardRequest, userId);

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
    public ResponseEntity<CustomResponse<Void>> updateBoard(@PathVariable Long boardId, @RequestBody UpdateBoardRequest updateBoardRequest, @RequestAttribute Long userId) {

        boardService.updateBoard(boardId, updateBoardRequest, userId);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 수정 성공"));
    }

    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<CustomResponse<Void>> deleteBoard(@PathVariable Long boardId, @RequestAttribute Long userId) {

        boardService.deleteBoard(boardId, userId);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 삭제 성공"));
    }

    @PostMapping("/boards/{boardId}/replies")
    public ResponseEntity<CustomResponse<Void>> createReply(@PathVariable Long boardId, @RequestBody PostReplyRequest replyRequest, @RequestAttribute Long userId) {

        replyService.postReply(userId, boardId, replyRequest);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("댓글 생성 성공"));
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<CustomResponse<Void>> deleteReply(@PathVariable Long replyId, @RequestAttribute Long userId) {

        replyService.deleteReply(replyId, userId);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("댓글 삭제 성공"));
    }

    @GetMapping("/admin/boards/templates")
    public ResponseEntity<CustomResponse<AllBoardTemplatesResponse>> getAllBoardTemplate() {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 템플릿 조회 성공",
                        boardTemplateService.findBoardTemplates()));
    }

    @GetMapping("/admin/boards/templates/{templateId}")
    public ResponseEntity<CustomResponse<SingleBoardTemplateResponse>> getBoardTemplate(@PathVariable Long templateId) {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 템플릿 상세 조회 성공",
                        boardTemplateService.findBoardTemplate(templateId)));
    }

    @PostMapping("/admin/boards/templates")
    public ResponseEntity<CustomResponse<Void>> createBoardTemplate() {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 템플릿 생성 성공"));
    }

    @PatchMapping("/admin/boards/templates/{templateId}")
    public ResponseEntity<CustomResponse<Void>> updateBoardTemplate(@PathVariable Long templateId) {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 템플릿 수정 성공"));
    }

    @DeleteMapping("/admin/boards/templates/{templateId}")
    public ResponseEntity<CustomResponse<Void>> deleteBoardTemplate(@PathVariable Long templateId) {

        boardTemplateService.deleteBoardTemplate(templateId);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("게시글 템플릿 삭제 성공"));
    }
}

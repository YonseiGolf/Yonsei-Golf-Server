package yonseigolf.server.board.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.service.BoardImageService;
import yonseigolf.server.board.service.BoardService;
import yonseigolf.server.board.service.ReplyService;
import yonseigolf.server.util.CustomResponse;

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
}

package yonseigolf.server.board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.request.UpdateBoardRequest;
import yonseigolf.server.board.dto.response.AllReplyResponse;
import yonseigolf.server.board.dto.response.BoardDetailResponse;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.dto.response.SingleReplyResponse;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Category;
import yonseigolf.server.board.entity.Reply;
import yonseigolf.server.board.exception.BoardNotFoundException;
import yonseigolf.server.board.repository.BoardRepository;
import yonseigolf.server.board.repository.ReplyRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository, ReplyRepository replyRepository) {

        this.boardRepository = boardRepository;
        this.replyRepository = replyRepository;
    }

    public Page<SingleBoardResponse> findAllBoard(Pageable pageable, Category category) {

        return boardRepository.findAllBoardPaging(pageable, category);
    }

    public void postBoard(CreateBoardRequest createBoardRequest, long userId) {

        boardRepository.save(Board.createBoardForPost(createBoardRequest, userId));
    }

    @Transactional
    public void updateBoard(Long boardId, UpdateBoardRequest createBoardRequest) {

        Board board = findById(boardId);
        board.updateBoard(createBoardRequest);
    }

    @Transactional
    public void deleteBoard(Long boardId) {

        Board board = findById(boardId);
        board.deleteBoard();
    }

    public BoardDetailResponse findBoardDetail(Long boardId) {

        Board board = findById(boardId);
        List<Reply> replies = replyRepository.findByBoardId(boardId);

        List<SingleReplyResponse> result = replies.stream()
                .map(SingleReplyResponse::fromReply)
                .collect(Collectors.toList());
        AllReplyResponse repliesResponse = AllReplyResponse.fromReplies(result);

        return BoardDetailResponse.createResponse(board, repliesResponse);
    }

    private Board findById(Long boardId) {

        return boardRepository.findById(boardId).
                orElseThrow(() -> new BoardNotFoundException("해당 게시글이 존재하지 않습니다."));
    }
}

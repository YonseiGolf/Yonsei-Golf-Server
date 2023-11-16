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

    // TODO: 작성자와 수정하려는 사람이 같은 지 확인
    @Transactional
    public void updateBoard(Long boardId, UpdateBoardRequest createBoardRequest, Long userId) {

        Board board = findById(boardId);
        if (board.getWriter().getId() != userId) {
            throw new IllegalArgumentException("작성자와 수정하려는 사람이 다릅니다.");
        }
        board.updateBoard(createBoardRequest);
    }

    // TODO: 작성자와 삭제하려는 사람이 같은지 확인
    @Transactional
    public void deleteBoard(Long boardId, Long userId) {

        Board board = findById(boardId);
        if (board.getWriter().getId() != userId) {
            throw new IllegalArgumentException("작성자만 게시글을 삭제할 수 있습니다.");
        }
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

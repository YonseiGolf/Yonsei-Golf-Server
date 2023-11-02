package yonseigolf.server.board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.repository.BoardRepository;


@Service
public class BoardService {

    private final BoardRepository boardRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository) {

        this.boardRepository = boardRepository;
    }

    public Page<SingleBoardResponse> findAllBoard(Pageable pageable) {

        return boardRepository.findAllBoardPaging(pageable);
    }

    public void postBoard(CreateBoardRequest createBoardRequest, long userId) {

        boardRepository.save(Board.createBoardForPost(createBoardRequest, userId));
    }
}

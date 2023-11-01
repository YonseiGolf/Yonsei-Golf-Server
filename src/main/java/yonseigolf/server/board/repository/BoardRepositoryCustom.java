package yonseigolf.server.board.repository;

import org.springframework.data.domain.Page;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import org.springframework.data.domain.Pageable;



public interface BoardRepositoryCustom {

    Page<SingleBoardResponse> findAllBoardPaging(Pageable pageable);
}

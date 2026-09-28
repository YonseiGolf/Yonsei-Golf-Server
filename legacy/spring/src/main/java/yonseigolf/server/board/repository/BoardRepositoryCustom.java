package yonseigolf.server.board.repository;

import org.springframework.data.domain.Page;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import org.springframework.data.domain.Pageable;
import yonseigolf.server.board.entity.Category;


public interface BoardRepositoryCustom {

    Page<SingleBoardResponse> findAllBoardPaging(Pageable pageable, Category category);
}

package yonseigolf.server.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.board.entity.Reply;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByBoardId(Long boardId);
}

package yonseigolf.server.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.board.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}

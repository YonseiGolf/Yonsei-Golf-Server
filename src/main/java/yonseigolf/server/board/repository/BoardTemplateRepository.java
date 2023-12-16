package yonseigolf.server.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.board.entity.BoardTemplate;

public interface BoardTemplateRepository extends JpaRepository<BoardTemplate, Long> {
}

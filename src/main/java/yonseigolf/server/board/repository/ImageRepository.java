package yonseigolf.server.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.board.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}

package yonseigolf.server.board.entity;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.*;

@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String url;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
}

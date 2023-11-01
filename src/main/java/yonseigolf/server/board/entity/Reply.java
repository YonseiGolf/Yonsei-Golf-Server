package yonseigolf.server.board.entity;

import yonseigolf.server.user.entity.User;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.*;

@Entity
public class Reply {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String content;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

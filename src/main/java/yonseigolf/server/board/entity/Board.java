package yonseigolf.server.board.entity;

import lombok.*;
import yonseigolf.server.user.entity.User;

import javax.persistence.*;

import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;
    @Enumerated(EnumType.STRING)
    private Category category;
    private String title;
    private String content;
    private LocalDateTime date;
}

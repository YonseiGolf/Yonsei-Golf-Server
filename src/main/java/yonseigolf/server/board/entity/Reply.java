package yonseigolf.server.board.entity;

import lombok.*;
import yonseigolf.server.board.dto.request.PostReplyRequest;
import yonseigolf.server.user.entity.User;

import javax.persistence.*;

import java.time.LocalDateTime;


@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static Reply createReplyForPost(long writerId, long boardId, PostReplyRequest request) {

        return Reply.builder()
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .board(Board.createBoardForForeignKey(boardId))
                .user(User.createUserForForeignKey(writerId))
                .build();
    }
}

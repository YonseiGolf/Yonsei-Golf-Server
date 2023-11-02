package yonseigolf.server.board.entity;

import lombok.*;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
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
    private LocalDateTime createdAt;

    public static Board createBoardForPost(CreateBoardRequest request, long userId) {

        return Board.builder()
                .writer(User.createUserForForeignKey(userId))
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
    }
}

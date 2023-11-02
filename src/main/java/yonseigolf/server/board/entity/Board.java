package yonseigolf.server.board.entity;

import lombok.*;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.request.UpdateBoardRequest;
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
    private boolean deleted;

    public static Board createBoardForPost(CreateBoardRequest request, long userId) {

        return Board.builder()
                .writer(User.createUserForForeignKey(userId))
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    public void updateBoard(UpdateBoardRequest request) {

        this.category = request.getCategory();
        this.title = request.getTitle();
        this.content = request.getContent();
    }
}

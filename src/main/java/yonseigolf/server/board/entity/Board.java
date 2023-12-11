package yonseigolf.server.board.entity;

import lombok.*;
import yonseigolf.server.board.dto.request.CreateBoardRequest;
import yonseigolf.server.board.dto.request.UpdateBoardRequest;
import yonseigolf.server.board.exception.DeletedBoardException;
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

    public static Board createBoardForForeignKey(long boardId) {

        return Board.builder()
                .id(boardId)
                .build();
    }

    public void updateBoard(UpdateBoardRequest request) {

        if (this.deleted) {
            throw new DeletedBoardException("이미 삭제된 게시글 입니다.");
        }

        this.category = request.getCategory();
        this.title = request.getTitle();
        this.content = request.getContent();
    }

    public void deleteBoard() {

        if (this.deleted) {
            throw new DeletedBoardException("이미 삭제된 게시글 입니다.");
        }

        this.deleted = true;
    }

    public void checkOwner(Long userId) {

        if(!this.writer.checkOwner(userId)){
            throw new IllegalArgumentException("작성자만 게시글을 수정/삭제 할 수 있습니다.");
        }
    }
}

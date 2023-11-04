package yonseigolf.server.board.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import yonseigolf.server.board.dto.response.QSingleBoardResponse;
import yonseigolf.server.board.dto.response.SingleBoardResponse;
import yonseigolf.server.board.entity.Category;

import java.util.List;

import static yonseigolf.server.board.entity.QBoard.board;
import static yonseigolf.server.user.entity.QUser.user;

public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Autowired
    public BoardRepositoryImpl(JPAQueryFactory queryFactory) {

        this.queryFactory = queryFactory;
    }

    @Override
    public Page<SingleBoardResponse> findAllBoardPaging(Pageable pageable, Category category) {

        QueryResults<SingleBoardResponse> results = queryFactory.select(new QSingleBoardResponse(
                        board.id,
                        board.category,
                        board.title,
                        board.writer.name,
                        board.createdAt
                ))
                .from(board)
                .join(board.writer, user)
                .where(board.deleted.eq(false))
                .where(eqCategory(category))
                .orderBy(board.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();


        List<SingleBoardResponse> boards = results.getResults();

        long total = results.getTotal();


        return new PageImpl<>(boards, pageable, total);
    }

    private BooleanExpression eqCategory(Category category) {
        if (category == null) {
            return null;
        }
        return board.category.eq(category);
    }
}

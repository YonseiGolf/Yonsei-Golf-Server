package yonseigolf.server.user.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import yonseigolf.server.user.dto.response.SingleUserResponse;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;

import java.util.List;
import java.util.stream.Collectors;

import static yonseigolf.server.user.entity.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SingleUserResponse> findAllUsers(Pageable pageable, UserClass userClass) {

        QueryResults<User> results = queryFactory.selectFrom(user)
                .where(user.userClass.eq(userClass))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<SingleUserResponse> userResponses = results.getResults().stream()
                .map(SingleUserResponse::fromUser)
                .collect(Collectors.toList());

        return new PageImpl<>(userResponses, pageable, results.getTotal());
    }
}

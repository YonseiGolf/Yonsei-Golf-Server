package yonseigolf.server.apply.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import yonseigolf.server.apply.dto.response.QSingleApplicationResult;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;

import java.util.List;

import static yonseigolf.server.apply.entity.QApplication.application;

@RequiredArgsConstructor
public class ApplicationRepositoryImpl implements ApplicationRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SingleApplicationResult> getApplicationResults(Boolean documentPass, Boolean finalPass, Pageable pageable) {

        QueryResults<SingleApplicationResult> result = queryFactory.select(
                        new QSingleApplicationResult(
                                application.id,
                                application.photo,
                                application.name,
                                application.interviewTime,
                                application.documentPass,
                                application.finalPass
                        ))
                .from(application)
                .where(
                        documentPassEq(documentPass),
                        finalPassEq(finalPass)
                )
                .orderBy(application.interviewTime.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<SingleApplicationResult> results = result.getResults();
        long total = result.getTotal();

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression documentPassEq(Boolean documentPass) {

        if (documentPass == null) {
            return application.documentPass.isNull();
        }

        return application.documentPass.eq(documentPass);
    }

    private BooleanExpression finalPassEq(Boolean finalPass) {

        if (finalPass == null) {
            return application.finalPass.isNull();
        }

        return application.finalPass.eq(finalPass);
    }
}

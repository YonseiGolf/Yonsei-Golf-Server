package yonseigolf.server.apply.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.entity.Application;

import java.util.List;

public interface ApplicationRepositoryCustom {

    Page<SingleApplicationResult> getApplicationResults(Boolean documentPass, Boolean finalPass, Pageable pageable);

    List<Application> findApplicationsForEmail(Boolean documentPass, Boolean finalPass);
}

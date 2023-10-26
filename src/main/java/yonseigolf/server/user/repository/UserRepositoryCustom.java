package yonseigolf.server.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yonseigolf.server.user.dto.response.SingleUserResponse;


public interface UserRepositoryCustom {

    Page<SingleUserResponse> findAllUsers(Pageable pageable);
}

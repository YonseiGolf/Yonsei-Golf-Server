package yonseigolf.server.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AllBoardTemplatesResponse {

    private List<BoardTemplatesResponse> templates;
}

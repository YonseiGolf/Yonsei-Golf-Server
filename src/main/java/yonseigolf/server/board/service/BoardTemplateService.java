package yonseigolf.server.board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.board.dto.response.AllBoardTemplatesResponse;
import yonseigolf.server.board.dto.response.BoardTemplatesResponse;
import yonseigolf.server.board.dto.response.SingleBoardTemplateResponse;
import yonseigolf.server.board.entity.BoardTemplate;
import yonseigolf.server.board.repository.BoardTemplateRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardTemplateService {

    private final BoardTemplateRepository boardTemplateRepository;

    @Autowired
    public BoardTemplateService(BoardTemplateRepository boardTemplateRepository) {
        this.boardTemplateRepository = boardTemplateRepository;
    }

    public AllBoardTemplatesResponse findBoardTemplates() {

        List<BoardTemplate> templates = boardTemplateRepository.findAll();

        List<BoardTemplatesResponse> responses = templates.stream()
                .map(BoardTemplatesResponse::new)
                .collect(Collectors.toList());

        return AllBoardTemplatesResponse.builder()
                .templates(responses)
                .build();
    }

    public SingleBoardTemplateResponse findBoardTemplate(long id) {

        BoardTemplate boardTemplate = findBoardTemplateById(id);

        return SingleBoardTemplateResponse.builder()
                .id(boardTemplate.getId())
                .title(boardTemplate.getTitle())
                .contents(boardTemplate.getContents())
                .build();
    }

    // TODO: 적절한 예외 처리 하기
    public void deleteBoardTemplate(long id) {

        if (boardTemplateRepository.existsById(id)) {
            boardTemplateRepository.deleteById(id);
        }
    }

    private BoardTemplate findBoardTemplateById(long id) {

        return boardTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다."));
    }
}

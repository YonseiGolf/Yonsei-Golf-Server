package yonseigolf.server.board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.board.dto.request.CreateBoardTemplateRequest;
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

    public void createBoardTemplate(CreateBoardTemplateRequest request) {

        BoardTemplate boardTemplate = BoardTemplate.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .build();

        boardTemplateRepository.save(boardTemplate);
    }

    @Transactional
    public void updateBoardTemplate(long id, CreateBoardTemplateRequest request) {

        BoardTemplate boardTemplate = findBoardTemplateById(id);

        boardTemplate.update(request.getTitle(), request.getContents());
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

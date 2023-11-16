package yonseigolf.server.board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.board.dto.request.PostReplyRequest;
import yonseigolf.server.board.entity.Reply;
import yonseigolf.server.board.repository.ReplyRepository;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;

    @Autowired
    public ReplyService(ReplyRepository replyRepository) {
        this.replyRepository = replyRepository;
    }

    public void postReply(long writerId, long boardId, PostReplyRequest replyRequest) {

        Reply reply = Reply.createReplyForPost(writerId, boardId, replyRequest);
        replyRepository.save(reply);
    }

    // TODO: 작성자와 삭제하려는 사람이 같은지 확인
    public void deleteReply(long replyId, long userId) {
        Reply reply = findReply(replyId);
        if (reply.getUser().getId() != userId){
            throw new IllegalArgumentException("작성자가 아닙니다.");
        }
        replyRepository.deleteById(replyId);
    }

    private Reply findReply(long replyId) {

        return replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
    }
}

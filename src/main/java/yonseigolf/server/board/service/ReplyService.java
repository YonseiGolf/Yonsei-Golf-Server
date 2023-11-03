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

    public void deleteReply(long replyId) {
        replyRepository.deleteById(replyId);
    }
}

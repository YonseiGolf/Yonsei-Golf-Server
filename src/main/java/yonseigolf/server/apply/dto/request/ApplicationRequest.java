package yonseigolf.server.apply.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    private String name;
    private String photo;
    private long age;
    private long studentId;
    private String major;
    private String phoneNumber;
    private long golfDuration;
    private long roundCount;
    private boolean lessonStatus;
    private boolean clubStatus;
    private String selfIntroduction;
    private String applyReason;
    private String skillEvaluation;
    private String golfMemory;
    // 다른 동아리 활동 질문
    private String otherClub;
    private String swingVideo;
    private LocalDateTime submitTime;
}

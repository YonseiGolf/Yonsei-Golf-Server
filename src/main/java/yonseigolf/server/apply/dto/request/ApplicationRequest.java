package yonseigolf.server.apply.dto.request;

import java.time.LocalDate;
import java.util.List;
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
    private LocalDate birthDate;
    private long studentId;
    private String major;
    private String email;
    private String phoneNumber;
    private String selfIntroduction;
    private String applyReason;
    private String skillEvaluation;
    private String golfMemory;
    // 다른 동아리 활동 질문
    private List<ActivityClubRequest> activityClubs;
    private String swingVideo;
    private LocalDateTime submitTime;
    private Long semester;
    private List<Long> availableInterviewTimeIds;
}


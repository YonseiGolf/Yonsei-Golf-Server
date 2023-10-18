package yonseigolf.server.apply.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.apply.dto.request.ApplicationRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    private Boolean passFail;
    private String etc;
    private Long interviewId;

    public static Application of(ApplicationRequest request) {

        return Application.builder()
                .name(request.getName())
                .photo(request.getPhoto())
                .age(request.getAge())
                .studentId(request.getStudentId())
                .major(request.getMajor())
                .phoneNumber(request.getPhoneNumber())
                .golfDuration(request.getGolfDuration())
                .roundCount(request.getRoundCount())
                .lessonStatus(request.isLessonStatus())
                .clubStatus(request.isClubStatus())
                .selfIntroduction(request.getSelfIntroduction())
                .applyReason(request.getApplyReason())
                .skillEvaluation(request.getSkillEvaluation())
                .golfMemory(request.getGolfMemory())
                .otherClub(request.getOtherClub())
                .swingVideo(request.getSwingVideo())
                .submitTime(LocalDateTime.now())
                .build();

    }
}

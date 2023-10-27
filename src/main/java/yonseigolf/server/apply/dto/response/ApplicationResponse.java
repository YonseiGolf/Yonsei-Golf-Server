package yonseigolf.server.apply.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.apply.entity.Application;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;
    private String name;
    private String photo;
    private long age;
    private long studentId;
    private String email;
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
    private String otherClub;
    private String swingVideo;
    @JsonFormat(pattern = "MM월dd일 HH:mm")
    private LocalDateTime submitTime;
    private Boolean documentPass;
    private Boolean finalPass;
    @JsonFormat(pattern = "MM월dd일 HH:mm")
    private LocalDateTime interviewTime;

    public static ApplicationResponse fromApplication(Application application) {

        return ApplicationResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .photo(application.getPhoto())
                .age(application.getAge())
                .studentId(application.getStudentId())
                .email(application.getEmail())
                .major(application.getMajor())
                .phoneNumber(application.getPhoneNumber())
                .golfDuration(application.getGolfDuration())
                .roundCount(application.getRoundCount())
                .lessonStatus(application.isLessonStatus())
                .clubStatus(application.isClubStatus())
                .selfIntroduction(application.getSelfIntroduction())
                .applyReason(application.getApplyReason())
                .skillEvaluation(application.getSkillEvaluation())
                .golfMemory(application.getGolfMemory())
                .otherClub(application.getOtherClub())
                .swingVideo(application.getSwingVideo())
                .submitTime(application.getSubmitTime())
                .documentPass(application.getDocumentPass())
                .finalPass(application.getFinalPass())
                .interviewTime(application.getInterviewTime())
                .build();
    }
}

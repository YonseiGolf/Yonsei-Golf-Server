package yonseigolf.server.apply.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
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
    private LocalDate birthDate;
    private long studentId;
    private String email;
    private String major;
    private String phoneNumber;
    private String selfIntroduction;
    private String applyReason;
    private String skillEvaluation;
    private String golfMemory;
    //    private String otherClub;
    private List<ActivityClubResponse> activities;
    private String swingVideo;
    @JsonFormat(pattern = "MM월dd일 HH:mm")
    private LocalDateTime submitTime;
    private Boolean documentPass;
    private Boolean finalPass;
    @JsonFormat(pattern = "MM월dd일 HH:mm")
    private LocalDateTime interviewTime;
    private Long semester;
    private List<InterviewTimeResponse> availableInterviewTimes;

    public static ApplicationResponse fromApplication(Application application) {
        List<ActivityClubResponse> activityClubResponses = application.getActivities().stream()
            .map(activity -> new ActivityClubResponse(
                activity.getClubName(),
                activity.getStartDate(),
                activity.getEndDate(),
                activity.getRole()
            ))
            .toList();

        List<InterviewTimeResponse> interviewTimeResponses = application.getAvailableInterviewTimes().stream()
            .map(interviewTime -> InterviewTimeResponse.builder()
                .id(interviewTime.getId())
                .interviewDateTime(interviewTime.getInterviewDateTime())
                .build())
            .toList();

        return ApplicationResponse.builder()
            .id(application.getId())
            .name(application.getName())
            .photo(application.getPhoto())
            .birthDate(application.getBirthDate())
            .studentId(application.getStudentId())
            .email(application.getEmail())
            .major(application.getMajor())
            .phoneNumber(application.getPhoneNumber())
            .selfIntroduction(application.getSelfIntroduction())
            .applyReason(application.getApplyReason())
            .skillEvaluation(application.getSkillEvaluation())
            .golfMemory(application.getGolfMemory())
            .activities(activityClubResponses)
            .swingVideo(application.getSwingVideo())
            .submitTime(application.getSubmitTime())
            .documentPass(application.getDocumentPass())
            .finalPass(application.getFinalPass())
            .interviewTime(application.getInterviewTime())
            .semester(application.getSemester())
            .availableInterviewTimes(interviewTimeResponses)
            .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityClubResponse {
        private String clubName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String role;
    }
}

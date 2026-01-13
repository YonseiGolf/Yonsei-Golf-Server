package yonseigolf.server.apply.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.UpdatePassRequest;

import javax.persistence.Column;
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
    private LocalDate birthDate;
    private long studentId;
    private String email;
    private String major;
    private String phoneNumber;
    @Column(columnDefinition = "TEXT")
    private String selfIntroduction;
    @Column(columnDefinition = "TEXT")
    private String applyReason;
    @Column(columnDefinition = "TEXT")
    private String skillEvaluation;
    @Column(columnDefinition = "TEXT")
    private String golfMemory;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "applicationId")
    private List<ClubActivity> activities = new ArrayList<>();
    private String swingVideo;
    private LocalDateTime submitTime;
    private Boolean documentPass;
    private Boolean finalPass;
    private LocalDateTime interviewTime;
    // 지원 기수
    private Long semester;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "application_available_interview_time",
            joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "interview_time_id")
    )
    @Builder.Default
    private List<InterviewTime> availableInterviewTimes = new ArrayList<>();

    public static Application of(ApplicationRequest request) {
        return Application.builder()
            .name(request.getName())
            .photo(request.getPhoto())
            .birthDate(request.getBirthDate())
            .studentId(request.getStudentId())
            .email(request.getEmail())
            .major(request.getMajor())
            .phoneNumber(request.getPhoneNumber())
            .selfIntroduction(request.getSelfIntroduction())
            .applyReason(request.getApplyReason())
            .skillEvaluation(request.getSkillEvaluation())
            .golfMemory(request.getGolfMemory())
            .swingVideo(request.getSwingVideo())
            .submitTime(LocalDateTime.now())
            .semester(request.getSemester())
            .build();
    }

    public void updatePass(UpdatePassRequest request) {

        this.documentPass = request.getDocumentPass();
        this.finalPass = request.getFinalPass();
    }

    public void updateInterviewTime(LocalDateTime time) {

        this.interviewTime = time;
    }

    public void setAvailableInterviewTimes(List<InterviewTime> interviewTimes) {
        this.availableInterviewTimes = interviewTimes;
    }
}

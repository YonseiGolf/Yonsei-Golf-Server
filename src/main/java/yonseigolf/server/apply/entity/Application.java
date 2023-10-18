package yonseigolf.server.apply.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
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
}

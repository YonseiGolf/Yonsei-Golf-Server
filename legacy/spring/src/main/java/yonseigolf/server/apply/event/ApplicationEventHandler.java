package yonseigolf.server.apply.event;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.entity.ApplicationResultLog;
import yonseigolf.server.apply.repository.ApplicationResultLogRepository;
import yonseigolf.server.email.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationEventHandler {

    private final EmailService emailService;
    private final ApplicationResultLogRepository applicationResultLogRepository;

    @EventListener(AppliedEvent.class)
    public void handleAppliedEvent(AppliedEvent event) {
        // email 전송되었는지 확인해보고 전송하기
        boolean appliedHistory = applicationResultLogRepository
            .existsByApplicationIdAndNotificationType(event.getApplicationid(), null);

        if (appliedHistory) {
            return;
        }

        log.info("ApplicationEventHandler::handleAppliedEvent applied user : {} application id: {}", event.getAppliedUserName(),event.getApplicationid());
        ApplicationResultLog applicationResultLog = ApplicationResultLog.builder()
            .applicationId(event.getApplicationid())
            .notificationType(null)
            .sentAt(LocalDateTime.now())
            .build();
        applicationResultLogRepository.save(applicationResultLog);

        try {
            emailService.sendEmail(event.getEmail(),
                "안녕하세요. 연세골프입니다.\n\n",
                event.getAppliedUserName() + "님의 지원서가 정상적으로 제출되었습니다. \n\n" +
                    "서류 합격 여부는 추후 이메일로 공지될 예정입니다. \n\n" +
                    "감사합니다."
            );
        } catch (Exception e) {
            log.error("지원서 제출 이메일 전송 실패 - applicationId: {}, email: {}", event.getApplicationid(), event.getEmail(), e);
        }
    }
}

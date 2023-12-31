package yonseigolf.server.apply.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
public class SingleApplicationResult {

    private long id;
    private String photo;
    private String name;
    @JsonFormat(pattern = "MM월dd일 HH:mm")
    private LocalDateTime interviewTime;
    private boolean documentPass;
    private boolean finalPass;

    @QueryProjection
    public SingleApplicationResult(long id, String photo, String name, LocalDateTime interviewTime, boolean documentPass, boolean finalPass) {

        this.id = id;
        this.photo = photo;
        this.name = name;
        this.interviewTime = interviewTime;
        this.documentPass = documentPass;
        this.finalPass = finalPass;
    }
}

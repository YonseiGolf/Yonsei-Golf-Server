package yonseigolf.server.apply.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String photoKey;
    private String name;
    @JsonFormat(pattern = "MM월dd일 HH:mm")
    private LocalDateTime interviewTime;
    private boolean documentPass;
    private boolean finalPass;

    @QueryProjection
    public SingleApplicationResult(long id, String photo, String photoKey, String name,
                                   LocalDateTime interviewTime, boolean documentPass, boolean finalPass) {

        this.id = id;
        this.photo = photo;
        this.photoKey = photoKey;
        this.name = name;
        this.interviewTime = interviewTime;
        this.documentPass = documentPass;
        this.finalPass = finalPass;
    }

    public SingleApplicationResult(long id, String photo, String name, LocalDateTime interviewTime,
                                   boolean documentPass, boolean finalPass) {

        this(id, photo, null, name, interviewTime, documentPass, finalPass);
    }

    public SingleApplicationResult withPhotoUrl(String photoUrl) {

        return new SingleApplicationResult(
                id, photoUrl, null, name, interviewTime, documentPass, finalPass);
    }
}

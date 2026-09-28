package yonseigolf.server.apply.dto.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {

    @NotBlank(message = "파일 이름을 입력해주세요.")
    @Size(max = 255, message = "파일 이름은 255자 이하여야 합니다.")
    private String fileName;

    @NotBlank(message = "이미지 형식을 입력해주세요.")
    @Pattern(
            regexp = "image/(jpeg|png|webp|gif)",
            message = "JPG, PNG, WEBP, GIF 이미지만 업로드할 수 있습니다."
    )
    private String contentType;

    @Positive(message = "파일 크기는 0보다 커야 합니다.")
    @Max(value = 10 * 1024 * 1024, message = "이미지는 10MB 이하여야 합니다.")
    private long fileSize;
}

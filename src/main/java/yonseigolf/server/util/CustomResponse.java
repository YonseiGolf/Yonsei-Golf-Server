package yonseigolf.server.util;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class CustomResponse<T> {

    private static final String HTTP_STATUS_SUCCESS = "success";
    private String status;
    private int code;
    private String message;
    private T data;

    public CustomResponse(String status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static CustomResponse successResponse(String message) {

        return new CustomResponse(
                HTTP_STATUS_SUCCESS,
                HttpStatus.OK.value(),
                message
        );
    }

    public static <T> CustomResponse<T> successResponse(String message, T data) {

        return new CustomResponse(
                HTTP_STATUS_SUCCESS,
                HttpStatus.OK.value(),
                message,
                data
        );
    }
}

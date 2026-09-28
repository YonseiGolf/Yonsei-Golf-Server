package yonseigolf.server.user.dto.request;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SignUpUserRequestTest {

    private LocalValidatorFactoryBean validator;

    @BeforeEach
    public void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
    }

    @Test
    @DisplayName("유효한 회원가입 요청이 들어오면 예외가 발생하지 않는다.")
    public void testValidSignUpUserRequest() {
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("John")
                .phoneNumber("1234567890")
                .studentId(10)
                .major("CS")
                .semester(2)
                .build();

        Set<ConstraintViolation<SignUpUserRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름이 길 경우 예외가 발생한다.")
    public void testNameSizeViolation() {
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("JohnDoeJohnDoe") // too long
                .phoneNumber("1234567890")
                .studentId(10)
                .major("CS")
                .semester(2)
                .build();

        Set<ConstraintViolation<SignUpUserRequest>> violations = validator.validate(request);
        ConstraintViolation<SignUpUserRequest> violation = violations.iterator().next();

        assertAll(
                () -> assertThat(violations).isNotEmpty(),
                () -> assertThat(violations).hasSize(1),
                () -> assertThat(violation.getPropertyPath().toString()).isEqualTo("name")
        );
    }

    @Test
    @DisplayName("핸드폰 번호가 길 경우 예외가 발생한다.")
    public void testPhoneNumberSizeViolation() {
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("John")
                .phoneNumber("1234567890123456") // too long
                .studentId(10)
                .major("CS")
                .semester(2)
                .build();

        Set<ConstraintViolation<SignUpUserRequest>> violations = validator.validate(request);
        ConstraintViolation<SignUpUserRequest> violation = violations.iterator().next();

        assertAll(
                () -> assertThat(violations).isNotEmpty(),
                () -> assertThat(violations).hasSize(1),
                () -> assertThat(violation.getPropertyPath().toString()).isEqualTo("phoneNumber")
        );
    }

    @Test
    @DisplayName("학번이 범위를 벗어나면 예외가 발생한다.")
    public void testStudentIdRangeViolation() {
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("John")
                .phoneNumber("1234567890")
                .studentId(100) // out of range
                .major("CS")
                .semester(2)
                .build();

        Set<ConstraintViolation<SignUpUserRequest>> violations = validator.validate(request);
        ConstraintViolation<SignUpUserRequest> violation = violations.iterator().next();

        assertAll(
                () -> assertThat(violations).isNotEmpty(),
                () -> assertThat(violations).hasSize(1),
                () -> assertThat(violation.getPropertyPath().toString()).isEqualTo("studentId")
        );
    }

    @Test
    @DisplayName("전공 범위를 벗어나면 예외가 발생한다.")
    public void testMajorRangeViolation() {
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("John")
                .phoneNumber("1234567890")
                .studentId(10)
                .major("CSCSCSCSCSCSCCSCCS")
                .semester(2)
                .build();

        Set<ConstraintViolation<SignUpUserRequest>> violations = validator.validate(request);
        ConstraintViolation<SignUpUserRequest> violation = violations.iterator().next();

        assertAll(
                () -> assertThat(violations).isNotEmpty(),
                () -> assertThat(violations).hasSize(1),
                () -> assertThat(violation.getPropertyPath().toString()).isEqualTo("major")
        );
    }

    @Test
    @DisplayName("기수가 범위를 벗어나면 예외가 발생한다.")
    public void testSemesterRangeViolation() {
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("John")
                .phoneNumber("1234567890")
                .studentId(10)
                .major("CS")
                .semester(100)
                .build();

        Set<ConstraintViolation<SignUpUserRequest>> violations = validator.validate(request);
        ConstraintViolation<SignUpUserRequest> violation = violations.iterator().next();

        assertAll(
                () -> assertThat(violations).isNotEmpty(),
                () -> assertThat(violations).hasSize(1),
                () -> assertThat(violation.getPropertyPath().toString()).isEqualTo("semester")
        );
    }
}
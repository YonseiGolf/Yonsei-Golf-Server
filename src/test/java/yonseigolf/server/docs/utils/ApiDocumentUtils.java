package yonseigolf.server.docs.utils;

import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

public interface ApiDocumentUtils {
    static OperationRequestPreprocessor getDocumentRequest() {

        return preprocessRequest(
                modifyUris()
                        .scheme("https")
                        .host("api.yonsei-golf.co.kr")
                        .removePort(),
                prettyPrint());
    }

    static OperationResponsePreprocessor getDocumentResponse() {

        return preprocessResponse(prettyPrint());
    }
}

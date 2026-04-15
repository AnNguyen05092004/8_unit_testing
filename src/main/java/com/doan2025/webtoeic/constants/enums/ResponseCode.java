package com.doan2025.webtoeic.constants.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ResponseCode {

    UNCATEGORIZED_EXCEPTION(404, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(404, "Invalid key", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(404, "Email must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(404, "{entity} is not true", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(404, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(404, "You do not have permission", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(404, "{entity} is expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALIDATED(404, "{entity} is invalid", HttpStatus.UNAUTHORIZED),

    INVALID_DOB(404, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID(404, "{entity} is invalid", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(404, "{entity} is invalid", HttpStatus.UNAUTHORIZED),

    EXISTED(404, "{entity} already exists", HttpStatus.BAD_REQUEST),
    NOT_EXISTED(404, "{entity} not existed", HttpStatus.NOT_FOUND),
    IS_NULL(404, "{entity} is null", HttpStatus.BAD_REQUEST),
    UNSUPPORTED(404, "Unsupported: {entity}", HttpStatus.BAD_REQUEST),
    NOT_AVAILABLE(404, "{entity} not available", HttpStatus.BAD_REQUEST),

    SUCCESS(200, "{entity} is successfully", HttpStatus.OK),
    CREATE_SUCCESS(200, "{entity} has been created ", HttpStatus.CREATED),
    UPDATE_SUCCESS(200, "{entity} has been updated ", HttpStatus.OK),
    DELETE_SUCCESS(200, "{entity} has been deleted ", HttpStatus.OK),
    GET_SUCCESS(200, "Get {entity} is successfully", HttpStatus.OK),
    UPLOAD_SUCCESS(200, "Upload {entity} is successfully", HttpStatus.OK),
    CANCELLED_SUCCESS(200, "Cancelled {entity} is successfully", HttpStatus.OK),
    ANALYSIS_SUCCESS(200, "Analysis {entity} is successfully", HttpStatus.OK),

    CANNOT_GET(404, "Cannot get {entity}", HttpStatus.NOT_FOUND),
    CANNOT_DELETE(404, "Cannot delete {entity}", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE(404, "Cannot update {entity}", HttpStatus.BAD_REQUEST),
    CANNOT_CREATE(404, "Cannot create {entity}", HttpStatus.BAD_REQUEST),
    CANNOT_UPLOAD(404, "Cannot upload {entity}", HttpStatus.BAD_REQUEST),
    CANNOT_SEND(404, "Cannot send {entity}", HttpStatus.BAD_REQUEST),

    NOT_MATCHED(404, "Not matched {entity}", HttpStatus.BAD_REQUEST),
    NOT_PERMISSION(404, "{entity} not permission ", HttpStatus.BAD_REQUEST),
    NOT_SUCCESS(404, "{entity} not successfully ", HttpStatus.BAD_REQUEST),

    HAS_PAID(200, "{entity} has been paid ", HttpStatus.OK),
    OVER_DUE(404, "{entity} has been overdue ", HttpStatus.OK),
    NOT_START(404, "{entity} has not started yet ", HttpStatus.OK),
    CANNOT_READ(404, "{entity} cannot read ", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_TYPE(404, "{entity} is not supported", HttpStatus.BAD_REQUEST),
    ;


    private final int code;
    private final HttpStatusCode statusCode;
    private String message;

    ResponseCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage(ResponseObject responseObject) {
        return message.replace("{entity}", responseObject.toString());
    }

    public void setMessage(ResponseObject responseObject) {
        this.message = message.replace("{entity}", responseObject.toString());
    }
}

package com.doan2025.webtoeic.exception;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class WebToeicException extends RuntimeException {
    private ResponseCode responseCode;
    private ResponseObject responseObject;
    private String message;


    public WebToeicException(ResponseCode responseCode, ResponseObject responseObject) {
        super(responseCode.getMessage().replace("{entity}", responseObject.toString()));
        this.responseCode = responseCode;
        this.responseObject = responseObject;
    }

}

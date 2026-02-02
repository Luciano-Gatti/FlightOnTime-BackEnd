package com.flightontime.app_predictor.infrastructure.in.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.server.ServerRequest;

@Controller
public class ApiErrorController implements ErrorController {
    private final ErrorAttributes errorAttributes;

    public ApiErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes((ServerRequest) request,
                ErrorAttributeOptions.defaults()
        );
        Object statusValue = attributes.getOrDefault("status", 500);
        int status = statusValue instanceof Integer value
                ? value
                : Integer.parseInt(statusValue.toString());
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(attributes);
    }
}

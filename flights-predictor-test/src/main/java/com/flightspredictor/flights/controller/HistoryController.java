package com.flightspredictor.flights.controller;

import com.flightspredictor.flights.domain.dto.history.HistoryItemResponse;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import com.flightspredictor.flights.domain.service.history.HistoryService;
import com.flightspredictor.flights.infra.security.UserPrincipal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<Page<HistoryItemResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String dest,
            @RequestParam(required = false) PredictedStatus status
    ) {
        Long userId = resolveUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<HistoryItemResponse> response = historyService.getHistory(
                userId,
                toLocalDateTime(from),
                toLocalDateTime(to),
                normalize(origin),
                normalize(dest),
                status,
                pageable
        );
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        Object details = authentication.getDetails();
        if (details instanceof java.util.Map<?, ?> map) {
            Object userId = map.get("userId");
            if (userId instanceof Long id) {
                return id;
            }
            if (userId instanceof Integer id) {
                return id.longValue();
            }
            if (userId instanceof String id) {
                try {
                    return Long.parseLong(id);
                } catch (NumberFormatException ignored) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private LocalDateTime toLocalDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalDateTime();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }
}

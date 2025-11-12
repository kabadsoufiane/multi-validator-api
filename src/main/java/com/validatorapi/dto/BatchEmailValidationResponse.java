package com.validatorapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BatchEmailValidationResponse {

    private Integer total;
    private Integer valid;
    private Integer invalid;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    @JsonProperty("checked_at")
    private LocalDateTime checkedAt;

    private List<Map<String, Object>> results;
}
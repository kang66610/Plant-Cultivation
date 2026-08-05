package com.plantcultivation.controller;

import com.plantcultivation.vo.ResultVO;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public ResponseEntity<ResultVO<Map<String, Object>>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("time", LocalDateTime.now().toString());

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            data.put("status", "UP");
            data.put("database", "UP");
            return ResponseEntity.ok(ResultVO.success(data));
        } catch (Exception e) {
            data.put("status", "DOWN");
            data.put("database", "DOWN");
            ResultVO<Map<String, Object>> result = ResultVO.error(503, "health check failed");
            result.setData(data);
            return ResponseEntity.status(503).body(result);
        }
    }
}

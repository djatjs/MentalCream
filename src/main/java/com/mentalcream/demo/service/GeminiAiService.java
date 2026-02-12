package com.mentalcream.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.domain.DoneItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    @Value("${gemini.api.key:YOUR_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 사용자님 환경에서 검증된 v1beta 및 gemini-2.5-flash 경로 유지
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public Map<String, String> generatePersonalizedSuggestion(Category category, DailyLog dailyLog, List<DoneItem> items) {
        if ("YOUR_API_KEY".equals(apiKey) || apiKey == null) {
            log.warn("Gemini API 키가 설정되지 않았습니다. 기본 추천을 제공합니다.");
            return getFallbackSuggestion(category);
        }

        String doneListStr = items.stream()
                .map(item -> "- " + item.getTitle() + "(" + item.getCategory() + ")")
                .collect(Collectors.joining("\n"));

        // 보습 컨셉 상담사 페르소나 적용 프롬프트 (v3: 고민 레이어 기반)
        String prompt = String.format("""
                너는 멘탈 관리 앱 '멘탈크림'의 따뜻한 상담사야. 
                사용자의 오늘 상태(기분:%d, 에너지:%d, 고민강도:%d/5)와 고민 내용("%s")을 분석해줘.
                또한 사용자가 오늘 완료한 일들(%s)과 메모(%s)도 참고해.
                
                이 사용자의 마음 장벽을 보호하기 위해 '%s' 카테고리에 맞는 '작은 행동 1개'를 추천해야 해.
                고민 강도가 높다면(4 이상) 부담 없는 15분 이내의 활동을, 평온하다면 활기찬 도전을 제안해줘.
                말투는 '보습제'를 바르는 것처럼 부드럽고 따뜻하게!
                
                응답은 반드시 JSON 구조로만 답변해:
                {
                  "title": "행동 제목",
                  "reason": "고민에 대한 따뜻한 공감과 이 활동이 마음을 어떻게 보습해주는지 설명 (100자 이내)"
                }
                """,
                dailyLog.getMood(), dailyLog.getEnergy(), 
                dailyLog.getWorryIntensity() != null ? dailyLog.getWorryIntensity() : 0,
                dailyLog.getWorryText() != null ? dailyLog.getWorryText() : "없음",
                doneListStr.isEmpty() ? "없음" : doneListStr,
                dailyLog.getNote() != null ? dailyLog.getNote() : "없음",
                category.name()
        );

        try {
            return callGeminiApi(prompt);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            return getFallbackSuggestion(category);
        }
    }

    private Map<String, String> callGeminiApi(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(Map.of("text", prompt)));
        requestBody.put("contents", List.of(content));

        // 엄격한 JSON 스키마 설정 유지 (2.5 버전 호환)
        requestBody.put("generationConfig", Map.of(
            "response_mime_type", "application/json",
            "response_schema", Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                    "title", Map.of("type", "STRING"),
                    "reason", Map.of("type", "STRING")
                ),
                "required", List.of("title", "reason")
            )
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String response = restTemplate.postForObject(GEMINI_API_URL + apiKey, entity, String.class);

        JsonNode root = objectMapper.readTree(response);
        String rawText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        
        return objectMapper.readValue(rawText, Map.class);
    }

    private Map<String, String> getFallbackSuggestion(Category category) {
        Map<String, String> fallback = new HashMap<>();
        fallback.put("title", "잠시 숨 고르기");
        fallback.put("reason", "지금은 많은 분들이 보습을 받고 있어 답변이 조금 늦어지고 있네요. 잠시 눈을 감고 깊게 숨을 들이마셔 볼까요?");
        return fallback;
    }
}

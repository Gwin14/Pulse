package com.pulse.infrastructure.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulse.domain.model.SalesAnalysis;
import com.pulse.domain.model.SalesData;
import com.pulse.domain.model.SalesMetric;
import com.pulse.domain.port.AiAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class OpenRouterAiAnalysisAdapter implements AiAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenRouterAiAnalysisAdapter.class);
    
    @Value("${ai.model:}")
    private String model;
    
    @Value("${ai.apiKey:}")
    private String apiKey;
    
    @Value("${ai.openrouter.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SalesAnalysis analyzeWithAi(SalesData salesData) {
        try {
            String prompt = buildAnalysisPrompt(salesData);
            String aiResponse = callOpenRouter(prompt);
            return parseAiResponse(aiResponse);
        } catch (Exception e) {
            logger.error("Erro na análise com IA: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na comunicação com serviço de IA", e);
        }
    }

    private String buildAnalysisPrompt(SalesData salesData) throws Exception {
        Map<String, Object> normalizedData = Map.of(
            "timezone", salesData.getTimezone(),
            "periodStart", salesData.getPeriodStart().toString(),
            "periodEnd", salesData.getPeriodEnd().toString(),
            "metrics", salesData.getMetrics().stream()
                .map(this::convertMetricToMap)
                .toList()
        );

        String jsonData = objectMapper.writeValueAsString(normalizedData);
        logger.info("Dados normalizados para IA: {}", jsonData);

        return """
            Você é especialista em análise de movimentação financeira e comportamento de vendas.
            Receba os dados abaixo e determine o melhor dia da semana para lançar promoções.

            Retorne tudo em português, seguindo as seguintes regras:

            Regras:
            1) Responder APENAS em JSON válido.
            2) Datas em ISO 8601 (YYYY-MM-DD).
            3) Campos obrigatórios:
               - best_day_date: data no formato YYYY-MM-DD
               - best_day_weekday: dia da semana em inglês (Monday, Tuesday, etc.)
               - score: número entre 0.0 e 1.0
               - reason: texto explicativo (máximo 200 caracteres)
               - volatility: número entre 0.0 e 1.0
               - insights: array com 2-5 itens, cada um com máximo 100 caracteres

            Critérios de análise:
            - Receita total por dia
            - Número de transações
            - Ticket médio
            - Padrões por dia da semana
            - Consistência histórica
            - Volatilidade das vendas

            Dados para análise:
            %s
            """.formatted(jsonData);
    }

    private Map<String, Object> convertMetricToMap(SalesMetric metric) {
        return Map.of(
            "date", metric.getDate().toString(),
            "revenue", metric.getRevenue(),
            "transactions", metric.getTransactions(),
            "ticket", metric.getTicket()
        );
    }

    private String callOpenRouter(String prompt) throws Exception {
        List<Map<String, String>> messages = List.of(
            Map.of("role", "user", "content", prompt)
        );

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", messages,
            "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            openRouterUrl, requestEntity, String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new RuntimeException("Erro na resposta da API: " + responseEntity.getStatusCode());
        }

        JsonNode responseRoot = objectMapper.readTree(responseEntity.getBody());
        JsonNode contentNode = responseRoot.at("/choices/0/message/content");
        
        if (contentNode == null || contentNode.isMissingNode()) {
            throw new RuntimeException("Resposta inválida da API");
        }

        return contentNode.asText();
    }

    private SalesAnalysis parseAiResponse(String jsonResponse) throws Exception {
        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        
        LocalDate bestDayDate = LocalDate.parse(responseNode.path("best_day_date").asText());
        DayOfWeek bestDayWeekday = DayOfWeek.valueOf(responseNode.path("best_day_weekday").asText().toUpperCase());
        double score = responseNode.path("score").asDouble();
        String reason = responseNode.path("reason").asText();
        double volatility = responseNode.path("volatility").asDouble();
        
        List<String> insights = new java.util.ArrayList<>();
        if (responseNode.has("insights") && responseNode.get("insights").isArray()) {
            responseNode.get("insights").forEach(insightNode -> 
                insights.add(insightNode.asText()));
        }

        return new SalesAnalysis(bestDayDate, bestDayWeekday, score, reason, volatility, insights);
    }
} 
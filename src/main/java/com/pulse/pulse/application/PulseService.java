package com.pulse.pulse.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulse.pulse.domain.InputData;
import com.pulse.pulse.domain.Metric;
import com.pulse.pulse.domain.PulseResponse;

@Service
public class PulseService {

    @Value("${ai.provider:gemini}")
    private String aiProvider;

    @Value("${ai.apiKey:}")
    private String apiKey;

    @Value("${ai.model:}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate http = new RestTemplate();

    public PulseResponse analyzeData(InputData input) {
        input.getMetrics().forEach(m -> {
            double t = m.getTransactions() <= 0 ? 0.0 : m.getRevenue() / m.getTransactions();
            m.setTicket(t);
        });

        Optional<PulseResponse> viaIa = callAi(input);
        return viaIa.orElseGet(() -> fallbackHeuristic(input));
    }

    private Optional<PulseResponse> callAi(InputData input) {
        try {
            if (apiKey == null || apiKey.isBlank()) return Optional.empty();

            String prompt = buildPrompt(input);

            if ("openrouter".equalsIgnoreCase(aiProvider)) {
                return Optional.ofNullable(callOpenRouter(prompt));
            } else {
                return Optional.ofNullable(callGemini(prompt));
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private PulseResponse callOpenRouter(String prompt) throws Exception {
        String useModel = model == null || model.isBlank() ? "google/gemini-2.0-flash-thinking-exp:free" : model;

        Map<String, Object> body = Map.of(
                "model", useModel,
                "input", prompt,
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, h);

        ResponseEntity<String> res = http.postForEntity("https://openrouter.ai/api/v1/chat/completions", req, String.class);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) return null;

        JsonNode root = mapper.readTree(res.getBody());
        JsonNode content = root.at("/choices/0/message/content");
        if (content == null || content.isMissingNode()) return null;

        return parseAiJson(content.asText());
    }

    private PulseResponse callGemini(String prompt) throws Exception {
        String useModel = model == null || model.isBlank() ? "gemini-1.5-flash" : model;

        Map<String, Object> body = Map.of(
                "model", useModel,
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-goog-api-key", apiKey);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, h);

        ResponseEntity<String> res = http.postForEntity("https://generativelanguage.googleapis.com/v1beta/models/" + useModel + ":generateContent", req, String.class);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) return null;

        JsonNode root = mapper.readTree(res.getBody());
        JsonNode text = root.at("/candidates/0/content/parts/0/text");
        if (text == null || text.isMissingNode()) return null;

        return parseAiJson(text.asText());
    }

    private String buildPrompt(InputData input) throws Exception {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("timezone", input.getTimezone());
        normalized.put("period_start", input.getPeriod_start());
        normalized.put("period_end", input.getPeriod_end());
        normalized.put("metrics", input.getMetrics());

        String jsonData = mapper.writeValueAsString(normalized);

        return """
                Você é especialista em análise de movimentação financeira e comportamento de vendas.
                Receba os dados abaixo e determine o melhor dia da semana para lançar promoções.

                Regras:
                1) Responder APENAS em JSON válido.
                2) Datas em ISO 8601.
                3) Campos: best_day_date (YYYY-MM-DD), best_day_weekday, score [0..1], reason (<=200 chars), volatility [0..1], insights (2-5 itens, cada <=100 chars).

                Critérios:
                - Receita total por dia, número de transações, ticket médio.
                - Padrões por dia da semana e consistência histórica.
                - Volatilidade das vendas.

                Dados:
                %s
                """.formatted(jsonData);
    }

    private PulseResponse parseAiJson(String jsonText) throws Exception {
        JsonNode n = mapper.readTree(jsonText);
        PulseResponse r = new PulseResponse();
        r.setBest_day_date(n.path("best_day_date").asText(null));
        r.setBest_day_weekday(n.path("best_day_weekday").asText(null));
        r.setScore(n.path("score").asDouble(0.0));
        r.setReason(n.path("reason").asText(null));
        r.setVolatility(n.path("volatility").asDouble(0.0));

        List<String> insights = new ArrayList<>();
        if (n.has("insights") && n.get("insights").isArray()) {
            n.get("insights").forEach(i -> insights.add(i.asText()));
        }
        r.setInsights(insights);
        return r;
    }

    private PulseResponse fallbackHeuristic(InputData input) {
        Map<DayOfWeek, List<Metric>> byDow = input.getMetrics().stream()
                .collect(Collectors.groupingBy(m -> LocalDate.parse(m.getDate()).getDayOfWeek()));

        DayOfWeek bestDow = null;
        double bestAvg = Double.NEGATIVE_INFINITY;

        for (Map.Entry<DayOfWeek, List<Metric>> e : byDow.entrySet()) {
            double avg = e.getValue().stream().mapToDouble(Metric::getRevenue).average().orElse(0.0);
            if (avg > bestAvg) {
                bestAvg = avg;
                bestDow = e.getKey();
            }
        }

        String bestDate = input.getMetrics().stream()
                .max(Comparator.comparingDouble(Metric::getRevenue))
                .map(Metric::getDate).orElse(null);

        PulseResponse r = new PulseResponse();
        r.setBest_day_date(bestDate);
        r.setBest_day_weekday(bestDow != null ? bestDow.toString() : null);
        r.setScore(0.55);
        r.setReason("Heurística local por média de receita");
        r.setVolatility(calcVolatility(input.getMetrics()));
        r.setInsights(List.of(
                "Média por dia da semana priorizada",
                "Sem IA externa disponível"
        ));
        return r;
    }

    private double calcVolatility(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) return 0.0;
        double mean = metrics.stream().mapToDouble(Metric::getRevenue).average().orElse(0.0);
        if (mean == 0.0) return 0.0;
        double var = metrics.stream().mapToDouble(m -> Math.pow(m.getRevenue() - mean, 2)).average().orElse(0.0);
        double std = Math.sqrt(var);
        double v = Math.min(1.0, std / (mean * 2.0));
        return v;
    }
}

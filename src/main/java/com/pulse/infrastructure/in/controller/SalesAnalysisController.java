package com.pulse.infrastructure.in.controller;

import com.pulse.application.service.SalesAnalysisUseCase;
import com.pulse.infrastructure.in.dto.SalesAnalysisRequest;
import com.pulse.infrastructure.in.mapper.SalesDataMapper;
import com.pulse.infrastructure.out.dto.SalesAnalysisResponse;
import com.pulse.infrastructure.out.mapper.SalesAnalysisResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pulse")
@Tag(name = "Sales Analysis", description = "API para análise de dados de vendas e identificação do melhor dia para promoções")
public class SalesAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesAnalysisController.class);
    
    private final SalesAnalysisUseCase salesAnalysisUseCase;
    private final SalesDataMapper salesDataMapper;
    private final SalesAnalysisResponseMapper responseMapper;

    public SalesAnalysisController(SalesAnalysisUseCase salesAnalysisUseCase,
                                 SalesDataMapper salesDataMapper,
                                 SalesAnalysisResponseMapper responseMapper) {
        this.salesAnalysisUseCase = salesAnalysisUseCase;
        this.salesDataMapper = salesDataMapper;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/analyze")
    @Operation(
        summary = "Analisar dados de vendas",
        description = "Analisa dados de vendas para determinar o melhor dia da semana para lançar promoções"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Análise realizada com sucesso",
            content = @Content(schema = @Schema(implementation = SalesAnalysisResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Dados de entrada inválidos"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Erro interno do servidor"
        )
    })
    public ResponseEntity<SalesAnalysisResponse> analyze(@Valid @RequestBody SalesAnalysisRequest request) {
        try {
            logger.info("Recebida requisição de análise para período: {} a {}", 
                       request.getPeriod_start(), request.getPeriod_end());
            
            var salesData = salesDataMapper.toDomain(request);
            var analysis = salesAnalysisUseCase.execute(salesData);
            var response = responseMapper.toResponse(analysis);
            
            logger.info("Análise concluída com sucesso. Melhor dia: {}", response.getBest_day_date());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados de entrada inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Erro inesperado durante análise: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 
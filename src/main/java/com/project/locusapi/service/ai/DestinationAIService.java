package com.project.locusapi.service.ai;

import com.project.locusapi.dto.destination.DestinationAIResponse;
import com.project.locusapi.dto.destination.TouristPointDTO;
import com.project.locusapi.exception.business.DestinationAIException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DestinationAIService {

    private static final int REQUIRED_TOURIST_POINTS = 5;

    private static final String SYSTEM_PROMPT = """
            Você é um guia turístico especialista e uma API de dados geográficos de alta precisão.
            Sua tarefa é receber o nome de um destino (cidade) e retornar exatamente 5 pontos turísticos imperdíveis e famosos dessa localidade.

            Restrições estritas de comportamento:
            1. Responda APENAS com um objeto JSON válido.
            2. Não inclua tags de bloco de código markdown, como ```json.
            3. Não inclua comentários, explicações ou qualquer texto fora do JSON.
            4. Todo o conteúdo descritivo deve ser gerado em Português do Brasil (pt-BR).

            Estrutura esperada:
            {
              "destino": "Nome da Cidade",
              "pais": "Nome do País",
              "pontosTuristicos": [
                {
                  "nome": "Nome",
                  "descricao": "2 a 3 frases cativantes.",
                  "categoria": "Ex: Museu, Parque"
                }
              ]
            }
            """;

    private final ChatClient chatClient;

    public DestinationAIService(ChatClient customChatClient) {
        this.chatClient = customChatClient;
    }

    public DestinationAIResponse recommendTouristPoints(String city) {
        String normalizedCity = normalizeCity(city);

        try {
            DestinationAIResponse response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user("Retorne exatamente 5 pontos turísticos para o destino: %s.".formatted(normalizedCity))
                    .call()
                    .entity(DestinationAIResponse.class);

            return validate(response);
        } catch (DestinationAIException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DestinationAIException("Não foi possível gerar recomendações turísticas no momento.");
        }
    }

    private String normalizeCity(String city) {
        if (!StringUtils.hasText(city)) {
            throw new IllegalArgumentException("O parâmetro 'city' é obrigatório.");
        }
        return city.trim();
    }

    private DestinationAIResponse validate(DestinationAIResponse response) {
        if (response == null || !StringUtils.hasText(response.destino()) || !StringUtils.hasText(response.pais())) {
            throw new DestinationAIException("A IA retornou uma resposta de destino incompleta.");
        }

        List<TouristPointDTO> touristPoints = response.pontosTuristicos();
        if (touristPoints == null || touristPoints.size() != REQUIRED_TOURIST_POINTS) {
            throw new DestinationAIException("A IA não retornou exatamente 5 pontos turísticos.");
        }

        boolean hasInvalidPoint = touristPoints.stream().anyMatch(point -> point == null
                || !StringUtils.hasText(point.nome())
                || !StringUtils.hasText(point.descricao())
                || !StringUtils.hasText(point.categoria()));

        if (hasInvalidPoint) {
            throw new DestinationAIException("A IA retornou pontos turísticos com campos obrigatórios ausentes.");
        }

        return response;
    }
}

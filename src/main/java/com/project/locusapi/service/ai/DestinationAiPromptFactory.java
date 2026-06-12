package com.project.locusapi.service.ai;

import org.springframework.stereotype.Component;

@Component
public class DestinationAiPromptFactory {

    public String touristPointsSystemPrompt() {
        return """
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
    }

    public String touristPointsUserPrompt(String city) {
        return "Retorne exatamente 5 pontos turísticos para o destino: %s.".formatted(city);
    }
}

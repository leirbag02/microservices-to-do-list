package com.example.demo;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Collections;

class Main {

    public static void main(String[] args) {
        // Substitua pelos valores reais do seu arquivo de configuração ou variáveis de ambiente
        String apiKey = "DFTVeuSykfUZWUsJDUhGpWfIavzyXg5bYwM1gTBcFisuB0UPAooPJQQJ99ALACHYHv6XJ3w3AAAAACOGVPYa";
        String endpoint = "https://ai-habilopes2144ai450298114938.openai.azure.com/";
        String deploymentName = "gpt-4o-mini";

        // Criação do cliente Azure OpenAI
        OpenAIClient openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();

        // Mensagem para o chat
        String userMessage = "Como está o clima hoje?";

        // Criando uma mensagem no formato de chat usando ChatRequestMessage
        ChatRequestMessage chatRequestMessage = new ChatRequestMessage();

        // Criando opções para a requisição
        ChatCompletionsOptions chatOptions = new ChatCompletionsOptions(Collections.singletonList(chatRequestMessage));
        chatOptions.setMaxTokens(100); // Definindo o número máximo de tokens

        try {
            // Fazendo a chamada à API para obter as respostas
            ChatCompletions chatCompletions = openAIClient.getChatCompletions(deploymentName, chatOptions);

            // Verificando se a resposta contém escolhas
            if (chatCompletions.getChoices() == null || chatCompletions.getChoices().isEmpty()) {
                throw new IllegalStateException("A resposta do Azure OpenAI não contém escolhas.");
            }

            // Exibindo o texto da primeira escolha
            System.out.println("Resposta: " + chatCompletions.getChoices().get(0).getMessage().getContent().trim());
        } catch (Exception e) {
            System.err.println("Erro ao chamar o Azure OpenAI: " + e.getMessage());
        }
    }
}

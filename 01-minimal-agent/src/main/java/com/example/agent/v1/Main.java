package com.example.agent.v1;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;

// PASO 1: agente sin tools.
// El modelo responde solo con su conocimiento de entrenamiento.
// No tiene acceso al repositorio ni puede leer archivos.
// Ejecutar: mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v1.Main
public class Main {

    static final String SYSTEM_PROMPT = """
            Eres un revisor de código senior.
            Analiza el diff recibido y da feedback constructivo y conciso.
            """;

    public static void main(String[] args) {
        var model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-6")
                .build();

        // Esto es todo: interfaz + builder + build(). LangChain4j genera el loop.
        CodeReviewAgent agent = AiServices.builder(CodeReviewAgent.class)
                .chatLanguageModel(model)
                .systemMessageProvider(id -> SYSTEM_PROMPT)
                .build();

        String diff = """
                - return result;
                + return Optional.ofNullable(result);
                """;

        System.out.println(agent.reviewPullRequest(diff));
    }
}

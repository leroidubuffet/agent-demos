package com.example.agent.v3;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;

// PASO 3: dos tools. El agente puede explorar el directorio
// para encontrar la definición de TaskNotFoundException sin que
// el revisor le diga la ruta exacta.
// Ejecutar: mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v3.Main
public class Main {

    static final String SYSTEM_PROMPT = """
            Eres un revisor de código senior.
            Tienes acceso al repositorio: puedes listar directorios y leer archivos.
            Si el diff hace referencia a un tipo que no conoces, búscalo antes de opinar.
            Limita la exploración a lo estrictamente necesario para la revisión.
            """;

    public static void main(String[] args) {
        var model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-6")
                .build();

        CodeReviewAgent agent = AiServices.builder(CodeReviewAgent.class)
                .chatLanguageModel(model)
                .tools(new GitTools())
                .systemMessageProvider(id -> SYSTEM_PROMPT)
                .build();

        String diff = """
                diff --git a/src/main/java/com/example/TaskService.java
                --- a/src/main/java/com/example/TaskService.java
                +++ b/src/main/java/com/example/TaskService.java
                @@ -12 +12,2 @@
                -    return taskRepository.findById(id);
                +    return taskRepository.findById(id)
                +           .orElseThrow(() -> new TaskNotFoundException(id));
                """;

        System.out.println(agent.reviewPullRequest(diff));
    }
}

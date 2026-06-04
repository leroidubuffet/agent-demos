package com.example.agent.v2;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;

// PASO 2: se añade .tools(new GitTools()).
// El agente puede leer archivos del repo para añadir contexto a la revisión.
// El diff menciona TaskNotFoundException: el agente puede decidir buscarla.
// Ejecutar: mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v2.Main
public class Main {

    static final String SYSTEM_PROMPT = """
            Eres un revisor de código senior.
            Tienes acceso al repositorio y puedes leer archivos para entender
            el contexto antes de revisar. Úsalo cuando el diff haga referencia
            a tipos o métodos definidos en otros archivos.
            """;

    public static void main(String[] args) {
        var model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-6")
                .build();

        CodeReviewAgent agent = AiServices.builder(CodeReviewAgent.class)
                .chatModel(model)
                .tools(new GitTools())           // ← la única diferencia con v1
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

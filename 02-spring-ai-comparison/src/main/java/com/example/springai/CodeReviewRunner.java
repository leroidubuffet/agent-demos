package com.example.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Comparativa con LangChain4j (v3/Main.java):
//
// LangChain4j:                          Spring AI:
// AiServices.builder(Agent.class)       ChatClient.Builder (inyectado)
//   .chatModel(model)                     .defaultSystem(...)
//   .tools(new GitTools())                .defaultTools(tools)  ← bean Spring
//   .systemMessageProvider(...)           .build()
//   .build()
//
// El resultado: misma funcionalidad, pero las tools pueden usar
// el ecosistema Spring completo (JPA, Security, Actuator...).
@Component
public class CodeReviewRunner implements ApplicationRunner {

    private final ChatClient chatClient;

    public CodeReviewRunner(ChatClient.Builder builder, CodeReviewTools tools) {
        this.chatClient = builder
                .defaultSystem("""
                        Eres un revisor de código senior.
                        Tienes acceso al repositorio: puedes listar directorios y leer archivos.
                        Si el diff hace referencia a un tipo que no conoces, búscalo antes de opinar.
                        """)
                .defaultTools(tools)
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        String diff = """
                diff --git a/src/main/java/com/example/TaskService.java
                --- a/src/main/java/com/example/TaskService.java
                +++ b/src/main/java/com/example/TaskService.java
                @@ -12 +12,2 @@
                -    return taskRepository.findById(id);
                +    return taskRepository.findById(id)
                +           .orElseThrow(() -> new TaskNotFoundException(id));
                """;

        String review = chatClient.prompt()
                .user("Revisa este diff:\n" + diff)
                .call()
                .content();

        System.out.println(review);
    }
}

package com.example.antipatterns.bad;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// ANTI-PATRÓN: descripciones vagas + sin manejo de error.
// Compara cada @Tool con su versión corregida en DescriptiveToolsAgent.java.
public class VagueDescriptionsAgent {

    interface ReviewAgent {
        String review(String instructions);
    }

    static class BadTools {

        // MAL: "procesa el archivo" no le dice al modelo cuándo usarla.
        // El modelo la invoca al azar o nunca.
        @Tool("procesa el archivo")
        String processFile(@P("archivo") String path) throws IOException {
            // MAL: lanza IOException cruda. El agente entra en bucle de reintentos
            // cada vez peores porque no sabe qué hacer con la excepción.
            return Files.readString(Path.of(path));
        }

        // MAL: "hace cosas con git" es igual de inútil.
        @Tool("hace cosas con git")
        String gitStuff() throws IOException {
            var proc = Runtime.getRuntime().exec(new String[]{"git", "log", "--oneline", "-5"});
            return new String(proc.getInputStream().readAllBytes());
        }

        // MAL: sin límite de iteraciones. Si gitStuff() devuelve siempre el mismo
        // error, el agente puede iterar indefinidamente.
        // Solución: devolver mensajes de error útiles + usar un contador externo.
    }

    public static void main(String[] args) {
        var model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-6")
                .build();

        ReviewAgent agent = AiServices.builder(ReviewAgent.class)
                .chatModel(model)
                .tools(new BadTools())
                .build();

        // Observa en la traza que el modelo no sabe cuándo usar processFile
        // ni gitStuff. Las llamadas son erráticas.
        System.out.println(agent.review("Revisa el archivo TaskService.java."));
    }
}

package com.example.antipatterns.good;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// SOLUCIÓN a los anti-patrones de VagueDescriptionsAgent:
// descripciones concretas + manejo de error + límite de llamadas.
public class DescriptiveToolsAgent {

    interface ReviewAgent {
        String review(String instructions);
    }

    static class SafeTools {

        private int callCount = 0;
        private static final int MAX_TOOL_CALLS = 10;

        // BIEN: el nombre es preciso, la descripción enumera cuándo usarla.
        @Tool("Lee el contenido completo de un archivo del repositorio. " +
              "Usar cuando necesites ver el código de una clase o método antes de comentarlo. " +
              "No usar para directorios.")
        String readFile(@P("ruta relativa a la raíz del repo, ej: src/main/java/com/example/TaskService.java") String path) {
            if (++callCount > MAX_TOOL_CALLS) {
                // Límite de iteraciones: el agente recibe un mensaje claro y puede detenerse.
                return "Límite de llamadas alcanzado (" + MAX_TOOL_CALLS + "). Resume con la información que tienes.";
            }
            try {
                return Files.readString(Path.of(path));
            } catch (IOException e) {
                // Mensaje de error útil: el modelo puede decidir probar otra ruta.
                return "No se puede leer '" + path + "': " + e.getMessage() + ". Prueba listar el directorio primero.";
            }
        }

        // BIEN: nombre y descripción no se solapan con readFile.
        @Tool("Obtiene las últimas N líneas del log de git con formato compacto. " +
              "Usar cuando necesites saber qué cambios se han hecho recientemente en el repo.")
        String gitLog(@P("número de commits a mostrar, típicamente 5-10") int count) {
            if (++callCount > MAX_TOOL_CALLS) {
                return "Límite de llamadas alcanzado. Resume con la información que tienes.";
            }
            try {
                var proc = Runtime.getRuntime().exec(new String[]{"git", "log", "--oneline", "-" + count});
                String output = new String(proc.getInputStream().readAllBytes());
                return output.isBlank() ? "No hay commits en este repositorio." : output;
            } catch (IOException e) {
                return "git no disponible: " + e.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        var model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-6")
                .build();

        ReviewAgent agent = AiServices.builder(ReviewAgent.class)
                .chatModel(model)
                .tools(new SafeTools())
                .systemMessageProvider(id -> "Eres un revisor de código senior con acceso al repositorio.")
                .build();

        // Observa en la traza que el modelo invoca readFile con la ruta correcta
        // y gitLog con un número de commits razonable, sin vacilar.
        System.out.println(agent.review("Revisa el archivo TaskService.java."));
    }
}

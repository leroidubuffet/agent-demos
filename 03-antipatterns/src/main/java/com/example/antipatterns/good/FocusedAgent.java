package com.example.antipatterns.good;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;

// SOLUCIÓN al anti-patrón de demasiadas tools:
// las 15 tools de TooManyToolsAgent se reparten en dos agentes especializados.
// Cada uno tiene un objetivo único y un conjunto de tools mínimo.
public class FocusedAgent {

    // Agente 1: solo lee y ejecuta. No escribe, no hace push, no toca Jira.
    interface CodeReviewAgent {
        String reviewPullRequest(String diff);
    }

    static class ReadOnlyTools {
        @Tool("Lee el contenido completo de un archivo. " +
              "Usar cuando necesites ver código existente antes de comentarlo.")
        String readFile(@P("ruta relativa a la raíz del repo") String path) {
            return "contenido simulado de " + path;
        }

        @Tool("Lista los archivos de un directorio (no recursivo). " +
              "Usar cuando necesites saber qué archivos existen antes de leerlos.")
        String listFiles(@P("ruta del directorio") String dir) {
            return "TaskService.java\nTaskRepository.java\nTaskNotFoundException.java";
        }

        @Tool("Ejecuta los tests y devuelve la salida completa. " +
              "Usar al final para verificar que los cambios revisados no rompen nada.")
        String runTests() {
            return "Tests: 42 passed, 0 failed.";
        }
    }

    // Agente 2: solo documenta y crea issues. No lee código arbitrario ni ejecuta tests.
    interface DocumentationAgent {
        String documentChanges(String summary);
    }

    static class DocumentationTools {
        @Tool("Crea un issue en Jira con el título y descripción dados. " +
              "Usar cuando el revisor haya identificado un problema que requiere seguimiento.")
        String createJiraIssue(@P("título del issue") String title, @P("descripción") String desc) {
            return "PROJ-" + (int)(Math.random() * 1000);
        }

        @Tool("Escribe el contenido dado en un archivo Markdown. " +
              "Usar solo para generar documentación, nunca para modificar código fuente.")
        String writeMarkdown(@P("ruta del archivo .md") String path, @P("contenido Markdown") String content) {
            return "ok";
        }
    }

    public static void main(String[] args) {
        var model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-6")
                .build();

        // Dos agentes con 3 tools cada uno en lugar de uno con 15.
        // Cada modelo tiene un espacio de decisión más pequeño y más claro.
        CodeReviewAgent reviewer = AiServices.builder(CodeReviewAgent.class)
                .chatModel(model)
                .tools(new ReadOnlyTools())
                .systemMessageProvider(id -> "Eres un revisor de código. Tu trabajo es leer, ejecutar tests y opinar. No escribes ni haces commits.")
                .build();

        DocumentationAgent documenter = AiServices.builder(DocumentationAgent.class)
                .chatModel(model)
                .tools(new DocumentationTools())
                .systemMessageProvider(id -> "Eres un documentador. Recibes el resumen de una revisión y creas los artefactos necesarios.")
                .build();

        String diff = "- return result;\n+ return Optional.ofNullable(result);";
        String review = reviewer.reviewPullRequest(diff);
        System.out.println("Revisión:\n" + review);

        String docResult = documenter.documentChanges(review);
        System.out.println("Documentación:\n" + docResult);
    }
}

package com.example.antipatterns.bad;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;

// ANTI-PATRÓN: un agente con 15 tools.
// Lo que ocurre en la traza: el modelo tarda, vacila entre herramientas
// similares (readFile vs getFileContent), y elige mal.
// Solución en FocusedAgent.java: descomponer en dos agentes especializados.
public class TooManyToolsAgent {

    interface DevAgent {
        String doTask(String instructions);
    }

    // 15 tools para un solo agente:
    // filesystem (5) + git (5) + jira/email/db (5) = espacio de decisión enorme
    static class AllTheTools {

        @Tool("Lee un archivo")
        String readFile(@P("ruta") String path) { return "contenido de " + path; }

        @Tool("Obtiene el contenido de un archivo")              // ← ambigua, similar a readFile
        String getFileContent(@P("ruta") String path) { return "contenido de " + path; }

        @Tool("Escribe contenido en un archivo")
        String writeFile(@P("ruta") String path, @P("contenido") String content) { return "ok"; }

        @Tool("Lista los archivos de un directorio")
        String listFiles(@P("directorio") String dir) { return "archivo1.java\narchivo2.java"; }

        @Tool("Elimina un archivo")
        String deleteFile(@P("ruta") String path) { return "eliminado"; }

        @Tool("Ejecuta los tests del proyecto")
        String runTests() { return "BUILD SUCCESS"; }

        @Tool("Compila el proyecto")
        String compile() { return "BUILD SUCCESS"; }

        @Tool("Lee el log de git")
        String gitLog() { return "commit abc123..."; }

        @Tool("Muestra el diff de git")
        String gitDiff() { return "diff --git a/..."; }

        @Tool("Hace commit con el mensaje dado")
        String gitCommit(@P("mensaje") String message) { return "ok"; }

        @Tool("Hace push al remoto")
        String gitPush() { return "ok"; }

        @Tool("Crea un issue en Jira")
        String createJiraIssue(@P("título") String title, @P("descripción") String desc) { return "PROJ-123"; }

        @Tool("Lee un issue de Jira")
        String getJiraIssue(@P("id del issue") String id) { return "issue: " + id; }

        @Tool("Manda un email")
        String sendEmail(@P("destinatario") String to, @P("asunto") String subject, @P("cuerpo") String body) { return "enviado"; }

        @Tool("Consulta la base de datos")
        String queryDatabase(@P("query SQL") String sql) { return "resultados"; }
    }

    public static void main(String[] args) {
        var model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-6")
                .build();

        // 15 tools en un solo agente: observa en la traza cuánto tarda
        // el modelo en decidir cuál usar y con qué frecuencia confunde
        // readFile con getFileContent.
        DevAgent agent = AiServices.builder(DevAgent.class)
                .chatLanguageModel(model)
                .tools(new AllTheTools())
                .build();

        System.out.println(agent.doTask("Lee el archivo TaskService.java y luego ejecuta los tests."));
    }
}

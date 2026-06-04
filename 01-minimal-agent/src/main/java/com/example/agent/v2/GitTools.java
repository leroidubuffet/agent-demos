package com.example.agent.v2;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class GitTools {

    // La descripción enumera CUÁNDO usar la tool, no qué hace por dentro.
    // Esto es lo que el modelo lee para decidir si invocarla.
    @Tool("Lee el contenido completo de un archivo. " +
          "Usar cuando necesites ver código existente antes de comentarlo o referenciarlo.")
    String readFile(@P("Ruta relativa a la raíz del repo") String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            // Las tools deben devolver mensajes útiles, no lanzar excepciones crudas.
            return "Error al leer '" + path + "': " + e.getMessage();
        }
    }
}

package com.example.agent.v3;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// PASO 3: dos tools. El agente puede explorar el directorio
// y después leer los archivos relevantes. El sistema prompt
// le indica que liste primero si no sabe dónde está un tipo.
class GitTools {

    @Tool("Lee el contenido completo de un archivo. " +
          "Usar cuando necesites ver código existente antes de comentarlo o referenciarlo.")
    String readFile(@P("Ruta relativa a la raíz del repo") String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return "Error al leer '" + path + "': " + e.getMessage();
        }
    }

    @Tool("Lista los archivos de un directorio (no recursivo). " +
          "Usar cuando necesites saber qué archivos existen antes de leerlos.")
    String listFiles(@P("Ruta del directorio relativa a la raíz del repo") String directory) {
        try (Stream<Path> paths = Files.list(Path.of(directory))) {
            return paths.map(Path::toString)
                        .sorted()
                        .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Error al listar '" + directory + "': " + e.getMessage();
        }
    }
}

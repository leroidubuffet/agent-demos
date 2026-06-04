package com.example.springai;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// En Spring AI las tools son beans de Spring: pueden usar @Autowired,
// acceder a repositorios JPA, etc. Es la diferencia clave con LangChain4j.
@Component
public class CodeReviewTools {

    @Tool("Lee el contenido completo de un archivo. " +
          "Usar cuando necesites ver código existente antes de comentarlo.")
    public String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return "Error al leer '" + path + "': " + e.getMessage();
        }
    }

    @Tool("Lista los archivos de un directorio (no recursivo). " +
          "Usar cuando necesites saber qué archivos existen antes de leerlos.")
    public String listFiles(String directory) {
        try (Stream<Path> paths = Files.list(Path.of(directory))) {
            return paths.map(Path::toString).sorted().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Error al listar '" + directory + "': " + e.getMessage();
        }
    }
}

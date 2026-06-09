package com.example.springai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CodeReviewTools.class);

    @Tool(description = "Lee el contenido completo de un archivo. " +
          "Usar cuando necesites ver código existente antes de comentarlo.")
    public String readFile(String path) {
        log.debug("Tool call → readFile(\"{}\")", path);
        try {
            String result = Files.readString(Path.of(path));
            log.debug("Tool result ← readFile: {} chars", result.length());
            return result;
        } catch (IOException e) {
            String error = "Error al leer '" + path + "': " + e.getMessage();
            log.debug("Tool result ← readFile: {}", error);
            return error;
        }
    }

    @Tool(description = "Lista los archivos de un directorio (no recursivo). " +
          "Usar cuando necesites saber qué archivos existen antes de leerlos.")
    public String listFiles(String directory) {
        log.debug("Tool call → listFiles(\"{}\")", directory);
        try (Stream<Path> paths = Files.list(Path.of(directory))) {
            String result = paths.map(Path::toString).sorted().collect(Collectors.joining("\n"));
            log.debug("Tool result ← listFiles: {}", result);
            return result;
        } catch (IOException e) {
            String error = "Error al listar '" + directory + "': " + e.getMessage();
            log.debug("Tool result ← listFiles: {}", error);
            return error;
        }
    }
}

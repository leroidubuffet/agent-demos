# agent-demos

Ejemplos de agentes Java con LangChain4j y Spring AI para el curso
**"IA generativa en el desarrollo de software"**.

## Requisitos

- Java 21
- Maven 3.9+
- Variable de entorno `ANTHROPIC_API_KEY` con una clave válida de Anthropic

## Módulos

### `01-minimal-agent` — Construcción incremental de un agente

Tres versiones del mismo agente de revisión de código, cada una añade una pieza:

| Versión | Qué añade |
|---------|-----------|
| `v1` | Interfaz + `AiServices.builder()`. Sin tools: el modelo responde solo con su entrenamiento. |
| `v2` | `@Tool readFile`. El agente puede leer archivos del repositorio antes de opinar. |
| `v3` | `@Tool listFiles`. El agente puede explorar un directorio para encontrar archivos sin que se los indiques. |

```bash
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v1.Main
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v2.Main
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v3.Main
```

### `02-spring-ai-comparison` — El mismo agente con Spring AI

Equivalente funcional de `v3` usando Spring Boot y Spring AI en lugar de LangChain4j.
Las tools son beans de Spring (`@Component`) inyectados en el `ChatClient`.

```bash
cd 02-spring-ai-comparison
ANTHROPIC_API_KEY=sk-ant-... mvn spring-boot:run
```

> Este módulo tiene su propio `pom.xml` con Spring Boot como parent y no forma
> parte del proyecto Maven raíz.

### `03-antipatterns` — Patrones problemáticos y sus correcciones

Cuatro archivos en dos carpetas:

| Archivo | Problema que ilustra |
|---------|----------------------|
| `bad/TooManyToolsAgent.java` | Un agente con 15 tools: el modelo vacila y elige mal. |
| `bad/VagueDescriptionsAgent.java` | Descripciones vagas y sin manejo de errores. |
| `good/FocusedAgent.java` | Las 15 tools repartidas en dos agentes especializados de 3 tools cada uno. |
| `good/DescriptiveToolsAgent.java` | Descripciones precisas, error handling y límite de llamadas. |

```bash
mvn -pl 03-antipatterns exec:java -Dexec.mainClass=com.example.antipatterns.bad.TooManyToolsAgent
mvn -pl 03-antipatterns exec:java -Dexec.mainClass=com.example.antipatterns.good.FocusedAgent
```

## Configuración

```bash
export ANTHROPIC_API_KEY=sk-ant-...
mvn install   # compila 01-minimal-agent y 03-antipatterns
```

La versión de LangChain4j se controla desde la propiedad `langchain4j.version`
en el `pom.xml` raíz.

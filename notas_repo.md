# Cómo mostrar agent-demos en clase

## Antes de empezar

> No es necesario si no vamos a ejecutar.

```bash
git clone https://github.com/leroidubuffet/agent-demos
cd agent-demos
export ANTHROPIC_API_KEY=sk-ant-...
mvn install -q
```

Abre el proyecto en el editor. Tendrás las carpetas `01-minimal-agent`, `02-spring-ai-comparison` y `03-antipatterns` visibles en el árbol. La forma más rápida de ir a los archivos es usar el buscador.

---

## Parte 1 — Construcción incremental del agente (`01-minimal-agent`)

El objetivo es mostrar que un agente en LangChain4j se construye en tres piezas. Cada paso añade una sola cosa.

### Paso 1 — La interfaz y el builder (v1)

Abre `01-minimal-agent/src/main/java/com/example/agent/v1/CodeReviewAgent.java`. Di: *"esto es el contrato del agente. Solo una interfaz Java. LangChain4j genera la implementación que gestiona el loop completo."*

Abre `01-minimal-agent/src/main/java/com/example/agent/v1/Main.java`. Señala el bloque del builder:

```java
AiServices.builder(CodeReviewAgent.class)
    .chatLanguageModel(model)
    .systemMessageProvider(id -> SYSTEM_PROMPT)
    .build();
```

Di: *"sin tools, el agente solo puede responder con lo que sabe de entrenamiento."* Aquí le damos un diff hardcodeado mínimo. No tiene acceso al repositorio ni puede leer archivos todavía.

Ejecuta:

```bash
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v1.Main
```

El agente responde al diff. No hay traza de tool calls porque no tiene herramientas.

---

### Paso 2 — Primera tool: `readFile` (v2)

Abre `01-minimal-agent/src/main/java/com/example/agent/v2/GitTools.java`. Señala la anotación `@Tool` y lee la descripción en voz alta: *"Lee el contenido completo de un archivo. Usar cuando necesites ver código existente antes de comentarlo o referenciarlo."* Di: *"esta descripción es lo que el modelo lee para decidir si invocar la herramienta. Como ya sabemos, una descripción vaga produce invocaciones incorrectas."*

Abre `01-minimal-agent/src/main/java/com/example/agent/v2/Main.java`. Señala la única línea añadida respecto a v1:

```java
.tools(new GitTools())
```

Esto es todo lo que cambia en el código del cliente. Permite al modelo llamar a `readFile` para leer archivos reales del repo y buscar contexto adicional.

En la práctica el flujo es:

1. Se pasa el diff hardcodeado al agente.
2. El modelo ve que el diff menciona `TaskNotFoundException`.
3. Decide invocar `readFile("...TaskNotFoundException.java")` para ver si esa clase existe y entender el contexto.
4. Con ese contexto añadido, genera el feedback.

Ejecuta:

```bash
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v2.Main
```

En la traza aparece una llamada a `readFile`. El agente lee `TaskService.java` para ver el código que se está modificando, y probablemente también `TaskNotFoundException.java` para verificar que la clase existe antes de opinar. Di: *"el loop tool-use que vimos en el slide 10 está ocurriendo aquí, gestionado por LangChain4j."*

---

### Paso 3 — Segunda tool: `listFiles` (v3)

Abre `01-minimal-agent/src/main/java/com/example/agent/v3/GitTools.java`. Señala el nuevo método `listFiles`. Di: *"ahora el agente puede explorar el directorio antes de leer un archivo, sin que nadie le diga la ruta exacta."*

Ejecuta:

```bash
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v3.Main
```

En la traza aparece primero `listFiles`, luego `readFile`. Di: *"nadie le dijo que listara primero. Decidió por su cuenta basándose en las descripciones de las tools y en el system prompt, que le indica que explore si no conoce una ruta."*

---

## Parte 2 — Comparativa Spring AI (`02-spring-ai-comparison`)

Abre `02-spring-ai-comparison/src/main/java/com/example/springai/CodeReviewTools.java`. Señala `@Component`. Di: *"en Spring AI las tools son beans de Spring. Pueden usar `@Autowired`, acceder a JPA, a Security, a cualquier parte del ecosistema Spring."*

Abre `02-spring-ai-comparison/src/main/java/com/example/springai/CodeReviewRunner.java` y muestra el comentario al inicio del archivo. Lee las dos columnas en voz alta:

```
LangChain4j:                          Spring AI:

AiServices.builder(...)               ChatClient.Builder (inyectado)
  .chatLanguageModel(model)             .defaultSystem(...)
  .tools(new GitTools())                .defaultTools(tools)  ← bean Spring
  .systemMessageProvider(...)           .build()
  .build()
```

Di: *"misma funcionalidad. La elección depende del proyecto: si ya está en Spring Boot, Spring AI. Si no, LangChain4j."*

Ejecutar este módulo es opcional. Si lo haces:

```bash
cd 02-spring-ai-comparison
ANTHROPIC_API_KEY=sk-ant-... mvn spring-boot:run
```

---

## Parte 3 — Anti-patrones (`03-antipatterns`)

### Anti-patrón 1: demasiadas tools

Pon en pantalla `bad/TooManyToolsAgent.java` y `good/FocusedAgent.java` en paralelo (pantalla partida o dos ventanas del editor).

**En `bad/`:** cuenta las tools en voz alta mientras scrolleas: `readFile`, `getFileContent` (similar a la anterior), `writeFile`, `listFiles`, `deleteFile`, `runTests`, `compile`, `gitLog`, `gitDiff`, `gitCommit`, `gitPush`, `createJiraIssue`, `getJiraIssue`, `sendEmail`, `queryDatabase`. Di: *"quince tools. El modelo tiene que elegir entre quince opciones en cada paso. Fíjate además en `readFile` y `getFileContent`: son casi idénticas. El modelo no sabe cuál usar."*

**En `good/`:** muestra que las 15 tools se reparten en dos agentes especializados: el revisor tiene 3 tools (`readFile`, `listFiles`, `runTests`) y el documentador tiene 2 (`createJiraIssue`, `writeMarkdown`). Di: *"cada agente tiene un objetivo único. El revisor solo lee y ejecuta tests; no escribe ni toca Jira. El espacio de decisión es radicalmente más pequeño."*

Desde la raíz del proyecto, ejecuta los dos y compara las trazas:

```bash
mvn -pl 03-antipatterns exec:java -Dexec.mainClass=com.example.antipatterns.bad.TooManyToolsAgent
```

```bash
mvn -pl 03-antipatterns exec:java -Dexec.mainClass=com.example.antipatterns.good.FocusedAgent
```

| | `TooManyToolsAgent` | `FocusedAgent` |
|---|---|---|
| Nº de agentes | 1 | 2 |
| Tools por agente | 15 | 3 (revisor) / 2 (documentador) |
| Tools duplicadas | `readFile` + `getFileContent` (hacen lo mismo) | Sin solapamiento |
| System prompt | Ninguno | Uno por agente, con rol explícito |
| Tiempo | 22 s | 69 s |
| Calidad del output | Superficial: "leí el archivo y los tests pasan" | Análisis detallado + documentación en Jira/Markdown |
| Por qué | El modelo completa la tarea mecánicamente y para | El rol fuerza al modelo a opinar, no solo a ejecutar |

**Lección clave:** el tiempo extra del `FocusedAgent` no es un coste, es el modelo trabajando de verdad. El `TooManyToolsAgent` fue más rápido porque hizo menos. Un espacio de decisión grande y sin rol definido produce respuestas mínimas; roles acotados producen respuestas de calidad.

---

### Anti-patrón 2: descripciones vagas y sin manejo de error

Abre `bad/VagueDescriptionsAgent.java`. Lee las descripciones en voz alta:

- `@Tool("procesa el archivo")` — pregunta al grupo: *"¿cuándo usaría el modelo esta herramienta?"*
- `@Tool("hace cosas con git")` — ídem

Señala que `processFile` lanza `IOException` sin capturar. Di: *"cuando una tool lanza una excepción cruda, el agente entra en un bucle de reintentos cada vez peores porque no sabe qué hacer con el error."*

Abre `good/DescriptiveToolsAgent.java`. Lee la descripción de `readFile`:

> "Lee el contenido completo de un archivo del repositorio. Usar cuando necesites ver el código de una clase o método antes de comentarlo. No usar para directorios."

Señala tres cosas:

1. La descripción enumera cuándo usar la tool y cuándo no.
2. El ejemplo de ruta en el `@P` ayuda al modelo a formatear el argumento correctamente.
3. El bloque `try/catch` devuelve un mensaje útil: `"No se puede leer '...'. Prueba listar el directorio primero."` El modelo puede reaccionar a eso.

Señala el contador `MAX_TOOL_CALLS = 10`. Di: *"este es el workaround práctico para el anti-patrón de bucle infinito. Si el agente llega a diez llamadas, recibe un mensaje claro y puede detenerse."*

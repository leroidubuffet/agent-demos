# agent-demos — Módulo 6, slides 25-28

Demostraciones de agentes Java con LangChain4j y Spring AI.
Cada módulo corresponde a un slide del curso.

## Prerrequisitos

- Java 21
- Maven 3.9+
- `ANTHROPIC_API_KEY` en el entorno

## Estructura

```
01-minimal-agent/     ← slide 26: construcción incremental en tres pasos
  v1/                 paso 1: solo interfaz + AiServices.builder()
  v2/                 paso 2: + una @Tool con readFile
  v3/                 paso 3: + listFiles (dos tools)

02-spring-ai-comparison/  ← slide 27: mismo agente, estilo Spring Boot
  (proyecto independiente con Spring Boot como parent)

03-antipatterns/          ← slide 28: código malo vs código corregido
  bad/TooManyToolsAgent       15 tools en un agente
  bad/VagueDescriptionsAgent  descripciones vagas + sin manejo de error
  good/FocusedAgent           dos agentes especializados con 3 tools cada uno
  good/DescriptiveToolsAgent  descripciones precisas + error handling + límite de llamadas
```

## Ejecutar

```bash
export ANTHROPIC_API_KEY=sk-ant-...

# Paso 1: agente sin tools
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v1.Main

# Paso 2: + readFile
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v2.Main

# Paso 3: + listFiles
mvn -pl 01-minimal-agent exec:java -Dexec.mainClass=com.example.agent.v3.Main

# Anti-patrón: demasiadas tools
mvn -pl 03-antipatterns exec:java -Dexec.mainClass=com.example.antipatterns.bad.TooManyToolsAgent

# Solución: agentes especializados
mvn -pl 03-antipatterns exec:java -Dexec.mainClass=com.example.antipatterns.good.FocusedAgent

# Spring AI (proyecto independiente)
cd 02-spring-ai-comparison && mvn spring-boot:run
```

## Guión de clase recomendado

1. Ejecutar `v1.Main` — el agente responde pero no sabe nada del repo
2. Ejecutar `v2.Main` — el agente llama a `readFile`; mostrar la traza
3. Ejecutar `v3.Main` — el agente usa `listFiles` antes de `readFile`
4. Abrir `bad/TooManyToolsAgent.java` y `good/FocusedAgent.java` en paralelo — el contraste visual hace el punto
5. Ejecutar ambos y comparar las trazas — TooManyTools vacila, FocusedAgent va directo
6. Abrir `bad/VagueDescriptionsAgent.java` y `good/DescriptiveToolsAgent.java` — leer las descripciones en voz alta

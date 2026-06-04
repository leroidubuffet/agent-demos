package com.example.agent.v3;

// PASO 3 — contrato idéntico, dos tools en GitTools (readFile + listFiles).
// El agente puede explorar el directorio antes de leer un archivo.
interface CodeReviewAgent {
    String reviewPullRequest(String diff);
}

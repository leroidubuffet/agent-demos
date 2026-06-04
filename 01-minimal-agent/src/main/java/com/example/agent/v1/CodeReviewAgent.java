package com.example.agent.v1;

// PASO 1 — solo el contrato.
// AiServices genera la implementación que orquesta el loop completo.
interface CodeReviewAgent {
    String reviewPullRequest(String diff);
}

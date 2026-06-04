package com.example.agent.v2;

// PASO 2 — el contrato no cambia al añadir tools.
// La diferencia está en el Main: se añade .tools(new GitTools()).
interface CodeReviewAgent {
    String reviewPullRequest(String diff);
}

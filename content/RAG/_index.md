---
title: "RAG & Chatbots"
description: "Udvikling af vores egen chatbot med RAG (Retrieval-Augmented Generation) og vores egen knowledge base."
---

# Udforskning af RAG til vores egen chatbot

Vi har arbejdet på at skabe vores egen chatbot ved hjælp af Retrieval-Augmented Generation (RAG) baseret på vores egen vidensbase. For at finde den bedste løsning har vi afprøvet tre forskellige værktøjer og platforme:

## 1. ChatGPT's Indbyggede RAG (Custom GPT)
Vi startede med at bygge en **Custom GPT** direkte gennem OpenAI's interface.
- **Beskrivelse:** Gør det muligt at uploade egne dokumenter og filer direkte, hvorefter ChatGPT automatisk søger i dataen for at understøtte sine svar.
- **Styrker:** Det er utroligt hurtigt at sætte op og kræver ingen kodning. Det er en fantastisk måde at prototype og teste på.
- **Oplevelse:** Perfekt til intern brug og for hurtigt at mærke "magien" ved RAG, men begrænset i forhold til integrationer på egne hjemmesider.

## 2. CustomGPT.ai
For at få en mere dedikeret og skræddersyet løsning testede vi **CustomGPT.ai**.
- **Beskrivelse:** En platform skræddersyet udelukkende til at bygge RAG-løsninger. Den indekserer ens hjemmeside, dokumenter og data, og genererer en chatbot.
- **Styrker:** Leverer meget præcise svar baseret *kun* på den data man fodrer den med, og den er god til at fremhæve kildehenvisninger.
- **Oplevelse:** Ideel som en "plug-and-play" erhvervsløsning, der nemt kan indlejres (embeddes) på en hjemmeside, uden at hallucinationer bliver et problem.

## 3. Dify.ai
Til sidst kiggede vi på **Dify.ai**, som giver meget mere frihed og kontrol.
- **Beskrivelse:** En open-source platform til at udvikle LLM-applikationer. Her kan man designe hele sit eget workflow grafisk.
- **Styrker:** Vi får fuld kontrol over hele RAG-pipelinen (hvordan tekster deles op - chunking, hvilke embeddings der bruges, og avancerede prompts).
- **Oplevelse:** Det bedste værktøj når man vil have dyb kontrol over arkitekturen og lave komplekse flows eller interagere dybt med eksterne API'er.


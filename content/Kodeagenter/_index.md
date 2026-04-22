---
title: "AI-baserede Kodeagenter"
description: "Introduktion til kodeagenter som Claude Code og OpenAI Codex – og hvordan vi brugte dem til at løse rigtige opgaver i undervisningen."
---


Kodeagenter er AI-systemer, der ikke bare besvarer spørgsmål, men aktivt kan skrive, køre, debugge og refaktorere kode. I stedet for at fungere som et avanceret autocomplete arbejder de som en samarbejdspartner, der forstår et helt projekts kontekst og kan tage beslutninger på tværs af filer.

## Hvad er en kodeagent?

En kodeagent kombinerer en stor sprogmodel med evnen til at:

- **Læse og skrive filer** direkte i projektet
- **Køre kommandoer** i terminalen og reagere på output
- **Søge i kodebasen** for at forstå arkitektur og afhængigheder
- **Iterere selvstændigt** – fejler noget, prøver agenten igen

Det adskiller sig fra et simpelt chatvindue, fordi agenten opererer i dit faktiske udviklingsmiljø frem for i en isoleret sandbox.

## De agenter vi arbejdede med

### Claude Code (Anthropic)

Claude Code kører direkte i terminalen og har adgang til hele arbejdsbiblioteket. Den er stærk til at forstå store kodebaser og forklare sine valg undervejs. Den er særligt god til refactoring og arkitekturspørgsmål, fordi den holder et overblik over sammenhænge på tværs af filer.

### OpenAI Codex

Codex er OpenAI's kodespecialiserede model og grundlaget for GitHub Copilot. Den er hurtig til at generere kodeblokke og fungerer godt til velafgrænsede opgaver. Vi brugte den via CLI-interfacet til at sammenligne dens output direkte med Claude's.

## Hvad vi lavede i undervisningen

### Trin 1: Introduktion og sammenligning

Vi startede med en gennemgang af, hvad kodeagenter overhovedet er, og hvordan Claude og Codex adskiller sig i tilgang og arkitektur. Hvornår er den ene bedre end den anden? Hvad koster det at køre dem? Og hvad betyder det egentlig, at en model er "agent" frem for blot "assistent"?

### Trin 2: Cupcake-projektet

Som skolen har et eksisterende **cupcake-projekt** – en webapplikation vi kender fra tidligere undervisning – brugte vi det som benchmark. Vi bad først **Claude Code** om at implementere projektet fra bunden og observerede, hvordan den navigerede opgaven: hvilke filer den oprettede, hvilke valg den traf om struktur, og hvor den bad om afklaring.

Derefter gentog vi øvelsen med **Codex** og løste den samme opgave.

**Sammenligning af resultaterne:**

| Kriterium | Claude Code | Codex |
|---|---|---|
| Kodestruktur | Organiseret i lag, fulgte MVC | Mere direkte, færre lag |
| Forklaringer undervejs | Detaljerede kommentarer | Kortere, mere kodecentreret |
| Fejlhåndtering | Proaktiv – tilføjede validering selv | Tilføjede det vi bad om |
| Samlet indtryk | God til komplekse arkitekturbeslutninger | Hurtig til afgrænsede opgaver |

Det var tydeligt, at de to agenter har forskellig "stil" – Claude tenderer mod at tænke i helheden, mens Codex er mere direkte og effektiv til velspecificerede delproblemer.

### Trin 3: Meditationsopgaven

Vores lærer stillede os en selvstændig opgave: vi skulle vælge **én kodeagent** og bruge den til at bygge en **quiz** inspireret af en eksisterende meditationshjemmeside – i hvert vores helt nye projekt. Formålet var at undersøge, hvordan det egentlig er at arbejde med en agent på en opgave man ikke selv har specificeret til mindste detalje.

Jeg valgte **Claude Code** og brugte den til at bygge quizzen fra bunden i et nyt projekt. Erfaringerne:

- **Hvad gik godt:** Agenten hjalp med at strukturere applikationen og foreslog fornuftige løsninger, jeg ikke selv havde tænkt på. Den var god til at holde styr på sammenhængen, efterhånden som projektet voksede.
- **Hvad var udfordrende:** Det krævede præcise prompts. Var opgaven for åben, tog agenten beslutninger, der ikke altid passede til mine intentioner – og så skulle jeg bruge tid på at korrigere kursen.
- **Konklusion:** Kodeagenter er stærke redskaber, men de kræver, at man som udvikler stadig tager ansvar for arkitekturen og retningen. Agenten er en produktiv samarbejdspartner – ikke en erstatning for at tænke selv.

## Samarbejde mellem udvikler og agent

Den vigtigste lektie fra undervisningen var, at den bedste brug af kodeagenter ikke er at "bare lade den køre". Det effektive mønster er:

1. **Udvikler definerer mål og afgrænsning** – hvad skal løses, og hvad er out of scope?
2. **Agent genererer et forslag** – kode, arkitektur, tests
3. **Udvikler reviewer og godkender** – forstår du koden, inden du accepterer den?
4. **Iteration** – stil opfølgende spørgsmål, bed om alternativer, juster

Kodeagenter fremskynder det tekniske arbejde markant, men den faglige vurdering forbliver udviklerens ansvar.

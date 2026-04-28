---
title: "LLM API-integration"
description: "Hvordan man kalder et eksternt LLM-API og bruger sprogmodellen til konkrete opgaver i sin applikation."
---

En stor sprogmodel (LLM) behøver ikke bo lokalt i din applikation. De fleste udbydere – Anthropic, OpenAI, Google m.fl. – stiller deres modeller til rådighed via et HTTP API, som du kan kalde fra næsten ethvert programmeringssprog. Det betyder, at du kan tilføje AI-funktioner til en eksisterende applikation uden at håndtere modelhosting selv.

## Hvad er et LLM-API?

Et LLM-API er en REST-tjeneste, der modtager en besked (en *prompt*) og returnerer modellens svar som JSON. Konceptuelt ligner det et hvilket som helst andet web-API:

1. Du sender en HTTP POST-request med din prompt og eventuelle parametre.
2. Serveren sender prompten videre til modellen.
3. Du modtager svaret og bruger det i din applikation.

Det kræver som regel en API-nøgle til autentifikation, og du betaler per token (omtrent per ord) du sender og modtager.

## Eksempel: Anthropic Claude API

Her er et minimalt Python-eksempel med Anthropics officielle SDK:

```python
import anthropic

client = anthropic.Anthropic(api_key="din-api-nøgle")

response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=1024,
    messages=[
        {"role": "user", "content": "Forklar hvad et REST API er i to sætninger."}
    ]
)

print(response.content[0].text)
```

De vigtigste parametre:

| Parameter | Beskrivelse |
|---|---|
| `model` | Hvilken model du vil bruge (bestemmer pris og kvalitet) |
| `max_tokens` | Det maksimale antal tokens i svaret |
| `messages` | Samtalens historik – mindst én `user`-besked |
| `system` | (valgfrit) En systeminstruktion der sætter modellens adfærd |

### Systemprompt: sæt konteksten

Med en `system`-parameter kan du fortælle modellen, hvilken rolle den skal spille:

```python
response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=1024,
    system="Du er en hjælpsom kundeservicemedarbejder for en dansk netbutik. Svar altid på dansk og hold tonen venlig og kort.",
    messages=[
        {"role": "user", "content": "Hvornår ankommer min pakke?"}
    ]
)
```

Systemprompten styrer tone, sprog og begrænsninger uden at det er synligt for brugeren.

## Hvad kan du bruge det til?

API-adgang åbner op for at bygge AI-funktioner direkte ind i dine egne applikationer:

### Tekstgenerering og -opsummering
Send et langt dokument og bed modellen om et kort resumé. Nyttigt til nyheder, rapporter eller mødereferater.

```python
tekst = "... [langt dokument] ..."

response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=300,
    messages=[
        {"role": "user", "content": f"Opsummer følgende tekst i tre punkter:\n\n{tekst}"}
    ]
)
```

### Klassifikation og tagging
Bed modellen om at kategorisere input – f.eks. om en kundemail er en klage, et spørgsmål eller en ros.

```python
mail = "Jeg har ventet på min ordre i tre uger uden nogen opdatering!"

response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=50,
    system="Klassificer kundemails som: KLAGE, SPØRGSMÅL eller ROS. Svar kun med ét af disse ord.",
    messages=[
        {"role": "user", "content": mail}
    ]
)

kategori = response.content[0].text.strip()  # → "KLAGE"
```

### Flertrins samtale (chat)
Du kan opbygge en hel samtalehistorik ved at sende alle tidligere beskeder med i hvert kald:

```python
historik = []

def send_besked(bruger_besked):
    historik.append({"role": "user", "content": bruger_besked})

    response = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=1024,
        system="Du er en hjælpsom assistent.",
        messages=historik
    )

    svar = response.content[0].text
    historik.append({"role": "assistant", "content": svar})
    return svar

print(send_besked("Hvad er Python?"))
print(send_besked("Kan du give et eksempel?"))  # Modellen husker konteksten
```

## Struktureret output

Ofte vil du have modellens svar i et bestemt format, f.eks. JSON, så du kan behandle det programmatisk. Det gøres ved at bede om det direkte i prompten:

```python
response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=200,
    messages=[
        {
            "role": "user",
            "content": (
                "Udtræk navn, by og alder fra denne tekst og returner som JSON:\n"
                "\"Maria Hansen bor i Aarhus og er 34 år gammel.\""
            )
        }
    ]
)

import json
data = json.loads(response.content[0].text)
# → {"navn": "Maria Hansen", "by": "Aarhus", "alder": 34}
```

## Praktiske hensyn

**Omkostninger:** Du betaler per token. Brug `max_tokens` til at begrænse udgifterne, og hold systemprompts korte.

**Latency:** Et API-kald tager typisk 0,5–3 sekunder. Til realtidsapplikationer kan du bruge *streaming*, så svar vises løbende mens modellen genererer dem.

**Sikkerhed:** Gem aldrig API-nøglen direkte i koden – brug miljøvariabler (`os.environ["ANTHROPIC_API_KEY"]`) eller en secrets manager.

**Fejlhåndtering:** API'er kan returnere fejl (rate limits, netværksfejl). Implementér retry-logik og fald-back-beskeder til brugeren.

## Opsummering

At kalde et LLM-API er ikke mere kompliceret end at kalde et hvhert andet web-API. De tre kerneelementer er:

- En **systemprompt** der definerer modellens rolle og begrænsninger
- En **brugerbesked** med det konkrete input
- Parsing af **svaret** til brug i din applikation

Derfra er mulighederne brede: opsummering, klassifikation, oversættelse, kodegenering, chatbots – alt hvad der kan beskrives som et sprogligt input/output-problem kan løses med et LLM-API.

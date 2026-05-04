---
title: "LLM API-implementering"
description: "Hvordan jeg har implementeret kald til en lokal LLM via mit eget FastAPI og håndterer svaret i min applikation."
---

## Valget: lokal LLM frem for eksternt API

Til dette projekt valgte jeg at køre min sprogmodel lokalt via **Ollama** frem for at kalde et eksternt API som OpenAI eller Anthropic. Grunden er simpel: et eksternt API koster penge per token, og da jeg primært bruger dette til udvikling og læring, ville omkostningerne hurtigt løbe op.

Jeg er godt klar over, at en lokal model som `llama3.2` ikke er på niveau med f.eks. GPT-4 eller Claude – svarene er generelt mindre nuancerede, og modellen klarer komplekse ræsonnementer dårligere. Det er en bevidst afvejning jeg ofrer noget svarkvalitet for til gengæld at kunne eksperimentere frit uden at tænke på udgifter.

## Arkitekturen

Jeg har bygget et lille **FastAPI**-lag, der sidder imellem min applikation og den lokale Ollama-server. Det giver mig ét samlet sted at styre hvilken model der bruges, hvilken systemprompt der sendes med, og hvordan fejl håndteres.

```
Klient  →  FastAPI (/ask)  →  Ollama (localhost:11434)  →  llama3.2
                ↑
        systemprompt fra .md-fil
```

## Implementeringen

Endpointet `/ask` modtager en prompt og et valgfrit modelnavn, læser systemprompt fra en ekstern markdown-fil og videresender det hele til Ollamas `/api/generate`:

```python
@app.post("/ask")
def ask_local_llm(request: PromptRequest):
    url = "http://localhost:11434/api/generate"

    system_prompt = ""
    try:
        with open("prompts/praktik_system_prompt.md", "r", encoding="utf-8") as f:
            system_prompt = f.read()
    except FileNotFoundError:
        print("Advarsel: Kunne ikke finde system prompt filen.")

    payload = {
        "model": request.model,   # standard: "llama3.2"
        "prompt": request.prompt,
        "system": system_prompt,
        "stream": False
    }

    response = requests.post(url, json=payload)
    response.raise_for_status()

    data = response.json()
    return {"response": data.get("response", "Intet svar fundet i JSON")}
```

Ollama returnerer et JSON-objekt, og jeg trækker svaret ud via `data.get("response")`. Hvis Ollama ikke er tilgængelig, kastes en `HTTPException` med en beskrivende fejlbesked.

## Systemprompt fra fil

I stedet for at hardcode systempromptens indhold direkte i koden læser jeg den fra en separat `.md`-fil. Det gør det nemt at justere modellens rolle og adfærd uden at ændre i Python-koden – man redigerer blot filen og genstarter endpointet.

## Hvad jeg ville have gjort anderledes med et eksternt API

Havde jeg valgt et eksternt API, ville flowet have været næsten identisk – forskellen er primært:

- **Autentifikation:** en API-nøgle i en miljøvariabel i stedet for ingen autentifikation til localhost.
- **Svarformat:** OpenAI og Anthropic bruger `choices[0].message.content` frem for `response`.
- **Svarqualitet:** markant bedre, særligt på komplekse eller flersprogede opgaver.

Arkitekturen med et FastAPI-lag imellem gør det let at skifte fra lokal til ekstern model på et senere tidspunkt – man ændrer blot URL og payload-format et sted.

---
title: "LLM + Eksternt API"
description: "To cases på hvordan man kan integrere et LLM i en eksisterende applikation via eksternt API – et mailsystem og en analyse af skolens chatbots."
---

# LLM i eksisterende applikationer

En af de mest praktiske anvendelser af store sprogmodeller er ikke at bygge noget helt nyt, men at **sætte dem ind i systemer der allerede eksisterer**. Frem for at erstatte kendte workflows, tilføjer LLM'en et lag af forståelse og analyse, som ellers ville kræve manuel menneskelig vurdering.

I dette forløb arbejdede vi med to cases, der begge illustrerer det mønster – men som også viste, at integration i praksis bringer udfordringer med sig, man ikke altid ser i teorien.

---

## Case 1: Mailsystem med automatisk prioritering

### Idéen

Mange organisationer modtager store mængder e-mail, og det er tidskrævende at afgøre, hvad der kræver handling nu. Idéen var at bygge et system, der:

1. **Lytter på indkommende mails**
2. **Bruger et LLM til at vurdere**, om mailen er vigtig eller ej
3. **Opretter automatisk en todo**, hvis mailen kræver handling

Flowet ville se sådan ud:

```
Ny mail ankommer
     │
     ▼
LLM analyserer indhold
     │
     ├─ Vigtig → Opret todo-opgave
     │
     └─ Ikke vigtig → Ignorer / arkiver
```

### Teknisk tilgang

Modellen ville få mailens emne og brødtekst som input og returnere en struktureret vurdering:

```python
response = client.messages.create(
    model="claude-opus-4-7",
    max_tokens=256,
    system=(
        "Du er en assistent, der vurderer om en e-mail kræver handling. "
        "Svar udelukkende med JSON: {\"vigtig\": true/false, \"grund\": \"...\", \"todo\": \"...\"}"
    ),
    messages=[
        {"role": "user", "content": f"Emne: {subject}\n\n{body}"}
    ]
)
```

Hvis `vigtig` er `true`, ville systemet kalde et eksternt opgave-API (fx Todoist eller Microsoft To Do) og oprette en opgave med den formulering, modellen foreslår.

### Udfordringerne – GDPR og datahåndtering

Det var her, det for alvor blev komplekst. E-mails indeholder **personoplysninger**, og det rejste en række problemer:

- **Databehandleraftale**: Sender man mailindhold til en ekstern LLM-udbyder (Anthropic, OpenAI osv.), er man forpligtet til at have en databehandleraftale på plads – og sikre sig, at data ikke bruges til træning.
- **Formål og lovhjemmel**: GDPR kræver, at der er et klart og legitimt formål med behandlingen. "Vi vil gerne sortere mails med AI" er ikke i sig selv tilstrækkeligt – der skal være et juridisk grundlag.
- **Minimering**: Man bør kun sende det nødvendige til modellen – ikke hele mailkæden med historik, CC-adresser og vedhæftede filer.
- **Gennemsigtighed**: Afsendere bør informeres om, at deres mails behandles automatisk af AI.

Disse udfordringer betød, at systemet i sin fulde form var svært at implementere ansvarligt uden en grundig juridisk og organisatorisk afklaring først. Det er et godt eksempel på, at **teknisk muligt ≠ lovligt eller etisk forsvarligt**.

### Hvad vi lærte

GDPR er ikke en detalje, man tilføjer til sidst. Det er en designbegrænsning, der skal tænkes ind fra starten. Maildelen viste, at LLM-integration i systemer med personoplysninger kræver et langt større forarbejde, end selve koden gør.

---

## Case 2: Analyse af skolens chatbots

### Baggrunden

På skolen har alle semestre på datamatikeruddannelsen adgang til en chatbot bygget med **CustomGPT.ai**. Botten er specialiseret til hvert semesters indhold og hjælper studerende med faglige spørgsmål.

Problemet var: **vi vidste ikke, hvad der faktisk blev spurgt om**. Hvilke emner stillede de studerende mest spørgsmål til? Hvad forstod de ikke godt nok? Og kunne svarene blive bedre?

### Løsningen

CustomGPT.ai stiller et API til rådighed, der giver adgang til alle forespørgsler (prompts) der er sendt til botten. Vi brugte det API til at hente historik og sendte det videre til et LLM, der skulle:

- **Kategorisere** spørgsmålene i emner
- **Identificere mønstre** – hvad spørges der gentagne gange om?
- **Foreslå forbedringer** til bottens svar, så de bliver mere afklarende

### Arkitektur

```
CustomGPT.ai API
      │
      │  GET /conversations  (alle forespørgsler)
      ▼
Python-script henter og strukturerer data
      │
      ▼
LLM analyserer batch af spørgsmål
      │
      ├─ Kategorier & temaer
      ├─ Hyppigt stillede spørgsmål
      └─ Forslag til svarforbedringer
      │
      ▼
Visuel rapport (grafer, ordsky, tabel)
```

### Datahentning fra CustomGPT.ai

```python
import requests

CUSTOMGPT_API_KEY = "..."
PROJECT_ID = "..."

def fetch_conversations() -> list[dict]:
    url = f"https://app.customgpt.ai/api/v1/projects/{PROJECT_ID}/conversations"
    headers = {"Authorization": f"Bearer {CUSTOMGPT_API_KEY}"}

    all_messages = []
    page = 1

    while True:
        response = requests.get(url, headers=headers, params={"page": page})
        data = response.json()
        messages = data.get("data", {}).get("data", [])
        if not messages:
            break
        all_messages.extend(messages)
        page += 1

    return all_messages
```

### LLM-analyse af spørgsmålene

Spørgsmålene sendes i batches til modellen med en struktureret prompt:

```python
def analyse_questions(questions: list[str]) -> dict:
    joined = "\n".join(f"- {q}" for q in questions)

    response = client.messages.create(
        model="claude-opus-4-7",
        max_tokens=1024,
        system=(
            "Du er en uddannelsesanalytiker. Du får en liste af spørgsmål stillet til en faglig chatbot. "
            "Returner JSON med: {\"kategorier\": [...], \"hyppige_temaer\": [...], \"forbedringsforslag\": [...]}"
        ),
        messages=[
            {"role": "user", "content": f"Spørgsmål:\n{joined}"}
        ]
    )

    return json.loads(response.content[0].text)
```

### Det visuelle output

Resultatet af analysen blev visualiseret, så det var nemt at handle på:

- **Søjlediagram** over de mest stillede emner (matplotlib)
- **Tabel** med konkrete forbedringsforslag til bottens svar
- **Ordsky** over de hyppigste nøgleord i spørgsmålene

Dette gav et konkret billede af, hvad de studerende kæmpede med, og hvilke dele af pensum der trængte til bedre forklaringer i bottens vidensbase.

### Hvad vi lærte

Kombinationen af et produktions-API og et LLM til analyse er meget kraftfuldt, fordi man **ikke selv skal kode kategorilogikken**. Modellen klarer klassificeringen og mønstergenkendelsen – man behøver bare at stille det rigtige spørgsmål i prompten.

Det interessante var også, at analysen ikke bare beskrev problemet, men pegede direkte på **handlinger**: hvad skal skrives om i vidensbasen, hvilke svar er uklare, og hvilke emner mangler dækning.

---

## Sammenligning af de to cases

| | Mailsystem | Chatbot-analyse |
|---|---|---|
| Datakilde | Indkommende e-mails | CustomGPT.ai API |
| LLM's rolle | Vurdere og handle (real-time) | Analysere og kategorisere (batch) |
| Output | Automatisk todo-oprettelse | Visuel rapport til undervisere |
| Største udfordring | GDPR og personoplysninger | Promptdesign og datakvalitet |
| Status | Udskudt pga. juridiske krav | Gennemført |

## Konklusion

Begge cases viser det samme grundmønster: **et eksternt API leverer data, og et LLM giver det mening**. Men de viser også, at integration i den virkelige verden kræver mere end en god idé og et par API-kald. GDPR, databehandleraftaler og organisatorisk kontekst er ikke efterskrifter – de er en del af designet.

Den største takeaway er, at LLM'er er bedst som et **fortolkningslag** oven på data, der allerede eksisterer – ikke som en erstatning for de systemer, der indsamler og opbevarer den.

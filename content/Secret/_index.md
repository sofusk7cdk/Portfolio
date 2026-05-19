---
title: "Secrets og AI: Er det hemmeligt – er det sikkert?"
description: "Hvordan kodeagenter og AI-integrationer ændrer risikobilledet for hemmeligheder som API-nøgler, .env-filer og SSH-nøgler – og hvad man gør ved det."
---

Ordet "secret" i en udviklingssammenhæng dækker over alt, vi ikke vil have andre til at se: API-nøgler, databaseadgangskoder, SSH-nøgler og tokens. Traditionelt var det relativt enkelt at holde styr på dem. Med AI-kodeagenter er risikobilledet ændret markant.

## Før og nu

**Tidligere** var flowet simpelt: udvikleren skriver kode, hemmeligheder lever i en `.env`-fil, applikationen læser dem, og alt er nogenlunde under kontrol.

**Nu** ser det anderledes ud. Når man bruger en kodeagent, sker der meget mere:

- Agenten læser hele repository'et
- Agenten kører shell-kommandoer
- Agenten redigerer filer og installerer pakker
- Agenten læser logs og debugger miljøet
- Agenten kan dele commits eller pakker
- Agenten sender kontekst til en ekstern model

Det er ikke agenten selv, der er fjendtlig – men den **har adgang til alt**, og fejl sker.

## Hvad er egentlig på spil?

### .env-filer er ikke en sikkerhedsmekanisme

`.env`-filer er et praktisk sted at samle miljøvariabler, men de er ikke designet til at holde hemmeligheder sikre. Den klassiske fejl er at pushe dem til et offentligt repository – noget der sker oftere end man tror, og som leaks-scannere på GitHub opdager kontinuerligt.

### Hvad sker der, hvis en AI-virksomhed ser mine nøgler?

Faren er ikke at OpenAI eller Anthropic "stjæler" nøglerne. Faren er at **fejl sker**, og at agenten utilsigtet eksponerer dem:

- Agenten inkluderer `.env` i et debug-resume sendt til en ekstern model
- Agenten kører en kommando der printer alle miljøvariabler (`printenv`, `env`)
- Agenten committer en fil der indeholder en nøgle
- Agenten installerer en ondsindet pakke, der læser og eksfiltrerer miljøvariabler
- Agenten skriver logs, der indeholder hemmelige værdier

Ingen af disse scenarier kræver en ondsindet agent – de opstår ved normale fejl og misforståelser.

## AI og "spill the beans"

Et interessant eksperiment er at forsøge at få en chatbot til at afsløre sin systemkonfiguration eller interne nøgler. For en simpel chatbot, der ikke har adgang til filer eller miljøvariabler, vil det typisk ikke lykkes – den har simpelthen ikke "bønner at spilde".

Men bygger man et backend-system, hvor AI'en har adgang til filsystemet og kan kalde `readFile(path)`, er situationen anderledes. Her er løsningen at **begrænse, hvad backend-toolet har adgang til**:

```
searchPublicDocs(query)  >  readFile(path)
```

Foretrækker altid snævre, formålsspecifikke værktøjer frem for brede filsystem-adgange. Jo mindre agenten kan se, desto færre hemmeligheder kan den lække.

## Hvor gemmer man så nøgler?

### .env-filer på live-serveren

Den sikreste løsning er at lade nøglerne blive på serveren – og aldrig invitere AI'en ind der. Produktionsmiljøet er stedet, AI-agenter ikke har adgang til.

Brug `.env.example` i stedet for `.env` i repository'et. Det giver et tydeligt template uden faktiske værdier, og alle ved hvilke variabler der skal sættes.

### Undgå at kompromittere udviklingstiden

Det er ikke en løsning at blokere al adgang, hvis det bremser arbejdet. Praktiske alternativer:

- **Testkeys til dev-miljøer** – opret nøgler med begrænsede rettigheder: TTL, whitelists på IP-adresser, rate limits. Brændes nøglen, er skaden minimal.
- **Mocking** – kræver tid at sætte op, men isolerer udviklingsmiljøet fuldstændigt fra rigtige credentials.
- **CI-flow** – ved et godt CI-setup kan man teste med rigtige nøgler i et isoleret, kortlivet miljø frem for lokalt.

### Key Vaults

Key vaults (fx Azure Key Vault, AWS Secrets Manager, HashiCorp Vault) er dedikerede systemer til at opbevare og rotere hemmeligheder. De løser dog ikke det grundlæggende problem:

> Hvis agenten har adgang til at *hente* en nøgle fra vaulten, kan den stadig lække den efterfølgende.

Key vaults giver et godt overblik og auditlog over hvem der tilgår hvad. Systemer med **2FA-krav** for nøgleadgang giver et ekstra lag, som en AI-agent ikke kan passere. Men de er ikke en magisk løsning.

## SSH-nøgler

SSH-nøgler anses ofte som mere "tekniske" og dermed sikrere – men risikoen er reel. En agent vil sandsynligvis aldrig aktivt forsøge at bruge dine SSH-nøgler, men **den kan**.

### Anbefalinger for SSH

- **Passphrase på alle SSH-nøgler er ikke længere valgfrit.** En nøgle uden passphrase, der havner i forkerte hænder, giver direkte adgang.
- **SSH Agent** – selv med passphrase hjælper det ikke, hvis SSH Agent allerede har nøglen loadet i hukommelsen. En agent med shell-adgang kan bruge den derfra.
- **Hardware-nøgler** (fx YubiKey) til kritiske systemer – det er den eneste løsning, der reelt forhindrer software-baseret misbrug, da den private nøgle aldrig forlader hardwaren.

## Takeaways

Undervisningens budskab kan opsummeres i tre punkter:

1. **"Vær årvågen" gælder stadig.** AI ændrer ikke de grundlæggende sikkerhedsprincipper – det forstærker konsekvenserne af at ignorere dem.

2. **Brug testkeys, begræns dem og brændt dem.** Hemmeligheder med kort levetid og snævre rettigheder begrænser skaden, hvis de lækkes.

3. **Betragt maskinen, AI'en kører på, som kompromitteret.** Det er en sund paranoiamodel. Hvad ville konsekvenserne være, hvis alt på den maskine pludselig var offentligt? Planlæg ud fra det svar.

AI-kodeagenter er kraftfulde redskaber, men de udvider angrebsfladen for utilsigtet eksponering af hemmeligheder. Den bedste forsvar er ikke at stoppe med at bruge dem – det er at designe systemer, hvor lækage af en nøgle har minimal konsekvens.

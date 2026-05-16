# Mit Projekt
### Hvad er projektet?

Et Java-system der automatiserer kommunikation med
leverandører til et julemarked.
Systemet læser leverandørdata fra Excel, genererer
personlige mails fra en skabelon,
håndterer indkomne svar og skriver status tilbage til
Excel.

### Teknologier

- **Java + Apache POI** — Excel-læsning og -skrivning
- **Jakarta Mail** — IMAP-integration til indkomne svar
- **Ollama (llama3.2)** — lokal LLM til klassifikation
  af svar

### Svaranalyse — regelbaseret vs. LLM

Systemet understøtter begge tilgange via et fælles
interface:

- **Regelbaseret** — hurtig og forudsigelig, med
  negationscheck så "jeg siger ikke ja" ikke
  fejlklassificeres
- **LLM via Ollama** — håndterer mere nuancerede svar
  på dansk, kører lokalt uden ekstern API

Falder automatisk tilbage til regelbaseret hvis Ollama
ikke er tilgængelig.

### Scope

Første iteration er bevidst minimal: ingen mailsending,
ingen database, ingen GUI.
Fokus er på kerneflowet — fra Excel-input til
klassificeret output — med en arkitektur
der er nem at udvide.

### Overvejelser

Det mest interessante designspørgsmål var hvornår en
LLM reelt tilfører værdi.
For korte JA/NEJ-svar er regelbaseret analyse faktisk
velegnet — hurtigere og mere
forudsigelig. LLM'en giver mening når sproget er mere
varieret og kontekstafhængigt.

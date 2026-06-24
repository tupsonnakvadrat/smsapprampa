# 📱 SMS Link Opener

Android aplikacija koja sluša dolazne SMS poruke i automatski otvara određeni URL.

## Što aplikacija radi

- Prima dolazne SMS poruke u pozadini
- Provjeri sadrži li poruka zadanu **ključnu riječ**
- Opcijski filtrira po **broju/imenu pošiljatelja**
- Automatski otvara zadani **URL** u browseru (lokalni IP, web, deep link...)

## Kako postaviti

### 1. Otvoriti u Android Studio

```
File → Open → odaberi mapu SmsLinkOpener
```

### 2. Buildati i instalirati

```
Build → Build APK(s)
```
ili
```
Run → Run 'app'  (USB debugging)
```

### 3. Konfiguracija unutar aplikacije

| Polje | Primjer | Opis |
|-------|---------|------|
| Ključna riječ | `OTVORI` | SMS mora sadržavati ovu riječ (prazno = svaki SMS) |
| Ciljni URL | `http://192.168.1.1/relay/1` | URL koji se otvara kada se poklopi |
| Filter pošiljatelja | `+385991234567` | Samo od ovog broja (prazno = svi) |

### 4. Dozvole

Na prvom pokretanju aplikacija traži **RECEIVE_SMS** dozvolu – obavezno prihvati.

Na Android 10+ možda je potrebno ručno odobriti u:
```
Postavke → Aplikacije → SMS Link Opener → Dozvole → SMS → Dopusti
```

## Primjeri upotrebe

```
# Pametna kuća – uključi svjetlo kad primi "SVJETLO ON"
Ključna riječ: SVJETLO ON
URL: http://192.168.1.100/api/light/on

# Router restart
Ključna riječ: RESTART
URL: http://192.168.1.1/cgi-bin/restart

# Lokalni web server
Ključna riječ: (prazno)
URL: http://localhost:8080/trigger
```

## Napomene

- **Android 8+**: aplikacija radi u pozadini bez problema
- **Android 12+**: ako uređaj ubija pozadinske procese, aplikaciju dodaj u "Zaštićene aplikacije" 
- Aplikacija pamti zadnji okidač (datum, pošiljatelj, poruka) za debugiranje
- Gumb "Testiraj URL" otvara URL odmah bez SMS-a

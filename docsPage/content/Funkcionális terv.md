---
title: "Funkcionális terv"
---

## Regisztráció
### Guest login
csak megtekintés, nem lehet elvállalni posztot
- Bemenet nincs
- Kimenet: A felhasználó geolokáció alapján, az ottani település posztjait látja

### Regisztráció:
Bement:
- Felhasználónév
	egyedi
- Keresztnév
- Vezetéknév
- Születés dátum
- Email
- Jelszó
	in 8 karakter, tartalmozzon: nagybetű, kisbetű, szám
- Település
	Az összes kötelező

Kimenet:
- Új felhasználó hozzáadása az adatbázishoz
- Átirányítás a bejelentkezés oldaldra

VAGY
### Google integráció:
Bemenet:
  - Google login gomb
    
Kimenet:
  - Regisztráció és Bejelentkezés Google fiókkal

## Bejelentkezés:
Bemenet:
  - felhasználónév
  - jelszó
  - bejelentkezés gomb
  
Kimenet:
  - bejelentkezik a felhasználó
  - átirátja a alap településre (regisztrációnál megadott)

VAGY
### Google integráció:
Bemenet:
  - Google login gomb
    
Kimenet:
  - Regisztráció és Bejelentkezés Google fiókkal

### Elfelejtettem a jelszavam
Bemenet:
- email cím

Kimenet:
- e-mail értesítés a jelszó módosításáról
- az e-mail-ben van egy gomb, amit átirányít a jelszó módosító űrlapra
  
## Felhasználó személyes adatok módosítása
Bemenet:
- Keresztnév
- Vezetéknév
- Születés dátum
- Település
- Profilhoz csatolt fájl (profilkép, önéletrajz)

Kienet:
  - Megadott adat frissítése az adatbázisban

## Jelszó módosítás
Bemenet:
- Jelszó módosítása gomb

Kimenet:
- e-mail értesítés a jelszó módosításáról
- az e-mail-ben van egy gomb, amit átirányít a jelszó módosító űrlapra

## Jelszó módosító űrlap
Bemenet:
- szöveg
	min 8 karakter
	tartalmozzon: nagybetű, kisbetű, szám
	nem lehet az előző jelszó

Kimenet:
- jelszó frissítése az adatbázisban
- e-mail értesítés a sikeres jelszó változtatásról
## Profilgomb
Bemenet:
  - Profil ikon hover
    - Opció megnyomása
  
Opciók:
  - Személyes adatok: megtekintés és módosítás
  - User statok / Dashboard
  - Beállítások
  - Kijelentkezés

## Másik felhasználó profiljának megtekintése
Bemenet:
  - profilkép megnyomása
  
Kimenet:
  - új lapon adott felhasználó statok / Dashboard
  
## Posztok
### Létrehozás
Bemenet:
  - Cím, leírás, kategória, település (kötelező)
  - Pénzjutalom (opcionális)
  - Fájlok feltöltése (opcionális)
  
Kimenet:
  - Poszt felkertül az adatbázisba
  - Publikusan elérhető az adott településnél

## Módosítás
Bemenet:
- cím
- leírás
- kategória
- település
- csatolt fájlok

Kienet:
  - Megadott adat frissítése az adatbázisban

### Lezárás
Bemenet:
  - Lezárás gomb
  - Probléma jelentése gomb
  
Kimenet:
  - Ha mindkét fél megnyomta a lezárás gombot, akkor a poszt állapota megváltozik lezártra / készre
  - Ha csak az egyik fél nyomja meg a lezárás gombot, akkor a poszt állapota megváltozik "lezárásra vár"-ra
  - Ha az egyik fél úgy érzni hogy a másik fél nem teljesítette a megállapodás alapján a kötelességét, akkor a "Probléma jelentése" gombra nyom


### Törlés
Bemenet:
- törlés gomb
	Csak akkor működik, ha a felhasználónak van rá jogosultsága
	A moderátor bármien posztot törölhet
	A felhasználó csak a saját posztját törölheti

Kimenet:
- A törölt poszt nem lesz publikusan elérhető
- Az adatbázisban egy archívum táblába kerül be
- Az archívumból 30 nap után törlődik az adat
  
### Megtekintés
- Poszt cím
- Leírás
- Csatolt fájlok
- Hozzáadott kommentek

Bemenet:
- Elvállalás gomb
- Privát chat indítása gomb
- Publikus komment
    - szövegdoboz és küldés gomb
- Feljelenés gomb
    - szövegdoboz és küldés gomb


Kimenet:
- A felhasználó elvállalja a feladatot
- Új oldalon, privát chat a másik felhasználóval
- Komment hozzáadása a poszthoz
- Moderátorok felhívása a feljelentett posztra

## Privát chat
(Valós idejű kommunikáció)

Bemenet:
- szöveg
- fájl csatolása
- Küldés gomb

Kimenet:
- A chat-en mindkét felhasználó látja az elküldott bemenetet


## User statok / Dashboard
- Összes poszt
- Létrehozott poszt száma
- Teljesített poszt száma

Bemenet:
- Privát chat indítása gomb
- Feljelenés gomb
    - szövegdoboz és küldés gomb

Kimenet:
- Új oldalon, privát chat a másik felhasználóval
- Moderátorok felhívása a feljelentett posztra


## Értékelés
Bement:
- igen/nem radio gomb
  VAGY
- 1/5 csillag
- szövegdoboz

Kimenet:
- feltölti a felhasználóhoz az értékelést


## Fejléc
### Profil ikon:
Hover:
- Kijelentkezés
- Beállítások
- Profiladatok megtekintése

Kattintás:
- Profiladatok megtekintése új oldalon
### Keresés:
Ha nincs település kiválasztva, akkor települést Keresés
Ha van település, akkor a posztok címében és szövegében keres
Ha a keresés @-al kezdődik, akkor csak felhasználónévre keres

Bemenet:
- Szöveg

Kimenet:
- Megadott település posztjai
- Leszűrt posztok
- Felhasználó

### Filter:
Bemenet:
  - közzétételi dátum (dátum), állapot (válaszható), felhasználónév (szöveg), pénz jutalom (mettől-meddig)
  
Kimenet:
  - Leszűrt posztok

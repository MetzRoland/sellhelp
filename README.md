# Vizsgaremek

Településenként a felhasználók ki tudnák rakni a problémájukat / kérésüket, amit valaki elfogadhat hogy segít megoldnat, aki kirakta a problémát pénzjutalmat is adhat a postban.


## Miért jó?
- **Gyors segítség hétköznapi gondokban**
- Erősíti a közösséget
- Több lehetőséget ad a helyi vállalkozásoknok
- Könnyű mellékkereseti lehetőség
- Gyors és helyi ügyfélszerzés
- Rugalmas

## Használati példák
- létra kölcsönkérese
- magántanár keresése
- polc befúrása
- kölzötés segítség
- darászfészek leszedése (helyi vállalkozásokank lehetőség)

## Technológiák

Backend:
- Java
	- Spring
	- Websocket
- PostgreSQL

Frontend:
- React (typescript)

Host:
- Docker

## Funkciók

[[Értékelési rendszer]]
Kommentek
Privát üzenet


## Részletek

A fizetés platformon kítül történik.

A pénz jutalom megadható egy konkrét számként ami alkudható.
A pénz jutalom megadható minimum és maximum terjedelmeként.
Ha OP nem adott meg értéket, a felvevő felajálnhat egy értéket.



## Bónusz

„Legaktívabb segítő a hónapban” kitűzó.
Google account integráció
2FA
JWT



## Hátrány
- Nem fizetés
	- Mindét félnek meg kell nyomni "Kész a feladat" gombot, ha nem, akkor [[egyéb intézkedésekhez]] lehet fordulni

- Munka idegenekkel
	- [[Értékelési rendszer]]

- Engedélyhez kötött munkák
	- Csak engedéllyel rendelkező féltől fogandjon el a falhasználó segítséget (helyi vállalkozás)
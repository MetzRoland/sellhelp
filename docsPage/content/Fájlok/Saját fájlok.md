---
title: Saját fájlok
---
## Profilkép
Egy felhasználó opcionálisan feltölthet egy profilképet.
A kép bármilyen `image/` MIME típusú lehet.

Egy felhasználóhoz csak egy profilkép tartozhat.

## Biztonság
A fájl típusát nem a `Content-type` header alapján állapítja meg, hanem a szerver megvizsgálja a feltöltött fájl metadata alapján.

## Saját fájlok
Egy felhasználó feltölthet **legfeljebb 10 saját fájlt**, ami megjelenik a saját profilján.

---
title: JWT token
---

Az oldal `access_token`-t és `refresh_token`-t használ a felhasználók hitelesítésére és a munkamenet kezelésére.

A token-ek a böngésző sütijeiben (cookies) kerülnek tárolásra, megfelelő biztonsági beállításokkal hogy minimalizálják az illetéktelen hozzáférés kockázatát.

## Használat

- **Bejelentkezett státusz hitelesítése**  
    Az `access_token` segítségével történik minden olyan cselekvés amelyhez autentikáció szükséges. Ha a token érvényes, a felhasználó hitelesítettnek számít.

- **Jelszó módosítás hitelesítése**  
    A művelet végrehajtásához érvényes `access_token` szükséges, amely igazolja a felhasználó személyazonosságát.

- **Token frissítés (session fenntartás)**  
    Ha az `access_token` lejár, a rendszer a `refresh_token` segítségével automatikusan új access tokent kérhet.
- **Kijelentkezés**  
    A kijelentkezés során mind az `access_token`, mind a `refresh_token` törlésre kerül a sütikből, ezzel megszüntetve a felhasználói munkamenetet.

## `password_update_token`

Jelszó módosításához egy külön JWT token-t alkalmazunk. Ez a sütikben **nem** kerül tárolásra. Erről részletek tovább olvashatók [[Jelszó módosítása|itt]].
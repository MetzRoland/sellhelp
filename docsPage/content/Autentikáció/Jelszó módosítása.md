---
title: Jelszó módosítása
---
A felhasználónak van lehetősége a jelszó megváltoztatására.

Az oldalon, ahol a felhasználónak lehetősége van megváltoztatni az adatait, van egy gomb, aminek megnyomásával igényelheni a jelszó megváltoztatását.

A gomb megnyomása esetén a felhasználó kap egy email-t. Az email-ben van egy gomb, ami továbbvezeti a felhasználót arra az oldalra, ahol megváltoztathatja a jelszavát. Ekkor a felhasználó kap egy ideiglenes [[JWT token]]-t, ami hitelesíti a jelszó megváltoztatását.

Erre a biztonság miatt van szükség, így az egyéb adatok megváltoztatásával ellentétben, a jelszó megválzotatása két faktoros.

A jelszó módosítás nem elérhető, ha a fiók [[Google integráció|Google fiókon keresztül történt regisztrálásra]].
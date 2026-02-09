---
title: Endpoint-ok
description: Kinek melyik endpoint-ok érhetőek el
---
## Publikus

### Fejlesztésre
- /swagger-ui/*
- /swagger-ui.html
- /api-docs/*
- /webjars/*
- /s3/*

### Autentikáció

- /auth/login/
- /auth/login/superuser
- /auth/register
- /auth/verify-totp
- /auth/login/refresh

### Külső forrásokra vezető
- /auth/login/google
- /oauth2/*
- /login/oauth2/*
- /auth/google/register


## Privát

- /user/*
- /superuser/*
- /post/*
- /api/*
- /post/*

### Csak adminoknak

- /superuser/*

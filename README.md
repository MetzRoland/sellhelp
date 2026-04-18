# SellHelp

Egy helyi közösségekre épülő digitális platform, amely összeköti azokat, akik segítséget / alkalmi munkaerőt keresnek, azokkal, akik szívesen elfogadnának kissebb, alkalmi munkákat plusz jövedelemként.

## Projekt célja

A platform célja egy egyszerű, átlátható és biztonságos rendszer létrehozása, ahol a felhasználók:

- segítséget / munkaerőt kérhetnek mindennapi vagy szakmai feladatokhoz  
- feladatokat vállalhatnak és teljesíthetnek  
- kapcsolatba léphetnek a közelükben élő emberekkel  

Az alapötlet egy IT ticketing rendszer logikájára épül, de kifejezetten lakossági és közösségi kissebb / alkalmi munkaerő piacra optimalizálva.

---

## Fő funkciók

### Feladat létrehozása
- Feladat címének megadása
- Rövid leírás megadása  
- Pénzjutalom hozzárendelése  
- Helyadatok (település) megadása
- Opcionálisan Fájlok megadása a feladathoz pl: Képek, dokumentumok

### Feladat elvállalása
- Más felhasználók jelentkezhetnek a feladatra  
- A státusz automatikusan változik:
  - `Elérhető` → `Elvállalva`

### Közös megerősítés
- A feladat csak akkor zárul le, ha **mindkét fél** visszaigazolja a teljesítést  
- Ez növeli a biztonságot és csökkenti a visszaélések esélyét

---

## Miért hasznos?

### ⏱Idő- és energiatakarékos
Gyorsan találhatsz segítséget olyan feladatokra, mint:
- költözés  
- szerelés  
- bevásárlás  

### Kiegészítő jövedelem
Lehetőséget biztosít alkalmi munkavégzésre, helyi szinten.

### Közösségépítés
A platform a helyi lakosokat köti össze, erősítve a bizalmat és az együttműködést.

### Rugalmasság
Bármilyen típusú feladat közzétehető:
- egyszerű (pl. kép felrakása)  
- szakértelmet igénylő (pl. számítógép javítás)

### Megbízhatóság
- Kétoldali visszaigazolás  
- Később bevezethető értékelési rendszer  

### Kisvállalkozások támogatása
Segíti a helyi szolgáltatók láthatóságát és ügyfélszerzését.

---

## Kihívások

A rendszer tervezése során figyelembe vett fontos kérdések:

- **Bizalom és biztonság**
- **Pénzkezelés és kifizetések**
- **Konfliktuskezelés**
- **Moderáció és szabályozás**

### Lehetséges megoldások:
- Felhasználói értékelések  
- Jelentési rendszer  
- Alapvető szabályzat  
- Admin/moderátor felület  

---

## Jövőbeli fejlesztések

- Értékelési és reputációs rendszer  
- Beépített chat funkció  
- Pontosabb helyalapú szűrés  
- Mobilalkalmazás  
- Biztonságos fizetési integráció  

---

## Technológiák a projektben

- Backend: Spring boot  
- Adatbázis: PostgreSQL
- Frontend: React + Typescript
- Auth: JWT / OAuth2
- Hosting: AWS EC2

---

## Projekt felállítása (self-hostolása)

Az alábbi lépések segítségével elindíthatod a projektet lokális környezetben. Ez által megismerheted, hogy hogyan működik az alkalmazás, milyen komponensei vannak. Valamint minden gépen működik és könnyen felállítható a projekt, mivel a projekt teljesen konténerizált, tehát docker segítségével működik.

---

### A projekt futtatásához szükséges feltételek:
- Docker
- Java JDK 25
- Proton Mail fiók a proton bridge konténerhez opcionálisan custom domain-nel(Ez felel az email értesítésekért)

Ha nem Proton Mail-t használsz email szolgáltatásnak, akkor ne használd a proton-bridge konténert, hanem a megfelelő email szolgáltató konénerét használd és ezt konfiguráld a docker-compose.yml fájlban.

---

### 1. Docker telepítése

Telepítsd a Docker-t és a Docker Compose-ot:

- https://www.docker.com/

Ellenőrzés:

```bash
docker --version
docker compose version
```

---

### 2. Java jdk 25 telepítése
https://www.oracle.com/java/technologies/downloads/#java25

### 3. Projekt klónozása

```bash
git clone https://github.com/MetzRoland/vizsgaremek.git
cd vizsgaremek
```

---

### 4. Környezeti változók beállítása

Hozd létre a `.env-docker` fájlt a gyökér könyvtárba és töltsd ki a saját konfigurációs adataidall.

```env
# Mail service configuration
SPRING_MAIL_HOST=your_host
SPRING_MAIL_SMTP_PORT=smtp_port
SPRING_MAIL_IMAP_PORT=imap_port
SPRING_MAIL_USERNAME=mail_username
SPRING_MAIL_PASSWORD=mail_password
SPRING_MAIL_FROM=mail_from
SPRING_MAIL_SMTP_SSL_TRUST=ssl_trust

# Database configuration
DATABASE_HOST=your_database_host
DATABASE_USER=your_database_user
DATABASE_PASSWORD=your_database_password

# S3
S3_ACCESS_KEY_ID=your_access_key
S3_SECRET_ACCESS_KEY=your_secret_key
S3_REGION=region
S3_BUCKET_NAME=your_bucket_name

S3_ENDPOINT=your_s3_endpoint
S3_SIGNED_URL_DOMAIN=your_signed_domain
S3_AWS=false

# JWT configuration
JWT_ACCESS_SECRET=your_access_secret
JWT_REFRESH_SECRET=your_refresh_secret
JWT_ACCESS_TIME=900000
JWT_REFRESH_TIME=1209600000

JWT_PASSWORD_UPDATE_SECRET=your_password_update_secret
JWT_PASSWORD_UPDATE_TIME=300000

JWT_COOKIE_ACCESS_TIME=900
JWT_COOKIE_REFRESH_TIME=1209600

# Google OAuth2 configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URL=your_google_redirect_url

# Docker
POSTGRES_USER=your_database_user
POSTGRES_PASSWORD=your_database_password
POSTGRES_DB=your_database_name

MINIO_ROOT_USER=your_minio_user
MINIO_ROOT_PASSWORD=your_minio_password
MINIO_DEFAULT_BUCKETS=your_bucket_name

# Backend URL
VITE_API_BASE_URL=your_backend_url

# Frontend URL
FRONTEND_URL=your_frontend_url
```

---

### 5. Frontend konténer buildelése

```bash
sudo docker build -t frontend frontend
```

---

### 6. Backend buildelése és Docker image létrehozása

```bash
cd backend
sudo ./mvnw clean verify
cd ..

sudo docker build -t sellhelp:latest backend
```

---

### 7. shdocs dokumentációs konténer buildelése

```bash
sudo docker build -t shdocs docsPage
```

---

### 8. Nginx beállítása (lokális használatra)

```bash
cd nginx
```

Hozd létre vagy módosítsd a `default.conf` fájlt:

```nginx
# ---------------------------
# Storage (MinIO) HTTPS
# ---------------------------
server {
    listen 443 ssl http2;
    server_name storage.sellhelp.org;

    client_max_body_size 20m;

    ssl_certificate /etc/nginx/certs/sellhelp_org.crt;
    ssl_certificate_key /etc/nginx/certs/sellhelp_org.key;

    location / {
        proxy_pass http://storage:9000/;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;

        proxy_buffering off;
        proxy_request_buffering off;

        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
    }
}

server {
    listen 80;
    server_name storage.sellhelp.org;
    return 301 https://$host$request_uri;
}

# ---------------------------
# Docs HTTPS
# ---------------------------
server {
    listen 443 ssl http2;
    server_name docs.sellhelp.org;

    ssl_certificate /etc/nginx/certs/sellhelp_org.crt;
    ssl_certificate_key /etc/nginx/certs/sellhelp_org.key;

    location / {
        proxy_pass http://docs:7999;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

server {
    listen 80;
    server_name docs.sellhelp.org;
    return 301 https://$host$request_uri;
}

# ---------------------------
# API HTTPS
# ---------------------------
server {
    listen 443 ssl http2;
    server_name api.sellhelp.org;

    client_max_body_size 20m;

    ssl_certificate /etc/nginx/certs/sellhelp_org.crt;
    ssl_certificate_key /etc/nginx/certs/sellhelp_org.key;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://sellhelp:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;

        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}

server {
    listen 80;
    server_name api.sellhelp.org;
    return 301 https://$host$request_uri;
}

# ---------------------------
# Frontend HTTPS
# ---------------------------
server {
    listen 443 ssl http2;
    server_name sellhelp.org www.sellhelp.org;

    ssl_certificate /etc/nginx/certs/sellhelp_org.crt;
    ssl_certificate_key /etc/nginx/certs/sellhelp_org.key;

    location / {
        proxy_pass http://frontend:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

server {
    listen 80;
    server_name sellhelp.org www.sellhelp.org;
    return 301 https://$host$request_uri;
}
```

---

### 9. hosts fájl módosítása:
```bash
127.0.0.1 storage.sellhelp.org
127.0.0.1 api.sellhelp.org
127.0.0.1 docs.sellhelp.org
127.0.0.1 sellhelp.org
127.0.0.1 www.sellhelp.org
```
---

### 10. Proton bridge konténer inicializálsa (felhasználónév + jelszó megadása a proton mailhez)

```bash
docker run --rm -it -v ./protonmail:/root dancwilliams/protonmail-bridge init
```

Itt kérni fogja a konténer a proton mail felhasználónevet és jelszót. Ezt csak egyszer kell megadni mert a belépési adatok el lesznek tárolva volum-ba.

---

### 11. Konténerek indítása

```bash
sudo docker compose up --build
```

---

### 12. Leállítás

```bash
sudo docker compose down
```

---

### 13. Projekt használata
Egy böngészőbe Írd be: https://sellhelp.org
és működik és a projekt self-hostolva.
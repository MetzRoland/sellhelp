---
title: Deployment
---
A teljes projekt az Amazon Web Services (AWS) egy Elastic Compute Cloud (EC2) példányon fut. Ezen belül a platform különböző részei konténerekre van bontva, amiket `docker compose` segítségével hangolunk össze.

Az alábbi konténereket alkalmazzuk:
- `Backend` - Java Spring Boot
- `Database` - PostgreSQL
- `Frontend` - Megépített React prject, amit Nginx szolgál ki
- `S3` - [[S3|Object storage]]
- `Docs` - Obsidian Quartz
- `Nginx` - Reverse proxy
- `Email brige` - Szükséges, hogy csatlakoztassunk a [[Email szolgáltatás|Proton szolgáltatására]]

# Diagram:
![[deployment.svg]]

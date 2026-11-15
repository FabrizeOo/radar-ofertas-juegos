# Radar de Ofertas Gaming (Proyecto 4)

Sistema **NoSQL con MongoDB**, **Arquitectura Hexagonal en Java 21 (Spring Boot)** y **Frontend HTML/JS desacoplado** con consultas en tiempo real (Server-Sent Events) y caja de comentarios en vivo.

## Estructura
- `backend/` — API REST + SSE en Java 21 con Arquitectura Hexagonal (`dominio`, `aplicacion`, `infraestructura`).
  - Consume JSON masivo desde **CheapShark API** (`https://www.cheapshark.com/api/1.0/deals` y `/stores`).
  - Almacena miles de documentos en **MongoDB** (`ofertas_juegos` y `comentarios_ofertas`) y calcula estadísticas con **Aggregation Pipelines**.
- `frontend/` — Sitio estático (`index.html`, `styles.css`, `app.js`) listo para subir a **Vercel**.

## 1. Configurar MongoDB Atlas (Gratis)
1. Crea una cuenta en [MongoDB Atlas](https://www.mongodb.com/atlas) y crea un clúster gratuito (**M0 Free**).
2. En **Database Access**, crea un usuario y contraseña.
3. En **Network Access**, agrega `0.0.0.0/0` para permitir conexiones desde tu PC y desde Render/Railway.
4. Copia tu URI de conexión, por ejemplo:
   `mongodb+srv://usuario:clave@cluster0.xxxxx.mongodb.net/radar_ofertas?retryWrites=true&w=majority`

## 2. Ejecutar en Local
En `backend/`, define la variable de entorno `MONGODB_URI` y ejecuta con Java 21:

```powershell
$env:MONGODB_URI="mongodb+srv://usuario:clave@cluster0.xxxxx.mongodb.net/radar_ofertas?retryWrites=true&w=majority"
cd backend
mvn spring-boot:run
```

Al iniciar por primera vez, si MongoDB está vacío, importará automáticamente **600 ofertas JSON** (~10 páginas) y podrás seguir sumando más con el botón **⚡ Sincronizar +600 JSON** en el HTML.

Luego abre `frontend/index.html` con Live Server (o cualquier servidor estático).

## 3. Desplegar en la Nube
- **Backend (Render / Railway):** Sube la carpeta `backend/` usando el `Dockerfile` incluido y configura la variable de entorno `MONGODB_URI`.
- **Frontend (Vercel):** Importa la carpeta `frontend/` en Vercel. Una vez desplegado, abre tu web en Vercel, pulsa **⚙️ API** arriba a la derecha (o edita `DEFAULT_API_URL` en `frontend/app.js`) y pega la URL pública de tu backend.


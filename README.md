# Ronda API — Backend para el TP de Desarrollo de Aplicaciones I

API REST en **Spring Boot 3 (Java 17)** + **MySQL** para la app "Ronda" (compra/venta entre personas). Pensada para que la app Android (Java) le pegue por HTTP a cada endpoint.

---

## 1. Cómo levantarla (cada integrante, en su máquina)

Todo el equipo trabaja contra la **misma base de datos** (MySQL en Aiven, gratis) y hay además una copia del backend **desplegada en Render**. Cada uno puede:
- **(a)** pegarle directo al backend ya desplegado (no hace falta instalar nada), o
- **(b)** correr el backend local en su máquina, apuntando a esa misma base de Aiven — recomendado para el día de la presentación en vivo, porque el free tier de Render "duerme" y tarda ~50s en responder la primera vez.

Este README no tiene (ni va a tener) las credenciales reales de la base — GitHub las bloquea automáticamente si las detecta en un commit (push protection), y aunque no las bloqueara, es mala práctica subir contraseñas al repo. Se reparten aparte.

### Requisitos
- Java 17+
- VS Code con la extensión de Java (Extension Pack for Java) — así levantás la app con el botón ▶ **Run** sobre `RondaApiApplication.java`, sin necesitar Maven instalado aparte.
- **NO hace falta instalar MySQL local** — se usa la base compartida en la nube.

### Pasos para correr local apuntando a la base compartida

1. Clonar el repo y pararse en la rama que corresponda (`Backend-2.0` mientras se termina de probar, después `main`).
2. Abrir la carpeta del proyecto en VS Code.
3. Copiar el archivo `.env.example` (está en la raíz del proyecto) y renombrar la copia a `.env` (mismo nivel que `pom.xml`). El `.env` **no se sube a git** a propósito — ahí van las credenciales reales.
4. Pedirle a Ivan los valores reales (por WhatsApp/Drive del grupo, no por git) y completar el `.env`:
   ```
   DB_URL=jdbc:mysql://<host-de-aiven>:<puerto>/<database>?sslMode=REQUIRED
   DB_USER=<usuario-de-aiven>
   DB_PASSWORD=<password-de-aiven>
   ```
5. Guardar y correr la app con el botón ▶ **Run** arriba de `RondaApiApplication.java` (el `launch.json` del proyecto ya está configurado para leer ese `.env` automáticamente — `"envFile": "${workspaceFolder}/.env"`).
6. Hibernate crea/actualiza las tablas solas al arrancar (`ddl-auto=update`) — como todos apuntan a la misma base, las tablas y los datos ya están creados desde que alguien corrió esto la primera vez.
7. Confirmar que levantó: `http://localhost:8080/api/categorias` debería devolver JSON.
8. Documentación interactiva (Swagger): `http://localhost:8080/swagger-ui.html`.

Si preferís terminal en vez de VS Code y tenés Maven instalado (`mvn -v` funciona en tu PowerShell), también podés usar `run-local.ps1.example` como base para un `run-local.ps1` propio (mismo patrón que el `.env`, pero seteando variables de entorno y corriendo `mvn spring-boot:run`).

### Usando el backend ya desplegado (sin correr nada local)

URL: la que te pase Ivan del deploy de Render. Como el free tier duerme a los 15 min de inactividad, la primera petición después de un rato puede tardar ~50 segundos — es normal, no es que esté roto.

Si van a probar contra el emulador de Android Studio con el backend corriendo **local en la misma PC**, usar `http://10.0.2.2:8080` en vez de `localhost:8080` (así es como el emulador ve al host).

---

## 2. Autenticación

Todos los endpoints salvo los marcados como **público** requieren el header:

```
Authorization: Bearer <token>
```

El `<token>` se obtiene de `/api/auth/otp/verificar` o `/api/auth/login`.

---

## 3. Endpoints

### 3.1 Auth (`/api/auth`) — todos públicos

**Solicitar OTP** — dispara el código (se loguea en la consola del backend, no se manda mail real).
```
POST /api/auth/otp/solicitar
Content-Type: application/json

{ "email": "juan@mail.com" }
```
→ `202 Accepted`

**Reenviar OTP**
```
POST /api/auth/otp/reenviar
Content-Type: application/json

{ "email": "juan@mail.com" }
```
→ `202 Accepted`

**Verificar OTP** — crea el usuario si es la primera vez, devuelve el JWT.
```
POST /api/auth/otp/verificar
Content-Type: application/json

{ "email": "juan@mail.com", "codigo": "123456" }
```
→ `200 OK`
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "usuarioId": 1, "email": "juan@mail.com", "username": null }
```

**Login con usuario y contraseña** — requiere haber seteado `username`/`password` antes con `PUT /api/usuarios/me`.
```
POST /api/auth/login
Content-Type: application/json

{ "usernameOrEmail": "juanp", "password": "miPassword123" }
```
→ `200 OK` (mismo formato que arriba)

---

### 3.2 Perfil y Reputación (`/api/usuarios`)

**Mi perfil** (requiere token)
```
GET /api/usuarios/me
Authorization: Bearer <token>
```
→
```json
{
  "id": 1, "nombre": null, "email": "juan@mail.com", "username": null,
  "telefono": null, "zona": null, "fechaAlta": "2026-08-30T10:00:00",
  "promedioEstrellas": 0.0, "operacionesComoComprador": 0, "operacionesComoVendedor": 0
}
```

**Editar mi perfil** (requiere token) — todos los campos son opcionales, mandá solo los que cambian.
```
PUT /api/usuarios/me
Authorization: Bearer <token>
Content-Type: application/json

{ "nombre": "Juan Pérez", "telefono": "1122334455", "zona": "Palermo", "username": "juanp", "password": "miPassword123" }
```

**Perfil público de otro usuario** (público, sin token)
```
GET /api/usuarios/5
```
→
```json
{
  "id": 5, "nombre": "Ana Gómez", "zona": "Belgrano",
  "fechaAlta": "2026-07-01T09:00:00", "promedioEstrellas": 4.5,
  "totalCalificaciones": 12, "publicacionesActivas": [ { "id": 10, "titulo": "Bici rodado 26", "precio": 45000 } ]
}
```

**Calificar a un usuario** (requiere token) — después de cerrar una operación.
```
POST /api/usuarios/5/calificaciones
Authorization: Bearer <token>
Content-Type: application/json

{ "publicacionId": 10, "rolReceptor": "VENDEDOR", "estrellas": 5, "comentario": "Todo perfecto" }
```
`rolReceptor`: `"COMPRADOR"` o `"VENDEDOR"`. → `201 Created`

---

### 3.3 Explorar Publicaciones — Home (`/api/publicaciones`) — público

```
GET /api/publicaciones?query=bici&categoriaId=5&precioMin=1000&precioMax=50000&estadoArticulo=USADO&zona=Palermo&orden=MENOR_PRECIO&pagina=0&tamanio=20
```
Todos los parámetros son opcionales.
- `estadoArticulo`: `NUEVO` | `COMO_NUEVO` | `USADO`
- `orden`: `RECIENTES` | `MENOR_PRECIO` | `MAYOR_PRECIO`

→
```json
{
  "contenido": [ { "id": 10, "titulo": "Bici rodado 26", "precio": 45000, "estadoArticulo": "USADO", "estado": "ACTIVA", "zonaEntrega": "Palermo", "fotoPrincipal": "https://...", "fechaPublicacion": "...", "vendedorNombre": "Ana Gómez" } ],
  "pagina": 0, "tamanio": 20, "totalElementos": 1, "totalPaginas": 1
}
```

---

### 3.4 Detalle de Publicación

```
GET /api/publicaciones/10
Authorization: Bearer <token>   (opcional: si mandás token, indica si la publicación es tuya)
```
→
```json
{
  "id": 10, "titulo": "Bici rodado 26", "descripcion": "...", "categoria": "Deportes",
  "precio": 45000, "estadoArticulo": "USADO", "estado": "ACTIVA", "zonaEntrega": "Palermo",
  "fechaPublicacion": "...", "fotos": ["https://...", "https://..."],
  "vendedorId": 5, "vendedorNombre": "Ana Gómez", "vendedorPromedioEstrellas": 4.5,
  "esPropia": false
}
```

**Preguntar** (requiere token)
```
POST /api/publicaciones/10/preguntas
Authorization: Bearer <token>
Content-Type: application/json

{ "mensaje": "¿Todavía está disponible?" }
```

**Ver preguntas de una publicación** (público)
```
GET /api/publicaciones/10/preguntas
```

**Responder una pregunta** (requiere token, solo el vendedor dueño)
```
PUT /api/publicaciones/10/preguntas/3/respuesta
Authorization: Bearer <token>
Content-Type: application/json

{ "respuesta": "Sí, sigue disponible" }
```

**Ofertar** (requiere token) — el `mensaje` es opcional. La oferta queda `PENDIENTE` y vence sola a las 48hs si nadie responde (ver punto 3.9).
```
POST /api/publicaciones/10/ofertas
Authorization: Bearer <token>
Content-Type: application/json

{ "monto": 40000, "mensaje": "¿Lo dejás en 40 mil?" }
```

**Ver ofertas recibidas** (requiere token, solo el vendedor dueño)
```
GET /api/publicaciones/10/ofertas
Authorization: Bearer <token>
```

---

### 3.5 Publicar un Artículo (carga guiada con borrador)

**Obtener/crear mi borrador actual** (requiere token) — si ya tenías uno sin terminar, devuelve ese mismo.
```
GET /api/publicaciones/borrador
Authorization: Bearer <token>
```

**Guardar un paso del borrador** (requiere token) — mandá solo los campos que ya completaste en ese paso, es acumulativo.
```
PUT /api/publicaciones/7
Authorization: Bearer <token>
Content-Type: application/json

{
  "titulo": "Bici rodado 26",
  "descripcion": "Poco uso, con cambios Shimano",
  "categoriaId": 5,
  "precio": 45000,
  "estadoArticulo": "USADO",
  "zonaEntrega": "Palermo",
  "fotosUrls": ["https://miapp.com/fotos/1.jpg", "https://miapp.com/fotos/2.jpg"]
}
```
(Las fotos se suben aparte por fuera de esta API —por ejemplo a Firebase Storage o Cloudinary desde la app— y acá solo se guardan las URLs resultantes.)

**Publicar** (pasa de BORRADOR a ACTIVA; valida que estén los campos obligatorios)
```
POST /api/publicaciones/7/publicar
Authorization: Bearer <token>
```

**Mis publicaciones**
```
GET /api/publicaciones/mias?estado=ACTIVA
Authorization: Bearer <token>
```
`estado` es opcional (`ACTIVA` | `PAUSADA` | `VENDIDA`); sin filtro trae todas menos los borradores.

**Pausar / Reactivar / Marcar vendida**
```
PATCH /api/publicaciones/7/pausar
PATCH /api/publicaciones/7/reactivar
PATCH /api/publicaciones/7/vendida
Authorization: Bearer <token>
```
→ `204 No Content`

---

### 3.6 Favoritos (`/api/favoritos`) — todos requieren token

**Marcar como favorita**
```
POST /api/favoritos/10
Authorization: Bearer <token>
```
→ `201 Created`

**Quitar de favoritos**
```
DELETE /api/favoritos/10
Authorization: Bearer <token>
```
→ `204 No Content`

**Listar mis favoritos** — incluye si cambió el precio desde que la agregaste.
```
GET /api/favoritos
Authorization: Bearer <token>
```
→
```json
[ { "favoritoId": 1, "publicacion": { "id": 10, "titulo": "Bici rodado 26", "precio": 40000 }, "cambioPrecio": true } ]
```

---

### 3.7 Búsquedas Guardadas (`/api/busquedas-guardadas`) — todos requieren token

**Guardar una búsqueda con sus filtros**
```
POST /api/busquedas-guardadas
Authorization: Bearer <token>
Content-Type: application/json

{ "nombre": "Bicis baratas en Palermo", "query": "bici", "categoriaId": 5, "precioMin": null, "precioMax": 50000, "estadoArticulo": "USADO", "zona": "Palermo" }
```

**Listar mis búsquedas guardadas** — `hayNovedades` indica si aparecieron publicaciones nuevas que matchean desde la última revisión.
```
GET /api/busquedas-guardadas
Authorization: Bearer <token>
```
→ `[ { "id": 1, "nombre": "Bicis baratas en Palermo", "hayNovedades": true } ]`

**Marcar como revisada** (resetea el indicador de novedad)
```
PATCH /api/busquedas-guardadas/1/revisada
Authorization: Bearer <token>
```

**Eliminar**
```
DELETE /api/busquedas-guardadas/1
Authorization: Bearer <token>
```

---

### 3.8 Categorías (`/api/categorias`) — público

```
GET /api/categorias
```
→ `[ { "id": 1, "nombre": "Hogar" }, { "id": 2, "nombre": "Electrodomésticos" } ]`

---

### 3.9 Ofertas y Negociación (`/api/ofertas`) — todos requieren token

La negociación es una cadena de ofertas: la oferta original la crea el interesado (`POST /api/publicaciones/{id}/ofertas`, punto 3.4). A partir de ahí, en cada paso **le toca responder a la contraparte** (si ofertó el comprador, responde el vendedor; si contraofertó el vendedor, responde el comprador, y así se va alternando). Cada oferta pendiente vence sola a las **48hs** (configurable, `app.ofertas.vigencia-horas`) — un job corre cada 15 minutos y las pasa a `VENCIDA` automáticamente.

Estados posibles de una oferta: `PENDIENTE`, `ACEPTADA`, `RECHAZADA`, `VENCIDA`, `CONTRAOFERTADA`.

**Aceptar una oferta** (requiere ser la contraparte que debe responder) — al aceptar se crea automáticamente una **Operación** (ver 3.10) para coordinar la entrega.
```
PUT /api/ofertas/25/aceptar
Authorization: Bearer <token>
```

**Rechazar una oferta**
```
PUT /api/ofertas/25/rechazar
Authorization: Bearer <token>
```

**Contraofertar** (crea una nueva oferta encadenada a la anterior, que pasa a `CONTRAOFERTADA`)
```
POST /api/ofertas/25/contraofertar
Authorization: Bearer <token>
Content-Type: application/json

{ "monto": 42000, "mensaje": "Te lo dejo en 42 mil y te lo alcanzo yo" }
```
→
```json
{
  "id": 26, "publicacionId": 10, "publicacionTitulo": "Bici rodado 26",
  "autorId": 5, "autorNombre": "Ana Gómez", "monto": 42000, "mensaje": "Te lo dejo en 42 mil y te lo alcanzo yo",
  "estado": "PENDIENTE", "fecha": "...", "fechaVencimiento": "...", "tipo": "CONTRAOFERTA"
}
```

**Mis ofertas enviadas** (como comprador/interesado) y **recibidas** (como vendedor, en todas mis publicaciones)
```
GET /api/ofertas/enviadas
GET /api/ofertas/recibidas
Authorization: Bearer <token>
```

---

### 3.10 Coordinación de Entrega y Mapa (`/api/operaciones`) — todos requieren token

Una **Operación** se crea sola cuando se acepta una oferta (3.9). Ahí se coordina el punto de encuentro (para mostrar el mapa y el botón "Cómo llegar" en la app) y se marca cuando la entrega ya se hizo.

**Mis operaciones** (como comprador o como vendedor)
```
GET /api/operaciones/mias
Authorization: Bearer <token>
```
→
```json
[{
  "id": 8, "publicacionId": 10, "publicacionTitulo": "Bici rodado 26",
  "compradorId": 5, "compradorNombre": "Ana Gómez", "vendedorId": 2, "vendedorNombre": "Juan Pérez",
  "montoFinal": 42000, "estado": "PENDIENTE_ENTREGA", "fechaAcordada": "...", "fechaEntrega": null,
  "direccionEncuentro": null, "latitudEncuentro": null, "longitudEncuentro": null,
  "puedeCalificar": false, "tipo": "COMPRA"
}]
```
`tipo` es relativo a quién consulta (`COMPRA` o `VENTA`). `puedeCalificar` ya viene calculado (ver 3.11).

**Detalle de una operación**
```
GET /api/operaciones/8
Authorization: Bearer <token>
```

**Definir/actualizar el punto de encuentro** (lat/long para el mapa; cualquiera de las dos partes puede cargarlo)
```
PUT /api/operaciones/8/punto-encuentro
Authorization: Bearer <token>
Content-Type: application/json

{ "direccion": "Plaza Serrano, Palermo", "latitud": -34.5885, "longitud": -58.4306 }
```
(Con `latitud`/`longitud` la app arma el link de "Cómo llegar", por ej. `https://www.google.com/maps/dir/?api=1&destination=<lat>,<long>`.)

**Marcar como entregada** — pasa la operación a `ENTREGADA`, marca la publicación como `VENDIDA` y abre la ventana de 7 días para calificar.
```
PUT /api/operaciones/8/entregada
Authorization: Bearer <token>
```

---

### 3.11 Historial y Calificaciones (`/api/operaciones/{id}/calificar`) — requiere token

Solo se puede calificar una operación **ya entregada**, dentro de los **7 días** posteriores a la entrega, y una sola vez por usuario. El receptor y su rol (`COMPRADOR`/`VENDEDOR`) se deducen solos según quién califica.
```
POST /api/operaciones/8/calificar
Authorization: Bearer <token>
Content-Type: application/json

{ "estrellas": 5, "comentario": "Todo perfecto, puntual y el artículo como lo describió" }
```
→ `201 Created` (o `409 Conflict` si ya venció la ventana de 7 días o ya calificaste esa operación)

Estas calificaciones alimentan la reputación del punto 3.2 (`GET /api/usuarios/{id}`: promedio de estrellas y cantidad de operaciones como comprador/vendedor).

---

## 4. Tabla resumen

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/auth/otp/solicitar` | No | Pide OTP para un email |
| POST | `/api/auth/otp/reenviar` | No | Reenvía OTP |
| POST | `/api/auth/otp/verificar` | No | Verifica OTP, crea usuario y da token |
| POST | `/api/auth/login` | No | Login con user/password |
| GET | `/api/usuarios/me` | Sí | Mi perfil |
| PUT | `/api/usuarios/me` | Sí | Editar mi perfil |
| GET | `/api/usuarios/{id}` | No | Perfil público |
| POST | `/api/usuarios/{id}/calificaciones` | Sí | Calificar a un usuario |
| GET | `/api/publicaciones` | No | Buscar/listar (Home) |
| GET | `/api/publicaciones/{id}` | Opcional | Detalle |
| GET | `/api/publicaciones/borrador` | Sí | Obtener/crear mi borrador |
| PUT | `/api/publicaciones/{id}` | Sí | Guardar paso del borrador |
| POST | `/api/publicaciones/{id}/publicar` | Sí | Publicar el borrador |
| GET | `/api/publicaciones/mias` | Sí | Mis publicaciones |
| PATCH | `/api/publicaciones/{id}/pausar` | Sí | Pausar |
| PATCH | `/api/publicaciones/{id}/reactivar` | Sí | Reactivar |
| PATCH | `/api/publicaciones/{id}/vendida` | Sí | Marcar vendida |
| POST | `/api/publicaciones/{id}/preguntas` | Sí | Preguntar |
| GET | `/api/publicaciones/{id}/preguntas` | No | Ver preguntas |
| PUT | `/api/publicaciones/{id}/preguntas/{pid}/respuesta` | Sí (vendedor) | Responder pregunta |
| POST | `/api/publicaciones/{id}/ofertas` | Sí | Ofertar |
| GET | `/api/publicaciones/{id}/ofertas` | Sí (vendedor) | Ver ofertas recibidas |
| PUT | `/api/ofertas/{id}/aceptar` | Sí (contraparte) | Aceptar oferta → crea Operación |
| PUT | `/api/ofertas/{id}/rechazar` | Sí (contraparte) | Rechazar oferta |
| POST | `/api/ofertas/{id}/contraofertar` | Sí (contraparte) | Contraofertar |
| GET | `/api/ofertas/enviadas` | Sí | Mis ofertas enviadas |
| GET | `/api/ofertas/recibidas` | Sí | Ofertas recibidas en mis publicaciones |
| GET | `/api/operaciones/mias` | Sí | Mis operaciones (compras y ventas) |
| GET | `/api/operaciones/{id}` | Sí (participante) | Detalle de una operación |
| PUT | `/api/operaciones/{id}/punto-encuentro` | Sí (participante) | Definir punto de encuentro (mapa) |
| PUT | `/api/operaciones/{id}/entregada` | Sí (participante) | Marcar entrega realizada |
| POST | `/api/operaciones/{id}/calificar` | Sí (participante) | Calificar (ventana de 7 días) |
| GET | `/api/favoritos` | Sí | Listar favoritos |
| POST | `/api/favoritos/{publicacionId}` | Sí | Agregar favorito |
| DELETE | `/api/favoritos/{publicacionId}` | Sí | Quitar favorito |
| GET | `/api/busquedas-guardadas` | Sí | Listar búsquedas guardadas |
| POST | `/api/busquedas-guardadas` | Sí | Crear búsqueda guardada |
| DELETE | `/api/busquedas-guardadas/{id}` | Sí | Eliminar |
| PATCH | `/api/busquedas-guardadas/{id}/revisada` | Sí | Marcar revisada |
| GET | `/api/categorias` | No | Listar categorías |

---

## 5. Notas para la app Android

- **Modo sin conexión (punto 6 del TP)**: es responsabilidad de la app, no del backend. La API ya devuelve todo lo necesario para cachear localmente (Room/SQLite) el detalle de las últimas publicaciones vistas (título, descripción, fotos, precio, vendedor). La app debe guardar esa respuesta al hacer `GET /api/publicaciones/{id}` y mostrarla si no hay conexión; las acciones que escriben (preguntar, ofertar, favoritos) deben deshabilitarse sin conexión ya que necesitan pegarle al servidor.
- **Emulador de Android Studio contra backend local**: usar `http://10.0.2.2:8080` en vez de `http://localhost:8080`.
- **Errores**: todos los errores devuelven este formato JSON con el código HTTP correspondiente (400, 403, 404, 409, 500):
  ```json
  { "timestamp": "...", "status": 400, "error": "Bad Request", "mensaje": "descripción del error" }
  ```
- **Fotos**: esta API guarda URLs de fotos, no archivos. Para subir fotos desde el celular, hay que subirlas primero a algún storage (Firebase Storage, Cloudinary, etc.) y mandar acá la URL resultante.

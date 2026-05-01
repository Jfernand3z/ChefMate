---
description: Se activa en todo el proyecto móvil (App Android). Conoce que usamos Kotlin, Jetpack Compose, MVVM y Retrofit, consumiendo una API RESTful local (FastAPI), es una App para encontrar recetas con un agente ia y con los datos que de productos que almacenamos en la bd.
---
# Directrices del Proyecto Móvil (Android + Kotlin)

- **Comentarios:** No debe existir ningun tipo de comentario en el código.
- **Idioma:** El idioma principal para los textos de despliegue, para variables parametros y todo lo relacionado al codigo ingles.
- **Paradigma de UI:** Toda la interfaz gráfica debe construirse de forma declarativa utilizando exclusivamente **Jetpack Compose**. Está estrictamente prohibido usar o sugerir vistas clásicas basadas en XML (`ViewBinding` o `DataBinding`).
- **Arquitectura:** El proyecto sigue rígidamente el patrón **MVVM (Model-View-ViewModel)** combinado con el **Patrón Repositorio**.
    - **View (Compose):** Solo observa el estado y emite eventos de intención del usuario. No contiene lógica de negocio.
    - **ViewModel:** Gestiona la lógica de presentación. Todo el estado de la UI debe exponerse a través de `StateFlow` o `SharedFlow`. No debe tener referencias a dependencias del framework de Android (como `Context`), salvo `AndroidViewModel` si es estrictamente necesario.
    - **Repository:** Es la única fuente de la verdad para la obtención de datos.
- **Asincronía:** Se deben utilizar exclusivamente **Kotlin Coroutines** para operaciones en segundo plano (llamadas de red, lógica pesada).
    - La comunicación con el backend (FastAPI) se realiza a través de **Retrofit** y serialización JSON (Gson o Moshi).
    - La autenticación en la app móvil **omite** el uso de cookies en favor de una inyección de headers directa. El endpoint de login devuelve el `access_token` y `refresh_token` en el payload JSON. Deben guardarse en almacenamiento local seguro e inyectarlos de vuelta mediante un Interceptor de Retrofit (`Authorization: Bearer <token>`). El endpoint `/refresh` también soporta POST en JSON.
- **Restricción de Alcance (MVP):** Por restricciones de tiempo del proyecto, **no se debe implementar base de datos local (Room)** ni caché offline compleja. El repositorio siempre debe consumir directamente los datos de la API remota.
- **Contexto:** Existe una backend donde se obtendra los datos `D:\TAW-251\Proyecto\APP\chefmate_backend`
- **Estilo:** Maneja los colores y fuentes que estan en `/theme/Color.kt` y `/theme/Type.kt`.

---

# Documentación de la API de ChefMate

Todas las rutas (excepto Login y Register) requieren autenticación mediante cabecera `Authorization: Bearer <access_token>`.

## 👤 Endpoints de Usuarios y Sesiones (Auth)

### 1. Iniciar Sesión (Login)
* **Endpoint:** `POST /ChefMate/users/login`
* **Request (application/x-www-form-urlencoded):** *(Ojo: no es JSON).*
  * `username`: juan@chefmate.com 
  * `password`: Password123!
* **Response (200 OK):**
  ```json
  {
    "success": true,
    "user": {
      "email": "juan@chefmate.com",
      "username": "Juan Cocinero"
    },
    "access_token": "eyJhbGciOiJIUzI1...",
    "refresh_token": "eyJhbGciOiJIUzI1..."
  }
  ```

### 2. Recuperar Perfil (Me)
* **Endpoint:** `GET /ChefMate/auth/me`
* **Response (200 OK):**
  ```json
  {
    "id": "60a7b4f59b...",
    "username": "Juan Cocinero",
    "email": "juan@chefmate.com"
  }
  ```

### 3. Renovar Token Caducado (Refresh)
* **Endpoint:** `POST /ChefMate/users/refresh`
* **Request (JSON):** 
  ```json
  { "refresh_token": "eyJh..." }
  ```
* **Response (200 OK):** Misma estructura que API `/auth/me` .

### 4. Configuración de Usuario (Update)
* **Endpoint:** `PUT /ChefMate/users/{id}`
* **Request (JSON):** Todos campos opcionales (`username`, `email`, `password`)

### 5. Destrucción de Sesión (Logout)
* **Endpoint:** `PATCH /ChefMate/users/logout`
* **Response (200 OK):** `{"success": true, "message": "Logout successful"}`


## 🥩 Endpoints de Gestión de Inventario (CRUD)

### 1. Listar Productos
* **Endpoint:** `GET /ChefMate/products`
* **Response (200 OK):**
  ```json
  {
    "products": [
      {
        "id": "65b9c2a3...",
        "name": "Costillar de Cerdo",
        "quantity": 12.5,
        "unit": "Kg",
        "category": "Carnes",
        "expiration_date": "2024-12-31T00:00:00"
      }
    ]
  }
  ```

### 2. Guardar/Añadir Ingrediente
* **Endpoint:** `POST /ChefMate/products`
* **Request (JSON):**
  ```json
  {
    "name": "Costillar de Cerdo",
    "quantity": 12.5,
    "unit": "Kg",
    "category": "Carnes",
    "expiration_date": "2024-12-31" 
  }
  ```
  *(La fecha puede ser enviada omitida/null).*

### 3. Actualizar
* **Endpoint:** `PUT /ChefMate/products/{id}`
* **Request (JSON):** Mismos datos que POST.

### 4. Eliminar
* **Endpoint:** `DELETE /ChefMate/products/{id}`

---

## 🛡️ Manejo de Seguridad y CAPTCHA (Login)

El sistema de autenticación de ChefMate utiliza **Google reCAPTCHA** obligatoriamente en el endpoint de Login para bloquear ataques de fuerza bruta.

**Operativa en la App Android:**
1. El backend exige el parámetro estricto `captcha_token` dentro de los datos del `Form` (`application/x-www-form-urlencoded`) al invocar `POST /ChefMate/users/login`.
2. Para que la petición HTTP no sea rechazada con un Error 400 Bad Request o un 403 Forbidden, la App debe integrar una solución nativa de reCAPTCHA para Android (como **SafetyNet reCAPTCHA API** o un `WebView` oculto que intercepte y extraiga el código dinámico de Google).
3. Ese token descifrado debe ser inyectado como `captcha_token=<token>` junto con el `username` y `password`.
4. El Backend se encarga de contactar la API de la oficina central de Google de manera Server-to-Server para certificar matemáticamente si ese pase es real o fabricado antes de otorgar el `access_token`.
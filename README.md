# 👨‍🍳 ChefMate AI
> **Asistente culinario inteligente para la gestión de inventario y reducción del desperdicio de alimentos mediante IA Generativa.**

ChefMate AI es un ecosistema integral diseñado para optimizar el consumo de alimentos en el hogar. El sistema utiliza modelos de lenguaje de gran escala (LLM) para transformar el inventario disponible de un usuario en recetas creativas, personalizadas según su ubicación geográfica, restricciones dietéticas y disponibilidad de tiempo.

---

## 🚀 Características Principales
- **Gestión de Inventario Inteligente:** Registro y control de productos con seguimiento de cantidades y unidades.
- **Generación de Recetas Dinámicas:** Motor de IA que prioriza el uso de ingredientes próximos a vencer para reducir el desperdicio.
- **Parametrización Regional:** Adaptación cultural de las recetas basada en la ubicación del usuario (ej. Gastronomía boliviana).
- **Arquitectura Híbrida de IA:** Soporte para inferencia local mediante **Ollama (Llama 3)** para máxima privacidad, o integración con **Google Gemini API** para mayor capacidad de procesamiento.
- **Interfaz Multiplataforma:** Aplicación móvil nativa para el consumidor final y panel de administración web para gestión de datos.

---

## 🛠️ Stack Tecnológico

### Backend (Core API)
- **Lenguaje:** Python 3.12+
- **Framework:** [FastAPI](https://fastapi.tiangolo.com/) (Asíncrono y de alto rendimiento).
- **Base de Datos:** [MongoDB](https://www.mongodb.com/) con Driver Motor (Async).
- **Validación de Datos:** Pydantic V2.
- **Contenerización:** Docker & Docker Compose.

### Mobile App
- **Lenguaje:** Kotlin.
- **UI Framework:** Jetpack Compose (UI Declarativa).
- **Arquitectura:** MVVM (Model-View-ViewModel).
- **Networking:** Retrofit + OkHttp (Manejo de Cookies HttpOnly).

### Inteligencia Artificial
- **Inferencia Local:** [Ollama](https://ollama.com/) ejecutando Llama 3 (8B).
- **Inferencia Cloud:** Google Gemini 1.5 Flash SDK.
- **Patrón:** RAG (*Retrieval-Augmented Generation*) mediante inyección de contexto en prompts parametrizados.

---

## 📐 Arquitectura del Sistema
El proyecto sigue principios de **Clean Architecture** en el backend para asegurar la escalabilidad y mantenibilidad. La comunicación entre componentes se realiza mediante una API RESTful protegida.

1. **Capa de Infraestructura:** Contenedores Docker que alojan el servidor FastAPI y la instancia de MongoDB.
2. **Capa de Dominio:** Lógica de negocio para la gestión de productos y validación de usuarios.
3. **Capa de Aplicación:** Integración con los motores de IA para el procesamiento de recetas en formato JSON estructurado.

---

## 📦 Instalación y Despliegue (Quick Start)

### Requisitos Previos
- Docker y Docker Compose instalados en el sistema.
- Ollama instalado (solo si se desea ejecutar la inferencia de IA de forma local).

### Pasos
1. Clonar el repositorio:
   ```bash
   git clone [[https://github.com/tu-usuario/chefmate-ai.git](https://github.com/Jfernand3z/ChefMate)]

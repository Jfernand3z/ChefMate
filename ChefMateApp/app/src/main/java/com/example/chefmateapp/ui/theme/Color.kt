package com.example.chefmateapp.ui.theme

import androidx.compose.ui.graphics.Color

// 🟢 Colores Base (Marca)
val Primary = Color(0xFF10B981)       // Verde Albahaca (Botones principales, header)
val PrimaryHover = Color(0xFF059669)   // Verde más oscuro al interactuar
val PrimaryLight = Color(0xFFD1FAE5)   // Verde muy pálido

val Accent = Color(0xFFF97316)        // Naranja Zanahoria (Botones flotantes, FAB)
val AccentHover = Color(0xFFEA580C)    // Naranja más oscuro
val AccentLight = Color(0xFFFFEDD5)    // Naranja muy suave

// ⚪ Fondos y Superficies
val Background = Color(0xFFF8FAFC)    // Blanco Humo (Fondo general de la pantalla)
val Surface = Color(0xFFFFFFFF)       // Blanco Puro (Cards, modales)
val SurfaceSecondary = Color(0xFFF1F5F9) // Gris muy claro (Alternancia)

// ⚫ Tipografía
val TextPrimary = Color(0xFF0F172A)   // Gris Pizarra oscuro (Títulos)
val TextSecondary = Color(0xFF64748B) // Gris Ceniza (Párrafos, descripciones)
val TextMuted = Color(0xFF94A3B8)     // Gris claro (Placeholders)

// 🔴 Colores Semánticos (Alertas, IA, Caducidad)
val Danger = Color(0xFFEF4444)        // Rojo (Botón de borrar, producto caducado)
val DangerHover = Color(0xFFDC2626)
val DangerLight = Color(0xFFFEE2E2)

val Warning = Color(0xFFF59E0B)       // Amarillo (Próximo a vencer)
val WarningHover = Color(0xFFD97706)
val WarningLight = Color(0xFFFEF3C7)

val Info = Color(0xFF3B82F6)          // Azul (Agente IA)
val InfoHover = Color(0xFF2563EB)
val InfoLight = Color(0xFFDBEAFE)

// 🔲 Bordes y Divisores
val BorderLight = Color(0xFFE2E8F0)   // Bordes sutiles para tarjetas
val BorderMedium = Color(0xFFCBD5E1)  // Bordes para inputs inactivos
val BorderFocus = Color(0xFF10B981)   // Borde activo (Mismo que Primary)
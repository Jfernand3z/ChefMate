/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // 🟢 Colores Base (Marca)
                primary: {
                    DEFAULT: '#10B981', // Verde Albahaca (Botones principales, header)
                    hover: '#059669',   // Verde más oscuro al pasar el mouse
                    light: '#D1FAE5',   // Verde muy pálido (para fondos de ítems seleccionados)
                },
                accent: {
                    DEFAULT: '#F97316', // Naranja Zanahoria (Botones flotantes, llamadas a la acción)
                    hover: '#EA580C',   // Naranja más oscuro al interactuar
                    light: '#FFEDD5',   // Naranja muy suave para destacar etiquetas
                },

                // ⚪ Fondos y Superficies
                background: '#F8FAFC',    // Blanco Humo (Fondo general de la pantalla)
                surface: {
                    DEFAULT: '#FFFFFF',     // Blanco Puro (Cards, modales, menús)
                    secondary: '#F1F5F9',   // Gris muy claro (Para alternar colores en tablas)
                },

                // ⚫ Tipografía
                text: {
                    primary: '#0F172A',     // Gris Pizarra oscuro (Títulos h1, h2, h3)
                    secondary: '#64748B',   // Gris Ceniza (Párrafos, descripciones)
                    muted: '#94A3B8',       // Gris claro (Placeholders de inputs, textos de ayuda)
                },

                // 🔴 Colores Semánticos (Alertas, IA, Caducidad)
                danger: {
                    DEFAULT: '#EF4444',     // Rojo (Botón de borrar, producto caducado)
                    hover: '#DC2626',
                    light: '#FEE2E2',       // Fondo suave para banners de error
                },
                warning: {
                    DEFAULT: '#F59E0B',     // Amarillo (Próximo a vencer)
                    hover: '#D97706',
                    light: '#FEF3C7',       // Fondo suave para alertas del sistema
                },
                info: {
                    DEFAULT: '#3B82F6',     // Azul (Botones o textos del Agente IA)
                    hover: '#2563EB',
                    light: '#DBEAFE',       // Fondo suave para las burbujas de chat de la IA
                },

                // 🔲 Bordes y Divisores
                border: {
                    light: '#E2E8F0',       // Bordes muy sutiles para separar las tarjetas
                    medium: '#CBD5E1',      // Bordes para los inputs inactivos
                    focus: '#10B981',       // Borde verde cuando el usuario hace clic en un input
                }
            },

            // 🔤 Configuración de Fuentes
            fontFamily: {
                // 'sans' es la fuente que Tailwind aplica por defecto a todo
                sans: ['Inter', 'sans-serif'],
                // 'heading' la usaremos manualmente en títulos: className="font-heading"
                heading: ['Poppins', 'sans-serif'],
            }
        },
    },
    plugins: [],
}
from typing import Optional, List

def build_recipe_prompt(
    products: list,
    servings: int,
    excluded_recipes: list,
    location: Optional[str] = None,
    selected_products: Optional[List[str]] = None,
    priority_product: Optional[str] = None,
    recipe_type: Optional[str] = None,
) -> str:
    if selected_products:
        selected_lower = [s.lower() for s in selected_products]
        products = [p for p in products if p["name"].lower() in selected_lower]

    product_lines = "\n".join(
        f"  - {p['name']}: {round(p['quantity'], 4)} {p['unit']} disponibles"
        for p in products
    )

    excluded_block = ""
    if excluded_recipes:
        names = "\n".join(f"  - {r}" for r in excluded_recipes)
        excluded_block = f"\nRECETAS YA USADAS — NO las repitas ni hagas variantes triviales de ellas:\n{names}\n"

    location_block = (
        f"\nCONTEXTO GASTRONÓMICO: Adapta las recetas a la tradición culinaria de {location}, "
        f"usando técnicas y sabores propios de esa región.\n"
        if location else ""
    )
    priority_block = (
        f"\nINGREDIENTE PROTAGONISTA: '{priority_product}' debe ser el ingrediente principal "
        f"y más destacado en al menos 3 de las 5 recetas.\n"
        if priority_product else ""
    )
    type_block = (
        f"\nCATEGORÍA: Genera ÚNICAMENTE recetas de tipo '{recipe_type}'. "
        f"{'Platos principales o de fondo, no aperitivos ni bebidas.' if recipe_type == 'plato' else 'Postres, pasteles, galletas, panes dulces y similares.'}\n"
        if recipe_type else ""
    )

    return f"""Eres un CHEF EXPERTO en cocina casera latinoamericana y boliviana. Tu especialidad es crear platos equilibrados: ni muy simples ni excesivamente complejos, perfectos para cocinar en casa pero con un toque especial.

MISIÓN: Crea EXACTAMENTE 5 recetas prácticas pero deliciosas usando únicamente los ingredientes listados.
{location_block}{type_block}{priority_block}
INGREDIENTES DISPONIBLES (no puedes usar ningún otro):
{product_lines}
{excluded_block}
ESTÁNDARES DE CALIDAD OBLIGATORIOS — Cada receta DEBE cumplir todos estos puntos:

1. COMPLEJIDAD EQUILIBRADA: Las recetas deben ser accesibles pero interesantes. Entre 3 y 6 pasos de preparación.
   - Usa técnicas comunes (sofrito, horneado, salteado, hervido, marinado corto).
   - Evita recetas excesivamente básicas ("mezclar y servir"), pero también evita procesos de nivel Michelin de horas de duración.

2. NOMBRE ATRACTIVO: Un nombre apetitoso y claro (ej: "Pollo Dorado con Arroz a las Hierbas").

3. DESCRIPCIÓN APETITOSA: 1-2 oraciones que describan el plato y por qué es delicioso.

4. PASOS CLAROS:
   - Explica de forma sencilla pero específica los tiempos y temperaturas.
   - Ejemplo: "Calienta aceite a fuego medio. Dora el pollo durante 5 minutos por lado hasta que esté bien cocido."

5. TIEMPO REALISTA: El tiempo total de preparación debe estar entre 20 y 60 minutos.

6. INGREDIENTES BIEN DOSIFICADOS: Cantidades lógicas por porción.
   - La cantidad × {servings} NO debe superar el stock disponible de cada ingrediente.

TIPOS DE PLATOS ACEPTADOS (elige variedad entre las 5 recetas):
- Platos principales prácticos
- Sopas o cremas caseras
- Arroces o pastas con buen sabor
- Guisos rápidos o ensaladas completas

PROHIBIDO generar:
- Recetas imposibles de hacer en una cocina normal.
- Recetas con 1 o 2 pasos (demasiado simples).
- Platos con un solo ingrediente principal sin acompañamiento.

FORMATO: Devuelve ÚNICAMENTE un array JSON válido. Sin texto extra, sin markdown, sin explicaciones.

[
  {{
    "name": "Nombre Atractivo del Plato",
    "type": "plato",
    "description": "Breve descripción apetitosa del plato.",
    "ingredients": [
      {{"name": "NombreIngrediente", "quantity": 0.15, "unit": "kg"}},
      {{"name": "OtroIngrediente", "quantity": 0.05, "unit": "kg"}}
    ],
    "steps": [
      "Paso 1 explicado de forma clara y práctica.",
      "Paso 2 con tiempos y temperaturas simples.",
      "Paso 3 indicando cómo integrar los ingredientes."
    ],
    "prep_time_minutes": 35
  }}
]

RECUERDA: 'quantity' es para 1 PORCIÓN. (quantity × {servings}) no debe superar el stock disponible.
Comienza directamente con '[' sin texto previo."""

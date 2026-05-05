from fastapi import HTTPException, status
from bson import ObjectId
from config.db import database
from api.utils.recipe_utils.unit_converter import convert_to_base

async def cook_saved_recipe(recipe_id: str, servings: int) -> dict:
    try:
        oid = ObjectId(recipe_id)
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="ID de receta inválido.",
        )

    recipe = await database.recipe.find_one({"_id": oid})
    if not recipe:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Receta no encontrada.",
        )

    for ingredient in recipe.get("ingredients", []):
        name = ingredient.get("name")
        qty_per_serving = float(ingredient.get("quantity", 0))
        ingredient_unit = ingredient.get("unit", "unidad").strip()
        total_needed = qty_per_serving * servings

        product = await database.product.find_one(
            {"name": {"$regex": f"^{name}$", "$options": "i"}}
        )
        if not product:
            continue

        product_unit = product.get("unit", "unidad").strip()

        # Convertir la cantidad necesaria a la unidad base
        needed_base, needed_type = convert_to_base(total_needed, ingredient_unit)
        product_base, product_type = convert_to_base(float(product["quantity"]), product_unit)

        if product_type == needed_type and product_type != "unknown":
            # Calcular cuánto queda en la unidad BASE y convertir de vuelta a la unidad del producto
            remaining_base = max(0.0, product_base - needed_base)
            # Factor de conversión inverso: de base a unidad del producto
            _, product_factor = _get_inverse_factor(product_unit, product_type)
            remaining_in_product_unit = remaining_base / product_factor if product_factor else remaining_base
        else:
            # Fallback: misma unidad, resta directa
            remaining_in_product_unit = max(0.0, float(product["quantity"]) - total_needed)

        await database.product.update_one(
            {"_id": product["_id"]},
            {"$set": {"quantity": round(remaining_in_product_unit, 6)}},
        )

    return {"success": True, "message": f"Ingredientes descontados para {servings} porción(es)."}


def _get_inverse_factor(unit: str, unit_type: str) -> tuple:
    """Retorna el factor para convertir de la unidad base a la unidad original."""
    from api.utils.recipe_utils.unit_converter import (
        VOLUME_TO_LITERS, MASS_TO_KG, UNIT_TO_BASE
    )
    key = unit.lower().strip()
    if unit_type == "volume":
        factor = VOLUME_TO_LITERS.get(key, 1.0)
    elif unit_type == "mass":
        factor = MASS_TO_KG.get(key, 1.0)
    else:
        factor = UNIT_TO_BASE.get(key, 1.0)
    return unit_type, factor


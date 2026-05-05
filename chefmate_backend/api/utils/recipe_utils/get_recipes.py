from config.db import database
from api.schemas.recipe_schemas.recipe_entity import recipeEntity
from api.utils.recipe_utils.unit_converter import max_servings_from_stock

async def get_recipes() -> list:
    recipes_cursor = database.recipe.find()
    recipes = await recipes_cursor.to_list(length=None)

    result = []
    for recipe in recipes:
        max_servings = await _calculate_max_servings(recipe)
        recipe_dict = recipeEntity(recipe)
        recipe_dict["max_servings"] = max_servings
        result.append(recipe_dict)

    return result


async def _calculate_max_servings(recipe: dict) -> int:
    ingredients = recipe.get("ingredients", [])
    if not ingredients:
        return 0

    min_servings = float("inf")

    for ingredient in ingredients:
        name = ingredient.get("name")
        qty_per_serving = ingredient.get("quantity", 0)
        ingredient_unit = ingredient.get("unit", "unidad").strip()

        if qty_per_serving <= 0:
            continue

        product = await database.product.find_one(
            {"name": {"$regex": f"^{name}$", "$options": "i"}}
        )
        if not product:
            return 0

        product_unit = product.get("unit", "unidad").strip()
        possible = max_servings_from_stock(
            product_qty=float(product["quantity"]),
            product_unit=product_unit,
            needed_qty_per_serving=float(qty_per_serving),
            needed_unit=ingredient_unit,
        )

        if possible < min_servings:
            min_servings = possible

    return 0 if min_servings == float("inf") else min_servings


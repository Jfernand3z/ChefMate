from config.db import database
from api.schemas.recipe_schemas.recipe_entity import recipeEntity

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

        if qty_per_serving <= 0:
            continue

        product = await database.product.find_one(
            {"name": {"$regex": f"^{name}$", "$options": "i"}}
        )
        if not product:
            return 0

        possible = int(product["quantity"] / qty_per_serving)
        if possible < min_servings:
            min_servings = possible

    return 0 if min_servings == float("inf") else min_servings

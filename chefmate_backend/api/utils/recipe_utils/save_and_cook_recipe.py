from datetime import datetime
from fastapi import HTTPException, status
from bson import ObjectId
from config.db import database
from api.schemas.recipe_schemas.recipe_entity import recipeEntity

async def save_and_cook_recipe(recipe_data: dict, servings: int) -> dict:
    recipe_to_save = {
        "name": recipe_data.get("name"),
        "type": recipe_data.get("type"),
        "description": recipe_data.get("description"),
        "ingredients": recipe_data.get("ingredients", []),
        "steps": recipe_data.get("steps", []),
        "prep_time_minutes": recipe_data.get("prep_time_minutes"),
        "location": recipe_data.get("location"),
        "created_at": datetime.now(),
    }

    result = await database.recipe.insert_one(recipe_to_save)
    if not result:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error al guardar la receta en la base de datos.",
        )

    await _discount_ingredients(recipe_to_save["ingredients"], servings)

    saved = await database.recipe.find_one({"_id": result.inserted_id})
    return recipeEntity(saved)


async def _discount_ingredients(ingredients: list, servings: int):
    for ingredient in ingredients:
        name = ingredient.get("name")
        qty_per_serving = ingredient.get("quantity", 0)
        total_to_discount = qty_per_serving * servings

        product = await database.product.find_one(
            {"name": {"$regex": f"^{name}$", "$options": "i"}}
        )
        if not product:
            continue

        new_quantity = max(0.0, product["quantity"] - total_to_discount)
        await database.product.update_one(
            {"_id": product["_id"]},
            {"$set": {"quantity": new_quantity}},
        )

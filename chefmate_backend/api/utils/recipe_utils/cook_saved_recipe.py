from fastapi import HTTPException, status
from bson import ObjectId
from config.db import database

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

    return {"success": True, "message": f"Ingredientes descontados para {servings} porción(es)."}

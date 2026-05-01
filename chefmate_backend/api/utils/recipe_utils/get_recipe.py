from fastapi import HTTPException, status
from bson import ObjectId
from config.db import database
from api.schemas.recipe_schemas.recipe_entity import recipeEntity

async def get_recipe(recipe_id: str) -> dict:
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

    return recipeEntity(recipe)

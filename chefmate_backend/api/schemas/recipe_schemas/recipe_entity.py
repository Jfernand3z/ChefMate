def recipeEntity(recipe) -> dict:
    return {
        "id": str(recipe["_id"]),
        "name": recipe.get("name"),
        "type": recipe.get("type"),
        "description": recipe.get("description"),
        "ingredients": recipe.get("ingredients", []),
        "steps": recipe.get("steps", []),
        "prep_time_minutes": recipe.get("prep_time_minutes"),
        "location": recipe.get("location"),
        "created_at": recipe["created_at"].isoformat() if recipe.get("created_at") else None,
    }

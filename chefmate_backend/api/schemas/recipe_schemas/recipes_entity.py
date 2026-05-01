from api.schemas.recipe_schemas.recipe_entity import recipeEntity

def recipesEntity(recipes: list) -> list:
    return [recipeEntity(recipe) for recipe in recipes]

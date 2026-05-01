from fastapi import APIRouter, Depends
from typing import Annotated
from api.authentication.auth import get_current_user
from api.models.recipe_models.recipe_generate_model import RecipeGenerateRequest
from api.models.recipe_models.recipe_cook_new_model import RecipeCookNewRequest
from api.models.recipe_models.recipe_history_cook_model import RecipeHistoryCookRequest
from api.utils.recipe_utils.generate_recipes import generate_recipes
from api.utils.recipe_utils.save_and_cook_recipe import save_and_cook_recipe
from api.utils.recipe_utils.cook_saved_recipe import cook_saved_recipe
from api.utils.recipe_utils.get_recipes import get_recipes
from api.utils.recipe_utils.get_recipe import get_recipe

recipe = APIRouter()

@recipe.post("/recipes/generate")
async def generate_recipes_route(
    params: RecipeGenerateRequest,
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await generate_recipes(params.dict())

@recipe.post("/recipes/cook")
async def cook_new_recipe_route(
    body: RecipeCookNewRequest,
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await save_and_cook_recipe(body.recipe, body.servings)

@recipe.post("/recipes/{id}/cook")
async def cook_saved_recipe_route(
    id: str,
    body: RecipeHistoryCookRequest,
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await cook_saved_recipe(id, body.servings)

@recipe.get("/recipes")
async def get_recipes_route(
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await get_recipes()

@recipe.get("/recipes/{id}")
async def get_recipe_route(
    id: str,
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await get_recipe(id)

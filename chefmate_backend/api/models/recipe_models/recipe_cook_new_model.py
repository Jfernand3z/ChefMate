from pydantic import BaseModel, Field

class RecipeCookNewRequest(BaseModel):
    recipe: dict
    servings: int = Field(..., gt=0)

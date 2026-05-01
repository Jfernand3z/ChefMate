from pydantic import BaseModel, Field

class RecipeHistoryCookRequest(BaseModel):
    servings: int = Field(..., gt=0)

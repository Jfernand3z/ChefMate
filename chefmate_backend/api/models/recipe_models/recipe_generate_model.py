from pydantic import BaseModel, Field
from typing import Optional, List

class RecipeGenerateRequest(BaseModel):
    servings: int = Field(..., gt=0)
    location: Optional[str] = None
    selected_products: Optional[List[str]] = None
    priority_product: Optional[str] = None
    recipe_type: Optional[str] = None

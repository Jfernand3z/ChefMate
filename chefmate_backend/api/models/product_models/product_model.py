from datetime import date
from pydantic import BaseModel, Field
from typing import Optional

class Product(BaseModel):
    name: str 
    quantity: float = Field(..., gt=0)
    unit: str 
    expiration_date: Optional[date] = None
    category: Optional[str] = None
from datetime import date
from pydantic import BaseModel, Field
from typing import Optional

class ProductBase(BaseModel):
    name: Optional[str] = None 
    quantity: Optional[float] = Field(None, gt=0)
    unit: Optional[str] = None
    expiration_date: Optional[date] = None
    category: Optional[str] = None
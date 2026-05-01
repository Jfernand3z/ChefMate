from datetime import datetime, date
from fastapi.responses import JSONResponse
from fastapi import status
from api.schemas.product_schemas.product_entity import productEntity
from config.db import database

async def create_product(product: dict):
    if "expiration_date" in product and isinstance(product["expiration_date"], date) and not isinstance(product["expiration_date"], datetime):
        product["expiration_date"] = datetime.combine(product["expiration_date"], datetime.min.time())
    result = await database.product.insert_one(product)
    if result:
        new_product = await database.product.find_one({"_id": result.inserted_id})
        return productEntity(new_product)
    else:
        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content={
                "success": False,
                "errors": [{"field": "database", "message": "Failed to create product"}],
            }
        )
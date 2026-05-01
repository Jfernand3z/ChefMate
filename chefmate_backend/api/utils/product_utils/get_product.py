from bson import ObjectId
from fastapi import status
from fastapi.responses import JSONResponse
from api.schemas.product_schemas.product_entity import productEntity
from config.db import database


async def get_product(id: str):
    product = await database.product.find_one({"_id": ObjectId(id)})
    if product:
        return productEntity(product)
    else:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={
                "success": False,
                "errors": [{"field": "id", "message": "Product not found"}],
            },
        )
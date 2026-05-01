from datetime import datetime, date
from fastapi import status
from fastapi.responses import JSONResponse
from config.db import database
from api.schemas.product_schemas.product_entity import productEntity
from bson import ObjectId

async def update_product(id: str, product: dict):
    if "expiration_date" in product and isinstance(product["expiration_date"], date) and not isinstance(product["expiration_date"], datetime):
        product["expiration_date"] = datetime.combine(product["expiration_date"], datetime.min.time())
    result = await database.product.update_one(
        {"_id": ObjectId(id)},
        {"$set": product}
    )
    if result.matched_count == 0:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={
                "success": False,
                "errors": [{"field": "id", "message": "Product not found"}],
            },
        )
    product = await database.product.find_one({"_id": ObjectId(id)})
    return productEntity(product)
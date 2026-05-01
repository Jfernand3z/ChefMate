from config.db import database
from bson import ObjectId
from fastapi import status
from fastapi.responses import JSONResponse

async def delete_product(id: str):
    result = await database.product.delete_one({"_id": ObjectId(id)})
    if result.deleted_count == 0:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={
                "success": False,
                "errors": [{"field": "id", "message": "Product not found"}],
            },
        )
    return JSONResponse(
        status_code=status.HTTP_200_OK,
        content={"success": True, "message": "Product deleted successfully"},
    )
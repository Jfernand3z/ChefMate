from fastapi import APIRouter, Depends
from typing import Annotated
from api.authentication.auth import get_current_user
from api.utils.product_utils.report_products import report_product
from api.utils.product_utils.create_product import create_product
from api.utils.product_utils.delete_product import delete_product
from api.utils.product_utils.get_distribucion_category import get_category_distribution_for_chart
from api.utils.product_utils.get_products import get_products
from api.utils.product_utils.get_product import get_product
from api.utils.product_utils.update_product import update_product
from api.models.product_models.product_model import Product

product = APIRouter()

@product.post("/products")
async def create_product_route(product_data: Product, current_user: Annotated[dict, Depends(get_current_user)]):
    return await create_product(product_data.dict())

@product.get("/products")
async def get_products_route(current_user: Annotated[dict, Depends(get_current_user)]):
    return await get_products()

@product.get("/products/report")
async def report_products_route(current_user: Annotated[dict, Depends(get_current_user)]):
    return await report_product()

@product.get("/products/distribution/category")
async def get_category_distribution_route(current_user: Annotated[dict, Depends(get_current_user)]):
    return await get_category_distribution_for_chart()

@product.get("/products/{id}")
async def get_product_route(id: str, current_user: Annotated[dict, Depends(get_current_user)]):
    return await get_product(id)

@product.put("/products/{id}")
async def update_product_route(id: str, product_data: Product, current_user: Annotated[dict, Depends(get_current_user)]):
    return await update_product(id, product_data.dict())

@product.delete("/products/{id}")
async def delete_product_route(id: str, current_user: Annotated[dict, Depends(get_current_user)]):
    return await delete_product(id)
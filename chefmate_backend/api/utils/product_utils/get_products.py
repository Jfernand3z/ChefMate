from config.db import database
from api.schemas.product_schemas.products_entity import productsEntity

async def get_products():
    products_cursor = database.product.find()
    products = await products_cursor.to_list(length=None)
    return productsEntity(products)
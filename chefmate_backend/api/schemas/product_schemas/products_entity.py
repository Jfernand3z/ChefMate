from api.schemas.product_schemas.product_entity import productEntity

def productsEntity(products: list):
    return [productEntity(product) for product in products]
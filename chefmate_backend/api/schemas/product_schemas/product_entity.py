def productEntity(product) -> dict:
    return {
        "id": str(product["_id"]),
        "name": product["name"],
        "quantity": product["quantity"],
        "unit": product["unit"],
        "expiration_date": product["expiration_date"].isoformat() if product.get("expiration_date") else None,
        "category": product['category'] if 'category' in product else None
    }
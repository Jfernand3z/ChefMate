from datetime import datetime
from config.db import database
from api.utils.recipe_utils.build_prompt import build_recipe_prompt
from api.utils.recipe_utils.gemini_service import ask_gemini

async def generate_recipes(params: dict) -> list:
    servings: int = params.get("servings")
    location: str = params.get("location")
    selected_products: list = params.get("selected_products")
    priority_product: str = params.get("priority_product")
    recipe_type: str = params.get("recipe_type")

    today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
    product_query = {
        "$and": [
            {"quantity": {"$gt": 0}},
            {
                "$or": [
                    {"expiration_date": None},
                    {"expiration_date": {"$gt": today}},
                ]
            },
        ]
    }
    products_cursor = database.product.find(product_query)
    products = await products_cursor.to_list(length=None)

    available_products = [
        {
            "name": p["name"],
            "quantity": p["quantity"],
            "unit": p["unit"],
        }
        for p in products
    ]

    recipes_cursor = database.recipe.find({}, {"name": 1})
    saved_recipes = await recipes_cursor.to_list(length=None)
    excluded_names = [r["name"] for r in saved_recipes]

    prompt = build_recipe_prompt(
        products=available_products,
        servings=servings,
        excluded_recipes=excluded_names,
        location=location,
        selected_products=selected_products,
        priority_product=priority_product,
        recipe_type=recipe_type,
    )

    recipes = await ask_gemini(prompt)
    return recipes[:5]

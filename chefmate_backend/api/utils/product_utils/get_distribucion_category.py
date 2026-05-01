from config.db import database

async def get_category_distribution_for_chart() -> list[dict]:
    pipeline = [
        {"$group": {
            "_id": "$category", 
            "value": {"$sum": 1}
        }},
       
        {"$project": {
            "name": {"$ifNull": ["$_id", "Sin Categoría"]},
            "value": 1,
            "_id": 0 
        }},
        {"$sort": {"value": -1}}
    ]
    
    cursor = database.product.aggregate(pipeline)
    result = await cursor.to_list(length=None)
    
    return result
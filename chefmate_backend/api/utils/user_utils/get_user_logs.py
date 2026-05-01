from config.db import database

async def get_user_logs_dashboard(user_id: str) -> dict:
    pipeline = [
        {"$match": {"user_id": user_id}},
        
        {"$facet": {
            "actions_distribution": [
                {"$group": {"_id": "$action", "value": {"$sum": 1}}},
                {"$project": {"name": "$_id", "value": 1, "_id": 0}}
            ],
            
            "browser_distribution": [
                {"$group": {"_id": "$browser", "value": {"$sum": 1}}},
                {"$project": {"name": "$_id", "value": 1, "_id": 0}},
                {"$sort": {"value": -1}} 
            ],
            
            "timeline": [
                {"$group": {
                    "_id": {"$dateToString": {"format": "%Y-%m-%d", "date": "$date"}},
                    "activity_count": {"$sum": 1}
                }},
                {"$sort": {"_id": 1}},
                {"$project": {"date": "$_id", "activity_count": 1, "_id": 0}}
            ],
            
            "recent_activity": [
                {"$sort": {"date": -1}}, 
                {"$limit": 10},
                {"$project": {"_id": 0, "date": 1, "action": 1, "browser": 1, "ip": 1}}
            ]
        }}
    ]
    
    cursor = database.access_logs.aggregate(pipeline)
    result = await cursor.to_list(length=1)
    
    if result and len(result) > 0:
        return result[0]
    
    return {
        "actions_distribution": [],
        "browser_distribution": [],
        "timeline": [],
        "recent_activity": []
    }
from bson import ObjectId
from config.db import database

async def delete_user(id: str):
    try:
        result = await database.user.delete_one({"_id": ObjectId(id)})
        result2 = await database.access_logs.delete_many({"user_id": id})
        if result.deleted_count == 0 or result2.deleted_count == 0:
            return {
                "success": False,
                "error": "User not found"
            }
    except Exception as e:
        return {
            "success": False,
            "error": str(e)
        }

    return {
        "success": True,
        "message": "User deleted successfully"
    }
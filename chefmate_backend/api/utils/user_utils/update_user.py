from bson import ObjectId
from fastapi import status
from fastapi.responses import JSONResponse
from config.db import database
from api.authentication.auth import get_password_hash
from api.schemas.user_schemas.user_get_entity import UserGetEntity

async def update_user(id: str, user: dict):
    actual_user = await database.user.find_one({"_id": ObjectId(id)})
    if not actual_user:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={
                "success": False,
                "errors": [{"field": "id", "message": "User not found with the provided ID."}],
            },
        )
    existing_user = await database.user.find_one(
        {
            "$and": [
                {"_id": {"$ne": actual_user["_id"]}},
                {"$or": [{"email": user.get("email")}, {"username": user.get("username")}]}
            ]
        }
    )
    if existing_user:
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={
                "success": False,
                "errors": [
                    {
                        "field": "username or email",
                        "message": f"The username '{user['username']}' or email '{user['email']}' already exists.",
                    }
                ],
            },
        )
    if "email" not in user or not user["email"]:
        user["email"] = actual_user["email"]
    if "username" not in user or not user["username"]:
        user["username"] = actual_user["username"]
    if "logs" not in user or not user["logs"]:
        user["logs"] = actual_user["logs"]
    if "password" not in user or not user["password"]:
        user["password"] = actual_user["password"]
    else:
        user["password"] = get_password_hash(user["password"])

    new_user_dict = user.copy()

    await database.user.find_one_and_update(
        {"_id": ObjectId(id)}, {"$set": new_user_dict}
    )
    newuser = await database.user.find_one({"_id": ObjectId(id)}, {"password": 0})
    return UserGetEntity(newuser)
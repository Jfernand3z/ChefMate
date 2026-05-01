from bson import ObjectId
from fastapi import status
from fastapi.responses import JSONResponse
from config.db import database
from api.authentication.auth import get_password_hash
from api.schemas.user_schemas.user_get_entity import UserGetEntity

async def register_user(user: dict):
    existing_user = await database.user.find_one(
        {"$or": [{"email": user.email}, {"username": user.username}]}
    )   

    if existing_user:
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={
                "success": False,
                "errors": [
                    {
                        "field": "email/username",
                        "message": "Email or username already exists. Please choose a different one.",
                    }
                ],
            },
        )

    user_dict = user.dict()
    user_dict["password"] = get_password_hash(user_dict["password"])
    result = await database.user.insert_one(user_dict)
    id = result.inserted_id
    new_user = await database.user.find_one({"_id": ObjectId(id)}, {"password": 0})
    return UserGetEntity(new_user)
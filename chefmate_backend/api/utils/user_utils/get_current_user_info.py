from fastapi import HTTPException
from bson import ObjectId
from api.authentication.auth import verify_token
from config.db import database
from api.schemas.user_schemas.user_get_entity import UserGetEntity

async def get_current_user_info(access_token: str):
    if not access_token:
        raise HTTPException(status_code=401, detail="Token access not provided")

    try:
        payload = verify_token(access_token)
        user_id = payload.get("user_id")
        if not user_id:
            raise HTTPException(status_code=401, detail="Invalid token: user ID missing")

        user = await database.user.find_one({"_id": ObjectId(user_id)}, {"password": 0})
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        return UserGetEntity(user)

    except Exception:
        raise HTTPException(status_code=401, detail="Invalid token or expired")
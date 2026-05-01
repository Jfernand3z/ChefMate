from bson import ObjectId
from datetime import datetime, timedelta, timezone
from fastapi import Response, HTTPException
from api.authentication.auth import (
    ACCESS_TOKEN_EXPIRE_MINUTES,
    REFRESH_TOKEN_EXPIRE_MINUTES,
    create_access_token,
    create_refresh_token,
    verify_refresh_token
)
from enviroment.env import settings
from config.db import database

async def refresh_user(refresh_token: str, response: Response):
    if not refresh_token:
        raise HTTPException(status_code=401, detail="Refresh token requerido")

    payload = verify_refresh_token(refresh_token)
    user_id: str = payload.get("user_id")
    if user_id is None:
        raise HTTPException(status_code=401, detail="Refresh token inválido")

    user = await database.user.find_one({"_id": ObjectId(user_id)})
    if user is None or user.get("refresh_token") != refresh_token:
        raise HTTPException(status_code=401, detail="Refresh token inválido")

    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    refresh_token_expires = timedelta(minutes=REFRESH_TOKEN_EXPIRE_MINUTES)

    new_access_token = create_access_token(
        data={
            "user_id": str(user["_id"]),
            "username": user["username"]
        },
        expires_delta=access_token_expires,
    )

    new_refresh_token = create_refresh_token(
        data={
            "user_id": str(user["_id"]),
            "username": user["username"]
        },
        expires_delta=refresh_token_expires,
    )

    await database.user.update_one(
        {"_id": ObjectId(user_id)}, {"$set": {"refresh_token": new_refresh_token}}
    )

    SECURE_COOKIES = settings.SECURE_COOKIES
    SAMESITE = settings.COOKIE_SAMESITE

    max_age_access = ACCESS_TOKEN_EXPIRE_MINUTES * 60
    max_age_refresh = REFRESH_TOKEN_EXPIRE_MINUTES * 60
    expires_access = datetime.now(timezone.utc) + timedelta(seconds=max_age_access)
    expires_refresh = datetime.now(timezone.utc) + timedelta(seconds=max_age_refresh)

    response.set_cookie(
        key="refresh_token",
        value=new_refresh_token,
        httponly=True,
        secure=SECURE_COOKIES,
        samesite=SAMESITE,
        path="/",
        max_age=max_age_refresh,
        expires=expires_refresh,
    )
    response.set_cookie(
        key="access_token",
        value=new_access_token,
        httponly=True,
        secure=SECURE_COOKIES,
        samesite=SAMESITE,
        path="/",
        max_age=max_age_access,
        expires=expires_access,
    )

    return {
        "success": True,
        "access_token": new_access_token,
        "refresh_token": new_refresh_token,
        "expires_in_minutes": ACCESS_TOKEN_EXPIRE_MINUTES,
        "message": "Token refreshed"
    }
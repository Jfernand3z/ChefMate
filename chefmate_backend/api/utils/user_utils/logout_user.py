from bson import ObjectId
from fastapi import Response, Request
from datetime import datetime
from config.db import database
from enviroment.env import settings
from api.utils.user_utils.browser_utils import get_browser_name

async def logout_user(id: str, request: Request, response: Response):
    try:

        ip = request.client.host
        raw_user_agent = request.headers.get("user-agent", "")
        browser = get_browser_name(raw_user_agent)

        await database.access_logs.insert_one({
            "user_id": id,
            "ip": ip,
            "browser": browser,
            "action": "salida",
            "date": datetime.utcnow()
        })

        await database.user.update_one(
            {"_id": ObjectId(id)},
            {"$set": {"refresh_token": None}}
        )

        SECURE_COOKIES = settings.SECURE_COOKIES
        SAMESITE = settings.COOKIE_SAMESITE

        # 📌 Eliminar cookies
        response.delete_cookie(
            key="access_token",
            httponly=True,
            secure=SECURE_COOKIES,
            samesite=SAMESITE,
            path="/",
        )

        response.delete_cookie(
            key="refresh_token",
            httponly=True,
            secure=SECURE_COOKIES,
            samesite=SAMESITE,
            path="/",
        )

    except Exception as e:
        return {
            "success": False,
            "error": str(e)
        }

    return {
        "success": True,
        "message": "Logout successful"
    }
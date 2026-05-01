from typing import Optional
from fastapi import HTTPException, Request, Response
from api.authentication.auth import extract_access_token_from_request
from api.utils.user_utils.get_current_user_info import get_current_user_info
from api.utils.user_utils.refresh_user import refresh_user

async def authenticate_user(request: Request, response: Response):
    token = None
    try:
        token = extract_access_token_from_request(request)
    except Exception:
        token = None

    if token:
        return await get_current_user_info(token)

    refresh_token = request.cookies.get("refresh_token")
    if refresh_token:
        refresh_result = await refresh_user(refresh_token, response)
        new_access = refresh_result.get("access_token") if isinstance(refresh_result, dict) else None
        if new_access:
            return await get_current_user_info(new_access)

    raise HTTPException(status_code=401, detail="Not authenticated")

async def handle_refresh_token(request: Request, response: Response, refresh_token: Optional[str] = None):
    token = refresh_token
    if not token:
        try:
            body = await request.json()
            token = body.get("refresh_token")
        except Exception:
            token = None
    return await refresh_user(token, response)
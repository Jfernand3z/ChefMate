from bson import ObjectId
from fastapi import status, Request
from fastapi.responses import JSONResponse, Response
from datetime import datetime, timedelta, timezone
from config.db import database
from api.authentication.auth import (
    ACCESS_TOKEN_EXPIRE_MINUTES,
    REFRESH_TOKEN_EXPIRE_MINUTES,
    create_access_token,
    create_refresh_token,
    verify_password
)
from enviroment.env import settings
from api.schemas.user_schemas.user_login_entity import UserLoginEntity
from api.authentication.captcha_service import verify_captcha_token
from api.utils.user_utils.browser_utils import get_browser_name

async def login_user(user: dict, request: Request, response: Response, remember_me: bool = False):
    ip = request.client.host
    raw_user_agent = request.headers.get("user-agent", "")
    browser = get_browser_name(raw_user_agent)

    # 1. VERIFICACIÓN DE CAPTCHA (Filtro inicial)
    # Asumimos que el frontend envía el token en el payload del usuario
    captcha_token = user.get("captcha_token") 
    
    if not captcha_token:
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={
                "success": False,
                "errors": [{"field": "captcha", "message": "Falta el token de verificación CAPTCHA"}],
            },
        )

    is_human = await verify_captcha_token(token=captcha_token, client_ip=ip)
    
    if not is_human:
        return JSONResponse(
            status_code=status.HTTP_403_FORBIDDEN,
            content={
                "success": False,
                "errors": [{"field": "captcha", "message": "Verificación CAPTCHA fallida. Intenta nuevamente."}],
            },
        )

    # 2. LÓGICA DE BASE DE DATOS Y AUTENTICACIÓN
    result = await database.user.find_one({"email": user["email"]})

    if not result:
        return JSONResponse(
            status_code=status.HTTP_401_UNAUTHORIZED,
            content={
                "success": False,
                "errors": [{"field": "email", "message": "Email incorrecto"}],
            },
        )

    if not verify_password(user["password"], result["password"]):
        return JSONResponse(
            status_code=status.HTTP_401_UNAUTHORIZED,
            content={
                "success": False,
                "errors": [{"field": "password", "message": "Contraseña incorrecta"}],
            },
        )

    # 3. GENERACIÓN DE TOKENS
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)

    if remember_me:
        refresh_token_expires = timedelta(minutes=settings.REMEMBER_REFRESH_MINUTES)
    else:
        refresh_token_expires = timedelta(minutes=REFRESH_TOKEN_EXPIRE_MINUTES)

    access_token = create_access_token(
        data={
            "user_id": str(result["_id"]),
            "username": result["username"]
        },
        expires_delta=access_token_expires,
    )

    refresh_token = create_refresh_token(
        data={
            "user_id": str(result["_id"]),
            "username": result["username"]
        },
        expires_delta=refresh_token_expires,
    )

    await database.user.update_one(
        {"_id": ObjectId(result["_id"])},
        {"$set": {"refresh_token": refresh_token}}
    )

    # 4. REGISTRO DE ACCESO
    # (Nota: Se recomienda usar datetime.now(timezone.utc) en lugar de datetime.utcnow() que está deprecado)
    await database.access_logs.insert_one({
        "user_id": str(result["_id"]),
        "username": result["username"],
        "ip": ip,
        "browser": browser,
        "action": "ingreso",
        "date": datetime.now(timezone.utc) 
    })

    # 5. CONFIGURACIÓN DE COOKIES Y RESPUESTA
    SECURE_COOKIES = settings.SECURE_COOKIES
    SAMESITE = settings.COOKIE_SAMESITE

    max_age_refresh = (
        settings.REMEMBER_REFRESH_MINUTES * 60
        if remember_me
        else REFRESH_TOKEN_EXPIRE_MINUTES * 60
    )

    max_age_access = ACCESS_TOKEN_EXPIRE_MINUTES * 60

    expires_access = datetime.now(timezone.utc) + timedelta(seconds=max_age_access)
    expires_refresh = datetime.now(timezone.utc) + timedelta(seconds=max_age_refresh)

    response.set_cookie(
        key="access_token",
        value=access_token,
        httponly=True,
        secure=SECURE_COOKIES,
        samesite=SAMESITE,
        path="/",
        max_age=max_age_access,
        expires=expires_access,
    )

    response.set_cookie(
        key="refresh_token",
        value=refresh_token,
        httponly=True,
        secure=SECURE_COOKIES,
        samesite=SAMESITE,
        path="/",
        max_age=max_age_refresh,
        expires=expires_refresh,
    )

    return UserLoginEntity(result, access_token, refresh_token)
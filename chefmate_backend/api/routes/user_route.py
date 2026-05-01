from fastapi import APIRouter, Cookie, Depends, Request, Response, Form
from fastapi.security import OAuth2PasswordRequestForm
from typing import Annotated, Optional
from api.utils.user_utils.delete_user import delete_user
from api.utils.user_utils.get_user_logs import get_user_logs_dashboard
from api.utils.user_utils.update_user import update_user
from api.utils.user_utils.refresh_user import refresh_user
from api.utils.user_utils.logout_user import logout_user
from api.utils.user_utils.login_user import login_user
from api.utils.user_utils.register_user import register_user
from api.authentication.auth import get_current_user
from api.utils.auth_utils.auth_util import authenticate_user, handle_refresh_token
from api.models.user_models.user_login_model import UserLogin
from api.models.user_models.user_update_model import UserUpdate
from api.models.user_models.user_model import User

user = APIRouter()

@user.post("/users/register")
async def register(user_data: User):
    return await register_user(user_data)

@user.post("/users/login")
async def login_user_route(
    request: Request,
    form_data: Annotated[OAuth2PasswordRequestForm, Depends()],
    response: Response,
    captcha_token: Optional[str] = Form(None)
):

    remember_me = "remember_me" in form_data.scopes if form_data.scopes else False

    user_dict = {
        "email": form_data.username,
        "password": form_data.password,
        "captcha_token": captcha_token
    }

    return await login_user(user_dict, request, response, remember_me)

@user.patch("/users/logout")
async def logout_user_route(
    current_user: Annotated[dict, Depends(get_current_user)], response: Response, request: Request
):
    return await logout_user(current_user["user_id"], request, response)

@user.post("/users/refresh")
async def refresh_token_route(request: Request, response: Response, refresh_token: Optional[str] = Cookie(default=None)):
    return await handle_refresh_token(request, response, refresh_token)

@user.put("/users/{id}")
async def update_user_route(
    id: str,
    user: UserUpdate,
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await update_user(id, user.dict())

@user.get("/auth/me")
async def get_current_user_route(request: Request, response: Response):
    return await authenticate_user(request, response)

@user.delete("/users/{id}")
async def delete_user_route(
    id: str,
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await delete_user(id)

@user.get("/users/logs")
async def get_user_logs_route(
    current_user: Annotated[dict, Depends(get_current_user)],
):
    return await get_user_logs_dashboard(current_user["user_id"])
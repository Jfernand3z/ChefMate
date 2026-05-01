from api.authentication.auth import ACCESS_TOKEN_EXPIRE_MINUTES

def UserLoginEntity(user, access_token, refresh_token=None) -> dict:
    data = {
        "success": True,
        "user": {
            "email": user["email"],
            "username": user["username"]
        },
        "access_token": access_token,
        "expires_in_minutes": ACCESS_TOKEN_EXPIRE_MINUTES
    }
    if refresh_token:
        data["refresh_token"] = refresh_token
    return data
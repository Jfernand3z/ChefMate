from fastapi.responses import JSONResponse
import re

def validate_password_logic(v: str) -> str:
    if len(v) < 6:
        return JSONResponse(
            status_code=400,
            content={
                "success": False,
                "errors": [{"field": "password", "message": "Password must be at least 6 characters long."}]
            }
        )
    
    if not re.search(r"[A-Z]", v):
        return JSONResponse(
            status_code=400,
            content={
                "success": False,
                "errors": [{"field": "password", "message": "Password must contain at least one uppercase letter."}]
            }
        )
    
    if not re.search(r"[a-z]", v):
        return JSONResponse(
            status_code=400,
            content={
                "success": False,
                "errors": [{"field": "password", "message": "Password must contain at least one lowercase letter."}]
            }
        )
    
    if not re.search(r"\d", v):
        return JSONResponse(
            status_code=400,
            content={
                "success": False,
                "errors": [{"field": "password", "message": "Password must contain at least one digit."}]
            }
        )
    
    if " " in v:
         return JSONResponse(
            status_code=400,
            content={
                "success": False,
                "errors": [{"field": "password", "message": "Password must not contain spaces."}]
            }
        )

    return v
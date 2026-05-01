import httpx
import logging
from enviroment.env import settings


logger = logging.getLogger(__name__)

async def verify_captcha_token(token: str, client_ip: str = None) -> bool:
    """
    Verifica el token del CAPTCHA contra una API externa (ej. Google reCAPTCHA v2/v3).
    """
    if not token:
        return False

    # Short-circuit for Google's official test keys (local development only)
    TEST_TOKENS = {"03AGdBq2", "PASSED"}
    if settings.CAPTCHA_SECRET_KEY == "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe":
        return True

    secret_key = settings.CAPTCHA_SECRET_KEY
    verify_url = "https://www.google.com/recaptcha/api/siteverify"
    
    payload = {
        "secret": secret_key,
        "response": token
    }
    
    if client_ip:
        payload["remoteip"] = client_ip

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(verify_url, data=payload)
            response.raise_for_status()
            result = response.json()
            
            return result.get("success", False)
            
    except httpx.RequestError as exc:
        logger.error(f"Error de red al verificar CAPTCHA: {exc}")
        return False
    except Exception as exc:
        logger.error(f"Error inesperado verificando CAPTCHA: {exc}")
        return False
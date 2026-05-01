from pathlib import Path
from dotenv import load_dotenv
import os

BASE_DIR = Path(__file__).resolve().parents[1]  
load_dotenv(BASE_DIR / ".env")

class Settings:
    SECRET_KEY: str = os.getenv("SECRET_KEY")
    CAPTCHA_SECRET_KEY: str = os.getenv("CAPTCHA_SECRET_KEY")
    ALGORITHM: str = os.getenv("ALGORITHM")

    ACCESS_TOKEN_EXPIRE_MINUTES: int = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES"))
    REFRESH_TOKEN_EXPIRE_MINUTES: int = int(os.getenv("REFRESH_TOKEN_EXPIRE_MINUTES"))

    SECURE_COOKIES: bool = os.getenv("SECURE_COOKIES").lower() in ("1", "true", "yes")
    COOKIE_SAMESITE: str = os.getenv("COOKIE_SAMESITE")
    FRONTEND_ORIGIN: str = os.getenv("FRONTEND_ORIGIN")
    REMEMBER_REFRESH_MINUTES: int = int(os.getenv("REMEMBER_REFRESH_MINUTES"))
    API_PREFIX: str = os.getenv("API_PREFIX")
    OLLAMA_URL: str = os.getenv("OLLAMA_URL")
    OLLAMA_MODEL: str = os.getenv("OLLAMA_MODEL")
    GOOGLE_API_KEY: str = os.getenv("GOOGLE_API_KEY")

settings = Settings()
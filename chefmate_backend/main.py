from fastapi import FastAPI
from api.routes.user_route import user
from api.routes.product_route import product
from api.routes.recipe_route import recipe
from fastapi.middleware.cors import CORSMiddleware
from enviroment.env import settings
import os
from dotenv import load_dotenv

load_dotenv()
origins_raw = os.getenv("CORS_ORIGINS")
if getattr(settings, "FRONTEND_ORIGIN", None):
    allow_origins = [settings.FRONTEND_ORIGIN]
elif origins_raw:
    allow_origins = [origin.strip() for origin in origins_raw.split(",")]
else:
    allow_origins = ["*"]

app = FastAPI(
    title="ChefMate API",
    description="API para la aplicación ChefMate, que ofrece funcionalidades de gestión de recetas, planificación de comidas y más.",
    version="1.0.0",
    docs_url=settings.API_PREFIX + "/docs",
    redoc_url=settings.API_PREFIX + "/redoc"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=allow_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(user, prefix=settings.API_PREFIX, tags=["User"])
app.include_router(product, prefix=settings.API_PREFIX, tags=["Product"])
app.include_router(recipe, prefix=settings.API_PREFIX, tags=["Recipe"])
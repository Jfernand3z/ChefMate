from pydantic import BaseModel, EmailStr, AfterValidator
from typing_extensions import Annotated
from typing import List, Optional
from api.models.user_models.logs_model import Logs
from api.models.user_models.helpers.validate_password_logic import validate_password_logic

class User(BaseModel):
    username: str
    email: EmailStr
    password: Annotated[str, AfterValidator(validate_password_logic)]
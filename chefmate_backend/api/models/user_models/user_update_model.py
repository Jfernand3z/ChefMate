from pydantic import AfterValidator, BaseModel, EmailStr as emailStr
from typing_extensions import Annotated
from typing import Optional
from api.models.user_models.helpers.validate_password_logic import validate_password_logic

class UserUpdate(BaseModel):
    username: Optional[str] = None
    email: Optional[emailStr] = None
    password: Optional[Annotated[str, AfterValidator(validate_password_logic)]] = None
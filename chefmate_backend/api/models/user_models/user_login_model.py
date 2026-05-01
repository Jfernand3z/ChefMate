from pydantic import AfterValidator, BaseModel, EmailStr as emailStr
from typing_extensions import Annotated
from api.models.user_models.helpers.validate_password_logic import validate_password_logic

class UserLogin(BaseModel):
    email: emailStr
    password: Annotated[str, AfterValidator(validate_password_logic)]
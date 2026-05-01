from pydantic import BaseModel

class Logs(BaseModel):
    date: str
    ip: str
    action: str
    browser: str
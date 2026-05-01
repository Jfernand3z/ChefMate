def UserGetEntity(users) -> dict:
    return {
        "id": str(users["_id"]),
        "username": users["username"],
        "email": users["email"]
    }
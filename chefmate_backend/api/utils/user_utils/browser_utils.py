def get_browser_name(user_agent: str) -> str:
    if not user_agent:
        return "Desconocido"
    user_agent = user_agent.lower()
    if "chefmateapp" in user_agent:
        return "App Móvil"
    elif "dart" in user_agent or "okhttp" in user_agent or "dalvik" in user_agent or "cfnetwork" in user_agent:
        return "App Móvil (Backend)"
    elif "edg" in user_agent:
        return "Edge"
    elif "opr" in user_agent or "opera" in user_agent:
        return "Opera"
    elif "chrome" in user_agent:
        return "Chrome"
    elif "firefox" in user_agent:
        return "Firefox"
    elif "safari" in user_agent:
        return "Safari"
    else:
        return "Otro"

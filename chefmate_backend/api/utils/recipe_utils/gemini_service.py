import json
import re
from fastapi import HTTPException, status
import google.generativeai as genai
from enviroment.env import settings

genai.configure(api_key=settings.GOOGLE_API_KEY)

async def ask_gemini(prompt: str) -> list:
    try:
        model = genai.GenerativeModel('gemini-2.5-flash')
        response = await model.generate_content_async(
            prompt,
            generation_config=genai.types.GenerationConfig(
                temperature=0.85,
                top_p=0.92,
                response_mime_type="application/json"
            )
        )
        
        raw_text = response.text.strip()
        
        # 1. Intentar parseo directo
        try:
            parsed = json.loads(raw_text)
        except json.JSONDecodeError:
            # 2. Limpiar bloques markdown si existen
            clean_text = re.sub(r'```(?:json)?', '', raw_text).replace('```', '').strip()
            try:
                parsed = json.loads(clean_text)
            except json.JSONDecodeError:
                # 3. Extraer el primer array [...] completo con regex greedy
                match = re.search(r'(\[.*\])', clean_text, re.DOTALL)
                if not match:
                    raise ValueError("No JSON array found in response")
                parsed = json.loads(match.group(1))
            
        if isinstance(parsed, dict):
            recipes_list = []
            for key in ["recipes", "recetas", "data"]:
                if key in parsed and isinstance(parsed[key], list):
                    recipes_list = parsed[key]
                    break
            if not recipes_list:
                recipes_list = [parsed]
        elif isinstance(parsed, list):
            recipes_list = parsed
        else:
            recipes_list = []
            
        # Normalizar para evitar fallos en el frontend
        normalized = []
        for r in recipes_list:
            if not isinstance(r, dict):
                continue
            # Desempaquetar si viene dentro de una llave 'receta'
            if 'recipe' in r and isinstance(r['recipe'], dict):
                r = r['recipe']
            elif 'receta' in r and isinstance(r['receta'], dict):
                r = r['receta']
            
            # Traducir llaves comunes
            if 'ingredientes' in r and 'ingredients' not in r:
                r['ingredients'] = r.pop('ingredientes')
            if 'pasos' in r and 'steps' not in r:
                r['steps'] = r.pop('pasos')
                
            # Garantizar propiedades
            if 'ingredients' not in r or not isinstance(r['ingredients'], list):
                r['ingredients'] = []
            if 'steps' not in r or not isinstance(r['steps'], list):
                r['steps'] = []
                
            normalized.append(r)
            
        return normalized
    except Exception as e:
        print(f"Error calling Gemini: {e}")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="La IA no devolvió un JSON válido o hubo un error al comunicarse con Gemini. Intenta de nuevo.",
        )

"""
Módulo de conversión de unidades para comparar cantidades de ingredientes
con cantidades en el inventario.
"""

# Factores de conversión → todo se convierte a la unidad BASE de cada tipo
# VOLUMEN → base: Litro (L)
VOLUME_TO_LITERS = {
    "l":   1.0,
    "lt":  1.0,
    "lts": 1.0,
    "litro":  1.0,
    "litros": 1.0,
    "litre":  1.0,
    "litres": 1.0,
    "liter":  1.0,
    "liters": 1.0,
    "ml":   0.001,
    "millilitro":  0.001,
    "millilitros": 0.001,
    "milliliter":  0.001,
    "milliliters": 0.001,
    "cl":   0.01,
    "centilitro":  0.01,
    "centilitros": 0.01,
    "dl":   0.1,
    "decilitro":  0.1,
    "decilitros": 0.1,
    "taza":   0.240,
    "tazas":  0.240,
    "cup":    0.240,
    "cups":   0.240,
    "oz":     0.02957,
    "fl oz":  0.02957,
    "tbsp":   0.01478,
    "cucharada":  0.015,
    "cucharadas": 0.015,
    "tsp":    0.004929,
    "cucharadita":  0.005,
    "cucharaditas": 0.005,
    "gallon": 3.78541,
    "gallons": 3.78541,
    "galon":  3.78541,
    "galones": 3.78541,
}

# MASA/PESO → base: Kilogramo (Kg)
MASS_TO_KG = {
    "kg":  1.0,
    "kgs": 1.0,
    "kilo":   1.0,
    "kilos":  1.0,
    "kilogramo":  1.0,
    "kilogramos": 1.0,
    "g":   0.001,
    "gr":  0.001,
    "gramo":  0.001,
    "gramos": 0.001,
    "gram":   0.001,
    "grams":  0.001,
    "mg":  0.000001,
    "miligramo":  0.000001,
    "miligramos": 0.000001,
    "milligram":  0.000001,
    "milligrams": 0.000001,
    "lb":  0.453592,
    "lbs": 0.453592,
    "libra":  0.453592,
    "libras": 0.453592,
    "pound":  0.453592,
    "pounds": 0.453592,
    "oz":  0.02835,       # onza de peso (no fluida)
    "onza":   0.02835,
    "onzas":  0.02835,
    "ounce":  0.02835,
    "ounces": 0.02835,
    "arroba": 11.5,
}

# UNIDADES DISCRETAS → base: Unidad
UNIT_TO_BASE = {
    "unidad":   1.0,
    "unidades": 1.0,
    "unit":     1.0,
    "units":    1.0,
    "pieza":    1.0,
    "piezas":   1.0,
    "piece":    1.0,
    "pieces":   1.0,
    "ud":       1.0,
    "u":        1.0,
    "docena":   12.0,
    "docenas":  12.0,
    "dozen":    12.0,
    "dozens":   12.0,
    "par":      2.0,
    "pares":    2.0,
    "paquete":  1.0,
    "paquetes": 1.0,
    "paquete pequeño": 1.0,
    "lata":     1.0,
    "latas":    1.0,
    "sobre":    1.0,
    "sobres":   1.0,
    "bolsa":    1.0,
    "bolsas":   1.0,
}


def _get_unit_type_and_base(unit: str):
    """
    Retorna (tipo, factor_a_base) donde tipo es 'volume', 'mass' o 'unit'.
    Si la unidad es desconocida retorna ('unknown', 1.0).
    """
    key = unit.lower().strip()
    if key in VOLUME_TO_LITERS:
        return "volume", VOLUME_TO_LITERS[key]
    if key in MASS_TO_KG:
        return "mass", MASS_TO_KG[key]
    if key in UNIT_TO_BASE:
        return "unit", UNIT_TO_BASE[key]
    return "unknown", 1.0


def convert_to_base(quantity: float, unit: str) -> tuple[float, str]:
    """
    Convierte una cantidad a su unidad base.
    Retorna (cantidad_en_base, tipo).
    """
    tipo, factor = _get_unit_type_and_base(unit)
    return quantity * factor, tipo


def can_fulfill(
    product_qty: float,
    product_unit: str,
    needed_qty: float,
    needed_unit: str,
) -> bool:
    """
    Retorna True si el producto en inventario alcanza para la cantidad necesaria.
    Convierte ambas cantidades a la unidad base antes de comparar.
    """
    product_base, product_type = convert_to_base(product_qty, product_unit)
    needed_base, needed_type = convert_to_base(needed_qty, needed_unit)

    # Si los tipos no son comparables (ej: kg vs litros), comparar directo
    if product_type != needed_type or product_type == "unknown":
        return product_qty >= needed_qty

    return product_base >= needed_base


def max_servings_from_stock(
    product_qty: float,
    product_unit: str,
    needed_qty_per_serving: float,
    needed_unit: str,
) -> int:
    """
    Calcula cuántas porciones se pueden hacer con el stock disponible.
    """
    if needed_qty_per_serving <= 0:
        return 999  # ingrediente sin cantidad definida, no limita

    product_base, product_type = convert_to_base(product_qty, product_unit)
    needed_base, needed_type = convert_to_base(needed_qty_per_serving, needed_unit)

    if product_type != needed_type or product_type == "unknown":
        # Fallback: comparar como misma unidad
        if needed_qty_per_serving <= 0:
            return 999
        return int(product_qty / needed_qty_per_serving)

    if needed_base <= 0:
        return 999

    return int(product_base / needed_base)

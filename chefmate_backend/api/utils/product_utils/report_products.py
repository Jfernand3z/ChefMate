from config.db import database
from datetime import datetime, date
from io import BytesIO
from fastapi.responses import StreamingResponse
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors


async def report_product():

    products = []
    cursor = database.product.find()

    async for product in cursor:
        products.append(product)

    buffer = BytesIO()

    basil_green = colors.HexColor("#10B981")
    smoke_white = colors.HexColor("#F8FAFC")
    tomato_red = colors.HexColor("#EF4444")
    saffron_yellow = colors.HexColor("#F59E0B")

    doc = SimpleDocTemplate(
        buffer,
        pagesize=letter,
        leftMargin=40,
        rightMargin=40,
        topMargin=40,
        bottomMargin=40
    )

    styles = getSampleStyleSheet()

    title_style = ParagraphStyle(
        "Title",
        parent=styles["Title"],
        fontName="Helvetica-Bold",
        fontSize=26,
        textColor=basil_green
    )

    subtitle_style = ParagraphStyle(
        "Subtitle",
        parent=styles["Normal"],
        fontSize=10,
        textColor=colors.grey
    )

    elements = []

    # 📌 Título
    elements.append(Paragraph("ChefMate - Reporte de Inventario", title_style))

    elements.append(
        Paragraph(
            f"Generado el {datetime.now().strftime('%d/%m/%Y %H:%M')}",
            subtitle_style
        )
    )

    elements.append(Spacer(1, 25))

    data = [
        ["Producto", "Cantidad", "Unidad", "Expiración", "Categoría", "Estado"]
    ]

    today = date.today()

    row_colors = []

    for p in products:

        exp_date_raw = p.get("expiration_date")
        exp_date = None
        status = "OK"
        color = smoke_white

        if exp_date_raw:
            if isinstance(exp_date_raw, str):
                # Split 'T' if format is ISO
                date_str = exp_date_raw.split("T")[0]
                exp_date = datetime.strptime(date_str, "%Y-%m-%d").date()
            elif hasattr(exp_date_raw, "date"):
                exp_date = exp_date_raw.date()
            else:
                exp_date = exp_date_raw
                
            days_left = (exp_date - today).days

            if days_left < 0:
                status = "Caducado"
                color = tomato_red

            elif days_left <= 2:
                status = "Por caducar"
                color = saffron_yellow

        data.append([
            p.get("name", ""),
            p.get("quantity", ""),
            p.get("unit", ""),
            str(exp_date) if exp_date else "N/A",
            p.get("category", "-"),
            status
        ])

        row_colors.append(color)

    table = Table(data, colWidths=[120, 70, 70, 90, 100, 80])

    style = [
        ("BACKGROUND", (0,0), (-1,0), basil_green),
        ("TEXTCOLOR",(0,0),(-1,0),colors.white),

        ("FONTNAME",(0,0),(-1,0),"Helvetica-Bold"),
        ("FONTSIZE",(0,0),(-1,-1),10),

        ("ALIGN",(1,1),(-1,-1),"CENTER"),

        ("GRID",(0,0),(-1,-1),0.5,colors.grey),

        ("ROWBACKGROUNDS",(0,1),(-1,-1),[smoke_white]),
    ]

    for i, color in enumerate(row_colors):
        style.append(("BACKGROUND", (0,i+1), (-1,i+1), color))

    table.setStyle(TableStyle(style))

    elements.append(table)

    elements.append(Spacer(1, 30))

    elements.append(
        Paragraph(
            "Leyenda: Productos en rojo están caducados. Amarillo indica que caducan pronto.",
            subtitle_style
        )
    )

    doc.build(elements)

    buffer.seek(0)

    return StreamingResponse(
        buffer,
        media_type="application/pdf",
        headers={
            "Content-Disposition": "attachment; filename=reporte_productos.pdf"
        }
    )
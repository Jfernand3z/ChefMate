package com.example.chefmateapp.ui.screens.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefmateapp.data.model.product.Product
import com.example.chefmateapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = getStatusColor(product.expirationDate)
    val initials = product.name.take(2).uppercase()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                style = AppTypography.titleMedium,
                fontSize = 20.sp
            )

            Surface(
                modifier = Modifier.padding(vertical = 4.dp),
                color = if (statusColor == Surface) PrimaryLight else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = product.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = AppTypography.labelMedium,
                    color = if (statusColor == Surface) Primary else TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderLight.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quantity", style = AppTypography.labelMedium, color = TextMuted)
                    Text("${product.quantity} ${product.unit}", style = AppTypography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expiration", style = AppTypography.labelMedium, color = TextMuted)
                    Text(
                        text = product.expirationDate?.split("T")?.get(0) ?: "N/A",
                        style = AppTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (statusColor == DangerLight) Danger else TextPrimary
                    )
                }
            }
        }
    }
}

private fun getStatusColor(expirationDate: String?): Color {
    if (expirationDate == null) return Surface
    
    return try {
        val datePart = expirationDate.split("T")[0]
        val expiry = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now()
        
        when {
            expiry.isBefore(today) -> DangerLight
            ChronoUnit.DAYS.between(today, expiry) <= 7 -> WarningLight
            else -> Surface
        }
    } catch (e: Exception) {
        Surface
    }
}
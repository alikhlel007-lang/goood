package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CafeIcons {

    val AVAILABLE_ICONS = listOf(
        "coffee" to "قهوة",
        "tea" to "شاي مهيل",
        "espresso" to "إسبريسو / كابتشينو",
        "cold_drink" to "مشروب بارد / سموذي",
        "juice" to "عصير طبيعي",
        "cake" to "كيك وحلويات",
        "breakfast" to "إفطار وكرواسون",
        "fast_food" to "وجبات سريعة",
        "burger" to "برجر وساندوتش",
        "pizza" to "بيتزا ومناقيش",
        "hookah" to "أراكيل ومعسل",
        "restaurant" to "مطعم عام"
    )

    fun getIcon(name: String): ImageVector {
        return when (name.lowercase()) {
            "coffee" -> Icons.Default.LocalCafe
            "tea" -> Icons.Default.EmojiFoodBeverage
            "espresso" -> Icons.Default.Coffee
            "cold_drink" -> Icons.Default.LocalDrink
            "juice" -> Icons.Default.LocalBar
            "cake" -> Icons.Default.Cake
            "dessert" -> Icons.Default.BakeryDining
            "breakfast" -> Icons.Default.BreakfastDining
            "fast_food" -> Icons.Default.Fastfood
            "burger" -> Icons.Default.LunchDining
            "pizza" -> Icons.Default.LocalPizza
            "hookah" -> Icons.Default.SmokingRooms
            "vip" -> Icons.Default.Star
            "table" -> Icons.Default.TableRestaurant
            else -> Icons.Default.LocalCafe
        }
    }
}

@Composable
fun CafeIconBadge(
    iconName: String,
    customImageUri: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (!customImageUri.isNullOrBlank()) {
            AsyncImage(
                model = customImageUri,
                contentDescription = iconName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = CafeIcons.getIcon(iconName),
                contentDescription = iconName,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

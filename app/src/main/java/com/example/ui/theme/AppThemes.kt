package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val titleAr: String, val titleEn: String) {
    SYSTEM("تلقائي النظام", "System Auto"),
    LIGHT("الوضع الفاتح ☀️", "Light Mode ☀️"),
    DARK("الوضع الداكن 🌙", "Dark Mode 🌙")
}

enum class CornerStyle(val radiusDp: Int, val titleAr: String, val titleEn: String) {
    MODERN_ROUNDED(20, "مستدير عصري (20dp)", "Modern Rounded (20dp)"),
    SOFT_CURVED(12, "منحنيات ناعمة (12dp)", "Soft Curved (12dp)"),
    SHARP_CLEAN(6, "أنيق كلاسيكي (6dp)", "Sharp Clean (6dp)")
}

data class ThemePreset(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val surfaceColor: Color,
    val containerColor: Color,
    val lightColors: ColorScheme,
    val darkColors: ColorScheme
)

object AppThemes {

    // 1. القهوة الكلاسيكية والكراميل الدافئ
    val ESPRESSO_WARM = ThemePreset(
        id = "espresso_warm",
        nameAr = "قهوة دافئة كلاسيكية",
        nameEn = "Classic Warm Espresso",
        descriptionAr = "أجواء مقاهي دافئة تجمع بين درجات البن المحمص والكراميل الذهبي",
        primaryColor = Color(0xFF6F3C16),
        secondaryColor = Color(0xFFC07000),
        surfaceColor = Color(0xFFFFFFFF),
        containerColor = Color(0xFFFBE8D8),
        lightColors = lightColorScheme(
            primary = Color(0xFF6F3C16),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFBE8D8),
            onPrimaryContainer = Color(0xFF2E1504),
            secondary = Color(0xFFC07000),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDFB0),
            onSecondaryContainer = Color(0xFF2C1600),
            background = Color(0xFFFAF7F2),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF1EAE2),
            onBackground = Color(0xFF1F1B18),
            onSurface = Color(0xFF1F1B18),
            onSurfaceVariant = Color(0xFF51453D),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFE5A974),
            onPrimary = Color(0xFF442006),
            primaryContainer = Color(0xFF5A2F0F),
            onPrimaryContainer = Color(0xFFFFDCC2),
            secondary = Color(0xFFFFB74D),
            onSecondary = Color(0xFF452B00),
            secondaryContainer = Color(0xFF633F00),
            onSecondaryContainer = Color(0xFFFFDFB0),
            background = Color(0xFF1A130E),
            surface = Color(0xFF261D17),
            surfaceVariant = Color(0xFF3B2E25),
            onBackground = Color(0xFFFAF7F2),
            onSurface = Color(0xFFFAF7F2),
            onSurfaceVariant = Color(0xFFD7C2B4),
            error = Color(0xFFFFB4AB),
            errorContainer = Color(0xFF93000A)
        )
    )

    // 2. الفخامة والذهب الأسود (VIP Luxury)
    val MIDNIGHT_GOLD = ThemePreset(
        id = "midnight_gold",
        nameAr = "فخامة ملكية وذهب أسود",
        nameEn = "Midnight Gold & Obsidian",
        descriptionAr = "تصميم فندقي راقٍ بدرجات الذهب الملكي اللامع مع الأوبسيديان الأسود",
        primaryColor = Color(0xFFD4AF37),
        secondaryColor = Color(0xFFB8860B),
        surfaceColor = Color(0xFFFFFFFF),
        containerColor = Color(0xFFFFF4D4),
        lightColors = lightColorScheme(
            primary = Color(0xFF946F08),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFE69A),
            onPrimaryContainer = Color(0xFF2E2100),
            secondary = Color(0xFF6B5D3F),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF4E1BB),
            onSecondaryContainer = Color(0xFF241A03),
            background = Color(0xFFFDFBF7),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF1ECE0),
            onBackground = Color(0xFF1D1B16),
            onSurface = Color(0xFF1D1B16),
            onSurfaceVariant = Color(0xFF4D4639),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFE5A93C),
            onPrimary = Color(0xFF141414),
            primaryContainer = Color(0xFFE5A93C),
            onPrimaryContainer = Color(0xFF141414),
            secondary = Color(0xFFECC055),
            onSecondary = Color(0xFF141414),
            secondaryContainer = Color(0xFF262626),
            onSecondaryContainer = Color(0xFFE5A93C),
            background = Color(0xFF141414),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF262626),
            onBackground = Color(0xFFEEEEEE),
            onSurface = Color(0xFFEEEEEE),
            onSurfaceVariant = Color(0xFFA5A5A5),
            error = Color(0xFFFF5252),
            errorContainer = Color(0xFF93000A)
        )
    )

    // 3. الزمرد والطبيعة الحيوية (Botanica & Matcha)
    val EMERALD_BOTANICA = ThemePreset(
        id = "emerald_botanica",
        nameAr = "طبيعة وماتشا الزمرد",
        nameEn = "Emerald Botanica & Matcha",
        descriptionAr = "طابع منعش وحيوي مستوحى من أوراق الماتشا الطبيعية والخضار العضوي",
        primaryColor = Color(0xFF1B5E39),
        secondaryColor = Color(0xFF2E7D32),
        surfaceColor = Color(0xFFFFFFFF),
        containerColor = Color(0xFFD5F0DE),
        lightColors = lightColorScheme(
            primary = Color(0xFF1B5E39),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFA5F2C1),
            onPrimaryContainer = Color(0xFF00210E),
            secondary = Color(0xFF4D6653),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCFEBD4),
            onSecondaryContainer = Color(0xFF0B2012),
            background = Color(0xFFF5FBF6),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFDDE7DF),
            onBackground = Color(0xFF171D18),
            onSurface = Color(0xFF171D18),
            onSurfaceVariant = Color(0xFF414942),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFF8AD5A6),
            onPrimary = Color(0xFF00391E),
            primaryContainer = Color(0xFF00522D),
            onPrimaryContainer = Color(0xFFA5F2C1),
            secondary = Color(0xFFB4CEB8),
            onSecondary = Color(0xFF203626),
            secondaryContainer = Color(0xFF364D3C),
            onSecondaryContainer = Color(0xFFCFEBD4),
            background = Color(0xFF0E1611),
            surface = Color(0xFF16221B),
            surfaceVariant = Color(0xFF23352A),
            onBackground = Color(0xFFE0E5E0),
            onSurface = Color(0xFFE0E5E0),
            onSurfaceVariant = Color(0xFFBFC9BF),
            error = Color(0xFFFFB4AB),
            errorContainer = Color(0xFF93000A)
        )
    )

    // 4. المخمل الياقوتي (Ruby Velvet & Bistro)
    val RUBY_VELVET = ThemePreset(
        id = "ruby_velvet",
        nameAr = "شفق ياقوتي مخملي",
        nameEn = "Ruby Velvet & Bistro",
        descriptionAr = "أناقة هادئة ورومانسية بدرجات التوت الياقوتي والورد المخملي للحلويات والمشروبات",
        primaryColor = Color(0xFF8B1D38),
        secondaryColor = Color(0xFFB33951),
        surfaceColor = Color(0xFFFFFFFF),
        containerColor = Color(0xFFFFD9DF),
        lightColors = lightColorScheme(
            primary = Color(0xFF8B1D38),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9DF),
            onPrimaryContainer = Color(0xFF3B0011),
            secondary = Color(0xFF75565B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9DE),
            onSecondaryContainer = Color(0xFF2C1519),
            background = Color(0xFFFCF6F7),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF2DEE1),
            onBackground = Color(0xFF201A1B),
            onSurface = Color(0xFF201A1B),
            onSurfaceVariant = Color(0xFF524345),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFFFB1BE),
            onPrimary = Color(0xFF54001E),
            primaryContainer = Color(0xFF720527),
            onPrimaryContainer = Color(0xFFFFD9DF),
            secondary = Color(0xFFE4BDC2),
            onSecondary = Color(0xFF43292E),
            secondaryContainer = Color(0xFF5B3F44),
            onSecondaryContainer = Color(0xFFFFD9DE),
            background = Color(0xFF191012),
            surface = Color(0xFF241619),
            surfaceVariant = Color(0xFF382328),
            onBackground = Color(0xFFEBE0E1),
            onSurface = Color(0xFFEBE0E1),
            onSurfaceVariant = Color(0xFFD6C2C5),
            error = Color(0xFFFFB4AB),
            errorContainer = Color(0xFF93000A)
        )
    )

    // 5. سافير كوزمو الحديث (Modern Sapphire & Cyan)
    val OCEAN_SAPPHIRE = ThemePreset(
        id = "ocean_sapphire",
        nameAr = "سافير كوزمو الحديث",
        nameEn = "Modern Ocean Sapphire",
        descriptionAr = "مظهر تقني عصري ونابض يجمع بين الأزرق الملكي والسيان الكهربائي",
        primaryColor = Color(0xFF0F4C81),
        secondaryColor = Color(0xFF0288D1),
        surfaceColor = Color(0xFFFFFFFF),
        containerColor = Color(0xFFD4E7FA),
        lightColors = lightColorScheme(
            primary = Color(0xFF0F4C81),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD4E7FA),
            onPrimaryContainer = Color(0xFF001D36),
            secondary = Color(0xFF00668B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFC3E8FF),
            onSecondaryContainer = Color(0xFF001E2C),
            background = Color(0xFFF7FAFD),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFDFE6ED),
            onBackground = Color(0xFF191C1E),
            onSurface = Color(0xFF191C1E),
            onSurfaceVariant = Color(0xFF41474D),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFA5C8FE),
            onPrimary = Color(0xFF00315B),
            primaryContainer = Color(0xFF004880),
            onPrimaryContainer = Color(0xFFD4E7FA),
            secondary = Color(0xFF7BD0FF),
            onSecondary = Color(0xFF00354A),
            secondaryContainer = Color(0xFF004C6A),
            onSecondaryContainer = Color(0xFFC3E8FF),
            background = Color(0xFF0F151B),
            surface = Color(0xFF162029),
            surfaceVariant = Color(0xFF23313E),
            onBackground = Color(0xFFE1E6EB),
            onSurface = Color(0xFFE1E6EB),
            onSurfaceVariant = Color(0xFFC2CBD2),
            error = Color(0xFFFFB4AB),
            errorContainer = Color(0xFF93000A)
        )
    )

    val allPresets = listOf(
        ESPRESSO_WARM,
        MIDNIGHT_GOLD,
        EMERALD_BOTANICA,
        RUBY_VELVET,
        OCEAN_SAPPHIRE
    )

    fun fromId(id: String?): ThemePreset {
        return allPresets.firstOrNull { it.id == id } ?: ESPRESSO_WARM
    }
}

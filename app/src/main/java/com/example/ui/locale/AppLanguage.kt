package com.example.ui.locale

enum class AppLanguage(val code: String, val titleInLanguage: String, val isRtl: Boolean) {
    ARABIC("ar", "العربية", true),
    ENGLISH("en", "English", false),
    KURDISH("ku", "کوردی", true);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ARABIC
        }
    }
}

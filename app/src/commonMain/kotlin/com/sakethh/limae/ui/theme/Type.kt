package com.sakethh.limae.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import limae.app.generated.resources.Res
import limae.app.generated.resources.googleSansFlex
import limae.app.generated.resources.momo_signature
import org.jetbrains.compose.resources.Font

private val googleSansFlexFontFamily
    @Composable get() = FontFamily(
        Font(
            Res.font.googleSansFlex,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(460),
                FontVariation.width(102f)
            )
        ),
        Font(
            Res.font.googleSansFlex,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(560),
                FontVariation.width(102f)
            )
        ),
        Font(
            Res.font.googleSansFlex,
            weight = FontWeight.SemiBold,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(660),
                FontVariation.width(102f)
            )
        ),
    )

private val momoSignatureFontFamily
    @Composable get() = FontFamily(
        Font(
            resource = Res.font.momo_signature,
            weight = FontWeight.Normal
        )
    )

val LimaeTypography
    @Composable get() = Typography(
        titleLarge = TextStyle(fontFamily = googleSansFlexFontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = TextStyle(fontFamily = googleSansFlexFontFamily, fontWeight = FontWeight.Medium),
        titleSmall = TextStyle(fontFamily = googleSansFlexFontFamily, fontWeight = FontWeight.Normal),
        labelSmall = TextStyle(fontFamily = momoSignatureFontFamily, fontWeight = FontWeight.Normal)
    )
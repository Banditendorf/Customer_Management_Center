package com.cmc.customer.ui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmc.customer.ui.theme.RedPrimary

// RedTopBar Ã¶lÃ§Ã¼leri - ayarlanabilir deÄŸiÅŸkenler
private val RedTopBarHeight = 56.dp               // ğŸ› ï¸ Top bar iÃ§eriÄŸinin yÃ¼ksekliÄŸi
private val RedTopBarHorizontalPadding = 6.dp     // ğŸ› ï¸ SaÄŸ-sol boÅŸluk
private val RedTopBarTitleFontSize = 20.sp        // ğŸ› ï¸ BaÅŸlÄ±k yazÄ± boyutu
private val RedTopBarTitleFontWeight = FontWeight.Bold // ğŸ› ï¸ BaÅŸlÄ±k yazÄ± kalÄ±nlÄ±ÄŸÄ±
private val RedTopBarTitlePaddingStart = 8.dp     // ğŸ› ï¸ BaÅŸlÄ±k baÅŸÄ±ndan boÅŸluk
private val RedTopBarIconTint = Color.White       // ğŸ› ï¸ Icon rengi
private val RedTopBarBackgroundColor = RedPrimary // ğŸ› ï¸ Arka plan rengi
private val RedTopBarIconSize = 24.dp             // ğŸ› ï¸ Icon boyutu
private val RedTopBarDropDownWidth = 180.dp       // ğŸ› ï¸ Dropdown MenÃ¼ geniÅŸliÄŸi

/**
 * RedTopBar with optional left back button, title, dropdown menu, leading and trailing images.
 *
 * @param title BaÅŸlÄ±k metni
 * @param showMenu MenÃ¼ ikonunu gÃ¶sterir
 * @param showBackButton Geri ikonunu gÃ¶sterir
 * @param onBackClick Geri tuÅŸuna tÄ±klama callback
 * @param leadingImage Opsiyonel: Sol tarafa eklenecek Painter
 * @param leadingImageDescription EriÅŸilebilirlik aÃ§Ä±klamasÄ± (sol)
 * @param leadingImageSize Sol gÃ¶rsel boyutu
 * @param trailingImage Opsiyonel: SaÄŸ tarafa eklenecek Painter
 * @param trailingImageDescription EriÅŸilebilirlik aÃ§Ä±klamasÄ± (saÄŸ)
 * @param trailingImageSize SaÄŸ gÃ¶rsel boyutu
 * @param menuContent MenÃ¼ iÃ§eriÄŸi (DropdownMenuItem lambdalarÄ±)
 */
@Composable
fun RedTopBar(
    title: String,
    showMenu: Boolean = false,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    leadingImage: Painter? = null,
    leadingImageDescription: String? = null,
    leadingImageSize: Dp = RedTopBarIconSize,
    trailingImage: Painter? = null,
    trailingImageDescription: String? = null,
    trailingImageSize: Dp = RedTopBarIconSize,
    menuContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(RedTopBarBackgroundColor)
            .statusBarsPadding()
            .height(RedTopBarHeight)
            .padding(horizontal = RedTopBarHorizontalPadding)
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sol alan: geri butonu, opsiyonel resim ve baÅŸlÄ±k
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBackButton && onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = RedTopBarIconTint,
                            modifier = Modifier.size(RedTopBarIconSize)
                        )
                    }
                }
                leadingImage?.let { painter ->
                    Image(
                        painter = painter,
                        contentDescription = leadingImageDescription,
                        modifier = Modifier
                            .size(leadingImageSize)
                            .padding(start = RedTopBarTitlePaddingStart)
                    )
                }
                Text(
                    text = title,
                    color = RedTopBarIconTint,
                    fontWeight = RedTopBarTitleFontWeight,
                    fontSize = RedTopBarTitleFontSize,
                    modifier = Modifier.padding(start = RedTopBarTitlePaddingStart)
                )
            }

            // SaÄŸ alan: opsiyonel resim, dropdown menÃ¼
            Row(verticalAlignment = Alignment.CenterVertically) {
                trailingImage?.let { painter ->
                    Image(
                        painter = painter,
                        contentDescription = trailingImageDescription,
                        modifier = Modifier
                            .size(trailingImageSize)
                            .padding(end = RedTopBarTitlePaddingStart)
                    )
                }
                if (showMenu && menuContent != null) {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "MenÃ¼",
                                tint = RedTopBarIconTint,
                                modifier = Modifier.size(RedTopBarIconSize)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.width(RedTopBarDropDownWidth)
                        ) {
                            menuContent()
                        }
                    }
                }
            }
        }
    }
}

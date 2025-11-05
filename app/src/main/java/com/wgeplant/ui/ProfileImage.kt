package com.wgeplant.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.wgeplant.R

@Composable
fun ProfileImage(uri: Uri?, size: Dp = 100.dp) {
    val painter: Painter = if (uri == null || uri.toString().isEmpty()) {
        painterResource(id = R.drawable.default_profile_picture)
    } else {
        rememberAsyncImagePainter(model = uri)
    }

    Image(
        painter = painter,
        contentDescription = "Profilbild",
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
    )
}

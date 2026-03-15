package com.wakee.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakee.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageCropView(
    imageUri: Uri,
    onCropped: (Uri) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val bitmap = remember(imageUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Filled.Close, contentDescription = "キャンセル", tint = PrimaryText)
            }
            Text("画像を調整", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            IconButton(onClick = {
                bitmap?.let { bmp ->
                    val croppedFile = cropAndSave(context, bmp, scale, offset)
                    croppedFile?.let { onCropped(Uri.fromFile(it)) }
                }
            }) {
                Icon(Icons.Filled.Check, contentDescription = "完了", tint = Accent)
            }
        }

        // Canvas with image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offset = Offset(
                            x = offset.x + pan.x,
                            y = offset.y + pan.y
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let { bmp ->
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val circleRadius = minOf(canvasWidth, canvasHeight) * 0.4f

                    // Draw image
                    drawIntoCanvas { canvas ->
                        val imgWidth = bmp.width.toFloat()
                        val imgHeight = bmp.height.toFloat()
                        val baseScale = minOf(canvasWidth / imgWidth, canvasHeight / imgHeight)
                        val finalScale = baseScale * scale
                        val dx = (canvasWidth - imgWidth * finalScale) / 2f + offset.x
                        val dy = (canvasHeight - imgHeight * finalScale) / 2f + offset.y

                        canvas.save()
                        canvas.translate(dx, dy)
                        canvas.scale(finalScale, finalScale)
                        canvas.drawImageRect(
                            bmp,
                            paint = Paint()
                        )
                        canvas.restore()
                    }

                    // Draw overlay (semi-transparent outside circle)
                    val path = Path().apply {
                        addRect(androidx.compose.ui.geometry.Rect(0f, 0f, canvasWidth, canvasHeight))
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                canvasWidth / 2 - circleRadius,
                                canvasHeight / 2 - circleRadius,
                                canvasWidth / 2 + circleRadius,
                                canvasHeight / 2 + circleRadius
                            )
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.6f),
                        style = androidx.compose.ui.graphics.drawscope.Fill
                    )

                    // Circle border
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = circleRadius,
                        center = Offset(canvasWidth / 2, canvasHeight / 2),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }
        }

        Text(
            text = "ピンチで拡大・縮小、ドラッグで位置調整",
            fontSize = 13.sp,
            color = SecondaryText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

private fun cropAndSave(
    context: android.content.Context,
    bitmap: ImageBitmap,
    scale: Float,
    offset: Offset
): File? {
    return try {
        val size = minOf(bitmap.width, bitmap.height)
        val androidBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(androidBitmap)
        val srcBitmap = bitmap.asAndroidBitmap()

        val dx = (size - srcBitmap.width * scale) / 2f + offset.x
        val dy = (size - srcBitmap.height * scale) / 2f + offset.y

        val matrix = android.graphics.Matrix().apply {
            postScale(scale, scale)
            postTranslate(dx, dy)
        }
        canvas.drawBitmap(srcBitmap, matrix, null)

        val file = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            androidBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

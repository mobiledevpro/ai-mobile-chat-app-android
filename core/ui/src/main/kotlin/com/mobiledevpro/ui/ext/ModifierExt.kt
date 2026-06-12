/*
 * Copyright 2025 | Dmitri Chernysh | https://github.com/dmitriy-chernysh
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.mobiledevpro.ui.ext

import android.util.Log
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun Modifier.conditional(
    condition: Boolean,
    ifTrue: @Composable Modifier.() -> Modifier,
    ifFalse: @Composable Modifier.() -> Modifier = { this }
): Modifier =
    if (condition) {
        then(ifTrue(Modifier))
    } else
        then(ifFalse(Modifier))


@Composable
fun Modifier.dashedBorder(
    color: Color,
    shape: Shape,
    strokeWidth: Dp = 1.dp,
    dashWidth: Dp = 4.dp,
    gapWidth: Dp = 4.dp,
    cap: StrokeCap = StrokeCap.Round
) = this.drawWithContent {
    val outline = shape.createOutline(size, layoutDirection, this)

    val path = Path()
    path.addOutline(outline)

    val stroke = Stroke(
        cap = cap,
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashWidth.toPx(), gapWidth.toPx()),
            phase = 0f
        )
    )

    this.drawContent()

    drawPath(
        path = path,
        style = stroke,
        color = color
    )
}


@Composable
fun Modifier.touchHandler(
    viewSize: () -> IntSize,
    zoomFactor: () -> Float,
    zoomOffset: () -> Offset,
    onZoomChanged: (Float) -> Unit,
    onZoomOffsetChanged: (Offset) -> Unit,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit

): Modifier {
    val coroutineScope = rememberCoroutineScope()
    val touchSlop = LocalViewConfiguration.current.touchSlop
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var tapCount by remember { mutableIntStateOf(0) }

    return this.pointerInput(Unit) {
        awaitEachGesture {
            // Wait for the first pointer event
            val down = awaitFirstDown(requireUnconsumed = true)
            var touchSlopExceeded = false

            // Log.d("gesture", "down: ${down.type}")

            val viewWidth = viewSize().toSize().width
            val viewHeight = viewSize().toSize().height

            // Wait for all pointers to be up
            do {
                val event = awaitPointerEvent(PointerEventPass.Main)
                /* Log.d(
                     "gesture",
                     "PdfViewerScreen: event ${event.type} | changes ${event.changes.size}"
                 )*/

                if (event.changes.isNotEmpty()) {
                    var zoom = zoomFactor() * event.calculateZoom()
                    zoom = 1f.coerceAtLeast(zoom)

                    if (zoom != zoomFactor()) {
                        onZoomChanged(zoom)
                        // Log.d("gesture", "touchHandler: zoom = $zoom")
                    }

                    val pan = event.calculatePan()

                    //  Log.d("gesture", "touchHandler: pan = $pan | zoom=$zoom | viewSize=${viewSize()}")

                    val maxOffsetX: Float = ((viewWidth * zoom) - viewWidth) / 2
                    val minOffsetX: Float = if (maxOffsetX != 0f) -maxOffsetX else 0f
                    val maxOffsetY: Float = ((viewHeight * zoom) - viewHeight) / 2
                    val minOffsetY: Float = if (maxOffsetY != 0f) -maxOffsetY else 0f

                    //Calculate offset based on the size x scale
                    var offsetX = zoomOffset().x + pan.x
                    var offsetY = zoomOffset().y + pan.y

                    //   Log.d("gesture", "touchHandler: OFFSET: x=$offsetX y=$offsetY | maxX=$maxOffsetX maxY=$maxOffsetY | minX=$minOffsetX minY=$minOffsetY")

                    // Check horizontal offset
                    if (offsetX >= maxOffsetX)
                        offsetX = maxOffsetX
                    else if (offsetX <= minOffsetX)
                        offsetX = minOffsetX

                    // Check vertical offset
                    if (offsetY >= maxOffsetY)
                        offsetY = maxOffsetY
                    else if (offsetY <= minOffsetY)
                        offsetY = minOffsetY


                    // Log.d("gesture_offset", "touchHandler: offset : X = $offsetX y = $offsetY | zoom $zoom")
                    onZoomOffsetChanged(
                        Offset(offsetX, offsetY)
                    )
                }

                // Check if touch slop is exceeded
                if (!touchSlopExceeded) {
                    val move = event.changes.firstOrNull()?.position
                    if (move != null && (move - down.position).getDistance() > touchSlop) {
                        touchSlopExceeded = true
                    }
                }

            } while (event.changes.any { it.pressed })

            // Handle single and double tap

            if (!touchSlopExceeded) {
                if (System.currentTimeMillis() - lastTapTime < 300L) {
                    tapCount++
                } else {
                    tapCount = 1
                }
                lastTapTime = System.currentTimeMillis()

                if (tapCount == 1) {
                    coroutineScope.launch {
                        delay(300L) // Wait to check if another tap is detected
                        if (tapCount == 1) {
                            onSingleTap()
                            tapCount = 0
                            Log.d(
                                "gesture",
                                "Single tap detected"
                            )
                        }
                    }
                } else if (tapCount == 2) {
                    Log.d(
                        "gesture",
                        "Double tap detected"
                    )
                    tapCount = 0
                    onDoubleTap()
                }
            }
        }

    }
}

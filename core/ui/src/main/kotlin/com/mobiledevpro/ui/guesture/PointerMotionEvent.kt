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
package com.mobiledevpro.ui.guesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.pointerMotionEvents(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (PointerInputChange) -> Unit = {},
    onUp: (PointerInputChange) -> Unit = {},
    onCancelDown: () -> Unit = {},
    delayAfterDownInMillis: Long = 0L,
    requireUnconsumed: Boolean = true,
    pass: PointerEventPass = PointerEventPass.Main,
    key1: Any? = Unit
) = this.then(
    Modifier.pointerInput(key1) {
        detectMotionEvents(
            onDown,
            onMove,
            onUp,
            onCancelDown,
            delayAfterDownInMillis,
            requireUnconsumed,
            pass
        )
    }
)

suspend fun PointerInputScope.detectMotionEvents(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (PointerInputChange) -> Unit = {},
    onUp: (PointerInputChange) -> Unit = {},
    onCancelDown: () -> Unit = {},
    delayAfterDownInMillis: Long = 0L,
    requireUnconsumed: Boolean = true,
    pass: PointerEventPass = PointerEventPass.Main
) {

    coroutineScope {
        awaitEachGesture {

            // Wait for at least one pointer to press down, and set first contact position
            val down: PointerInputChange = awaitFirstDown(
                requireUnconsumed = requireUnconsumed,
                pass = pass
            )

            onDown(down)

            var pointer = down
            // Main pointer is the one that is down initially
            var pointerId = down.id

            // If a move event is followed fast enough down is skipped, especially by Canvas
            // to prevent it we add delay after first touch
            var waitedAfterDown = false

            launch {
                delay(delayAfterDownInMillis)
                waitedAfterDown = true
            }

            while (true) {

                val event: PointerEvent = awaitPointerEvent(pass)

                // Don't move by 2 more fingers
                if (event.changes.size > 1) {
                    onCancelDown()
                    pointer.consume()
                    break
                }

                val anyPressed = event.changes.any { it.pressed }

                // There are at least one pointer pressed
                if (anyPressed) {
                    // Get pointer that is down, if first pointer is up
                    // get another and use it if other pointers are also down
                    // event.changes.first() doesn't return same order
                    val pointerInputChange =
                        event.changes.firstOrNull { it.id == pointerId }
                            ?: event.changes.first()

                    // Next time will check same pointer with this id
                    pointerId = pointerInputChange.id
                    pointer = pointerInputChange

                    if (waitedAfterDown) {
                        onMove(pointer)
                    }
                } else {
                    // All of the pointers are up
                    onUp(pointer)
                    break
                }
            }
        }
    }
}
package com.pomodorofocus.app.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RotaryNumberPicker(
    values: List<Int>,
    selectedValue: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val initialIndex = values.indexOf(selectedValue).coerceAtLeast(0)

    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val index = listState.firstVisibleItemIndex
            val value = values.getOrNull(index) ?: selectedValue
            onSelected(value)
        }
    }

    Box(modifier = modifier.height(180.dp)) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(values) { index, value ->
                val centerIndex = listState.firstVisibleItemIndex
                val distance = kotlin.math.abs(index - centerIndex)
                val alpha = (1f - (distance * 0.2f)).coerceAtLeast(0.3f)
                val scale = (1f - (distance * 0.05f)).coerceAtLeast(0.8f)
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(6.dp)
                        .alpha(alpha)
                        .scale(scale)
                )
            }
        }
    }
}

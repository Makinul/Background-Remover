package com.makinul.background.remover.ui.main

import com.makinul.background.remover.data.model.Point

interface ZoomableImageListener {
    fun onDrag()
    fun onEdit(points: List<Point>)
}
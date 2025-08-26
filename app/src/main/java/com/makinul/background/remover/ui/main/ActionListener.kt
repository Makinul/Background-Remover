package com.makinul.background.remover.ui.main

import com.makinul.background.remover.data.model.Point

enum class ImageState {
    NONE, EDIT, DRAG, ZOOM
}

interface ActionListener {
    fun onEdit(point: Point)
    fun onDragStarted()
    fun onComplete(imageState: ImageState, points: List<Point>)
}
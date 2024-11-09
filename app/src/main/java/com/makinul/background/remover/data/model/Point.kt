package com.makinul.background.remover.data.model

data class Point(val x: Float, val y: Float, val z: Float = -1f) {
    override fun toString(): String {
        return "x = ${x}f, y = ${y}f"
    }
}
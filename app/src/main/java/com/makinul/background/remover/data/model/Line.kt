package com.makinul.background.remover.data.model

data class Line(val pointA: Point, val pointB: Point) {
    override fun toString(): String {
        return "pointA x: ${pointA.x}, y: ${pointA.y}, z: ${pointA.z}, pointB x: ${pointB.x}, y: ${pointB.y}, z: ${pointB.z}, "
    }
}
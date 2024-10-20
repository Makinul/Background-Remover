package com.makinul.background.remover.data.model

data class Angle(val pointA: Point, val pointB: Point, val pointC: Point, val clockWise: Boolean) {
    override fun toString(): String {
        return "pointA x: ${pointA.x}, y: ${pointA.y}, z: ${pointA.z}\n" +
                "pointB x: ${pointB.x}, y: ${pointB.y}, z: ${pointB.z}\n" +
                "pointC x: ${pointC.x}, y: ${pointC.y}, z: ${pointC.z}"
    }
}
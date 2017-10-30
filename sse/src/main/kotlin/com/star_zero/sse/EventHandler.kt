@file:Suppress("PackageName")

package com.star_zero.sse

interface EventHandler {

    fun onOpen()

    fun onMessage(event: MessageEvent)

    fun onError(e: Exception)
}

package com.qiscus.pocagora.ui.presentation.route

sealed class Route(val route: String) {
    object Login : Route("Login")
    object Video : Route("Video/{channel}/{role}") {
        fun createRoute(channel: String, role: String) = "Video/${channel}/${role}"
    }
}
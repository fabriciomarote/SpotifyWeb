package main

import controllers.PlaylistController
import controllers.SpotifyController
import controllers.UserController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.security.RouteRole
import io.javalin.core.util.RouteOverviewPlugin
import org.api.token.TokenAccessManager
import org.github.unqui.getSpotifyService
import token.JwtController

enum class Roles: RouteRole {
    ANYONE,USER
}
class Api {

    fun start() {
        val system = getSpotifyService()
        val jwtController = JwtController(system)
        val userController = UserController(system)
        val playlistController = PlaylistController(system)
        val spotifyController = SpotifyController(system)

        val app = Javalin.create {
            it.defaultContentType = "application/json"
            it.registerPlugin(RouteOverviewPlugin("/routes"))
            it.accessManager(TokenAccessManager(jwtController))
            it.enableCorsForAllOrigins()
        }

        app.before {
            it.header("Access-Control-Expose-Headers", "*")
        }

        app.start(7070)

        app.routes {
            path("*") {
                post(spotifyController::addSong, Roles.USER)
            }
            path("register") {
                post(userController::register, Roles.ANYONE)
            }
            path("login") {
                post(userController::login, Roles.ANYONE)
            }
            path("user") {
                get(userController::getUser, Roles.USER)
                put(userController::editUser, Roles.USER)
                post(userController::addPlaylist, Roles.USER)
                path("{userId}") {
                    get(userController::getUserById, Roles.USER)
                }
            }
            path("playlist") {
                get(spotifyController::getPlaylists, Roles.USER)
                path("{playlistId}") {
                    get(playlistController::getPlaylistById, Roles.USER)
                    put(playlistController::likePlaylistById, Roles.USER)
                    patch(playlistController::editPlaylist, Roles.USER)
                }
            }
            path("search") {
                get(spotifyController::getSearch, Roles.USER)
            }
            path("songs") {
                get(spotifyController::getSongs, Roles.USER)
            }
        }
    }
}

fun main() {
    Api().start()
}

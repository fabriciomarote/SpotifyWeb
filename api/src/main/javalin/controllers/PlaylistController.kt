package controllers

import SpotifyService
import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse
import org.github.unqui.PlayListDraft
import org.github.unqui.PlaylistException
import responses.*

class PlaylistController(private val system: SpotifyService) {

    fun getPlaylistById(ctx: Context) {
        val id = ctx.pathParam("{playlistId}")
        try{
            val playlist = system.getPlaylist(id)
            val playlistResponse = PlaylistResponse(playlist.id, playlist.name, playlist.image, playlist.image, playlist.songs, Transform.userToSimpleUser(playlist.author), Transform.dateFormattedDate(playlist.lastModifiedDate), Transform.likesToSimpleUsers(playlist.likes), playlist.duration())
            ctx.status(200).json(playlistResponse)
        } catch (e: PlaylistException){
            ctx.status(404).json(ErrorResponse("Not found playlist with id $id"))
        }
    }

    fun likePlaylistById(ctx: Context) {
        val id = ctx.pathParam("{playlistId}")
        try{
            val userId = getUserId(ctx)
            val user = system.getUser(userId)
            system.addOrRemoveLike(userId, id)
            val userResponse = UserResponse(user.id, user.displayName, user.image, Transform.playlistsToSimplePlaylists(user.myPlaylists), Transform.playlistsToSimplePlaylists(user.likes))
            ctx.status(200).json(userResponse)
        } catch (e: PlaylistException){
            ctx.status(404).json(ErrorResponse("Not found playlist with id $id"))
        }
    }

    fun editPlaylist(ctx: Context) {
        val id = ctx.pathParam("{playlistId}")
        val userId = getUserId(ctx)
        val body = ctx.bodyValidator<PlaylistDraftResponse>()
            .check({ it.name.isNotEmpty() }, "Name cannot be empty")
            .check({ it.description.isNotEmpty() }, "Description cannot be empty")
            .check({ it.image.isNotEmpty() }, "Image cannot be empty")
            .get()
        try {
            val playlist = system.modifyPlaylist(userId, id, PlayListDraft(body.name, body.description, body.image, body.songs))
            val playlistResponse = PlaylistResponse(playlist.id, playlist.name, playlist.image, playlist.image, playlist.songs, Transform.userToSimpleUser(playlist.author), Transform.dateFormattedDate(playlist.lastModifiedDate), Transform.likesToSimpleUsers(playlist.likes), playlist.duration())
            ctx.status(200).json(playlistResponse)
        } catch (e: PlaylistException){
            ctx.status(404).json(ErrorResponse("Not found playlist with id $id"))
        } catch (e: PlaylistException ){
            ctx.status(404).json(ErrorResponse("Not found playlist with id $id"))
        }
    }

    private fun getUserId(ctx: Context) : String {
        val id : String? = ctx.attribute("id")
        if (id == null) {
            UnauthorizedResponse()
        }
        return id!!
    }
}
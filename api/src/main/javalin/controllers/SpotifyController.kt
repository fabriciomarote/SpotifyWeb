package controllers

import SpotifyService
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.github.unqui.SongDraft
import org.github.unqui.SongException
import responses.*

class SpotifyController(private val system: SpotifyService) {

    fun getSearch(ctx: Context){
        val search = ctx.queryParam("q") ?: throw BadRequestResponse ("Nothing to search")
        val playlists = system.searchPlaylist(search)
        val songs = system.searchSong(search)
        val users = system.searchUser(search)
        val searchResponse = SearchResponse(Transform.playlistsToSimplePlaylists(playlists), songs, Transform.likesToSimpleUsers(users))
        ctx.status(200).json(searchResponse)
    }

    fun addSong(ctx: Context) {
        val body = ctx.bodyValidator<SongDraftResponse>()
            .check({ it.name.isNotEmpty() }, "Name cannot be empty")
            .check({ it.band.isNotEmpty() }, "Band cannot be empty")
            .check({ it.url.isNotEmpty() }, "Url cannot be empty")
            .check({ it.duration > 0 }, "Duration cannot be empty")     //consultar
            .get()
        try {
            val song = system.addSong(SongDraft(body.name, body.band, body.url, body.duration))
            ctx.status(200).json(song)
        } catch (e: SongException) {
            ctx.status(404).json(ErrorResponse("Existe una cancion con el mismo nombre"))
        }
    }

    fun getSongs(ctx: Context) {
        val songs = system.getAllSongs()
        ctx.status(200).json(songs)
    }

    fun getPlaylists(ctx: Context) {
        val playlists = system.getAllPlaylists()
        ctx.status(200).json(Transform.playlistsToSimplePlaylists(playlists))
    }

}
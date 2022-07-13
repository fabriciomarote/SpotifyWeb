package controllers

import SpotifyService
import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse
import org.api.token.TokenAccessManager
import org.github.unqui.*
import responses.*
import token.JwtController

class UserController(private val system: SpotifyService) {

    val tokenJWT = JwtController(system)

    fun register(ctx: Context){
        val body = ctx.bodyValidator<UserSimpleResponse>()
            .check({ it.name.isNotEmpty() }, "Name cannot be empty")
            .check({ it.email.isNotEmpty() }, "Email cannot be empty")
            .check({
                "^[a-zA-Z0-9.!#$%&'+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)$"
                    .toRegex()
                    .matches(it.email)
            }, "Invalid email address")
            .check({ it.password.isNotEmpty() }, "Password cannot be empty")
            .check({ it.image.isNotEmpty() }, "Image cannot be empty")
            .get()
        try {
            val user = system.register(UserDraft(body.email, body.image, body.password, body.name))
            ctx.status(201).json(RegisterResponse(user.id, user.displayName, user.image, Transform.playlistsToSimplePlaylists(user.myPlaylists), Transform.playlistsToSimplePlaylists(user.likes)))
        }catch (e: UserException){
            ctx.status(404).json(ErrorResponse("The e-mail is not available"))
        }
    }

    fun login(ctx: Context){
        val body = ctx.bodyValidator<LoginResponse>()
            .check({ it.email.isNotEmpty() }, "Email cannot be empty")
            .check({
                "^[a-zA-Z0-9.!#$%&'+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)$"
                    .toRegex()
                    .matches(it.email)
            }, "Invalid email address")
            .check({ it.password.isNotEmpty() }, "Password cannot by empty")
            .get()
        try {
            val user = system.login(body.email, body.password)
            ctx.header("Authorization", tokenJWT.generate(user))
            ctx.status(200).json(RegisterResponse(user.id, user.displayName, user.image, Transform.playlistsToSimplePlaylists(user.myPlaylists), Transform.playlistsToSimplePlaylists(user.likes)))
        }catch (e: UserException){
            ctx.status(404).json(LoginErrorResponse("error", "User not found"))
        }
    }

    fun getUser(ctx: Context) {
        val userId = getUserId(ctx)
        val user = system.getUser(userId)
        val userResponse = UserResponse(user.id, user.displayName, user.image, Transform.playlistsToSimplePlaylists(user.myPlaylists), Transform.playlistsToSimplePlaylists(user.likes))
        ctx.status(200).json(userResponse)
    }

    fun getUserById(ctx: Context) {
        val id = ctx.pathParam("{userId}")
        try{
            val user = system.getUser(id)
            val userResponse = UserResponse(user.id, user.displayName, user.image, Transform.playlistsToSimplePlaylists(user.myPlaylists), Transform.playlistsToSimplePlaylists(user.likes))
            ctx.status(200).json(userResponse)
        } catch (e: UserException){
            ctx.status(404).json(ErrorResponse("Not found user with id $id"))
        }
    }

    fun addPlaylist(ctx: Context) {
        val userId = getUserId(ctx)
        val body = ctx.bodyValidator<PlaylistDraftResponse>()
            .check({ it.name.isNotEmpty() }, "Name cannot be empty")
            .check({ it.description.isNotEmpty() }, "Description cannot be empty")
            .check({ it.image.isNotEmpty() }, "Image cannot be empty")
            .get()
        try{
            val playlist = system.addPlaylist(userId, PlayListDraft(body.name, body.description, body.image, body.songs))
            ctx.status(200).json(PlaylistResponse(playlist.id, playlist.name, playlist.description, playlist.image, playlist.songs, Transform.userToSimpleUser(playlist.author), Transform.dateFormattedDate(playlist.lastModifiedDate), Transform.likesToSimpleUsers(playlist.likes), playlist.duration()))
        } catch (e: PlaylistException ){
            ctx.status(404).json(ErrorResponse("Not found playlist with id $userId"))
        } catch (e: UserException ){
            ctx.status(404).json(ErrorResponse("Not found user with id $userId"))
        }
    }

    fun editUser(ctx: Context) {
        val userId = getUserId(ctx)
        val body = ctx.bodyValidator<UserDraftResponse>()
            .check({ it.image.isNotEmpty() }, "Image cannot be empty")
            .check({ it.password.isNotEmpty() }, "Password cannot be empty")
            .check({ it.displayName.isNotEmpty() }, "DisplayName cannot be empty")
            .get()
        try {
            val user = system.editUser(userId, EditUser(body.image,body.password, body.displayName))
            val userResponse = UserResponse(user.id, user.displayName, user.image, Transform.playlistsToSimplePlaylists(user.myPlaylists), Transform.playlistsToSimplePlaylists(user.likes))
            ctx.status(200).json(userResponse)
        } catch (e: UserException) {
            ctx.status(404).json(ErrorResponse("result : Not found user with $userId"))
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
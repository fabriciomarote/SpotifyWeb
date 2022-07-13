package responses

import org.github.unqui.Playlist
import org.github.unqui.Song
import org.github.unqui.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class OkResponse(val result: String = "ok")

data class ErrorResponse(val result: String)

data class UserSimpleResponse(val name: String,
                              val email: String,
                              val password: String,
                              val image: String)

data class RegisterResponse(val id: String,
                            val displayName: String,
                            val image: String,
                            val myPlaylist: List<SimplePlaylist>,
                            val likes: List<SimplePlaylist>)

data class LoginResponse(val email: String,
                         val password: String)

data class LoginErrorResponse(val result: String,
                              val message: String)

data class UserResponse(val id: String,
                        val name: String,
                        val image: String,
                        val myPlaylist: List<SimplePlaylist>,
                        val likes: List<SimplePlaylist>)

data class PlaylistResponse(val id: String,
                            val name: String,
                            val description: String,
                            val image: String,
                            val songs: List<Song>,
                            val author: SimpleUser,
                            val lastModifiedDate: String,
                            val likes: List<SimpleUser>,
                            val duration: Int)

data class SimplePlaylist(val id: String,
                          val name: String,
                          val description: String,
                          val image: String,
                          val author: SimpleUser,
                          val lastModifiedDate: String,
                          val likes: List<SimpleUser>,
                          val duration: Int)

data class SimpleUser(val id: String,
                      val displayName: String,
                      val image: String)

data class SearchResponse(val playlists: List<SimplePlaylist>,
                          val songs: List<Song>,
                          val users: List<SimpleUser>)

data class PlaylistDraftResponse(val name: String,
                                 val description: String,
                                 val image: String,
                                 val songs: MutableList<Song>)

data class UserDraftResponse(val image: String,
                             val password: String,
                             val displayName: String)

data class SongDraftResponse(val name: String,
                             val band: String,
                             val url: String,
                             val duration: Int)



class Transform() {
    companion object transformer {

        fun userToSimpleUser(user: User): SimpleUser {
            return SimpleUser(user.id, user.displayName, user.image)
        }

        fun playlistToSimplePlaylist(playlist: Playlist): SimplePlaylist {
            return SimplePlaylist(playlist.id, playlist.name, playlist.description, playlist.image, userToSimpleUser(playlist.author), dateFormattedDate(playlist.lastModifiedDate), likesToSimpleUsers(playlist.likes), playlist.duration())
        }

        fun dateFormattedDate(date: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
            val formatDateTime = LocalDateTime.parse(date.toString())
            return formatDateTime.format(formatter).toString()
        }

        fun likesToSimpleUsers(likes: List<User>): List<SimpleUser>{
            val likesTransformed = likes.map{user -> SimpleUser(user.id, user.displayName, user.image)}
            return likesTransformed
        }

        fun playlistsToSimplePlaylists(playlists: List<Playlist>): List<SimplePlaylist>{
            val playlistsTransformed = playlists.map{playlist -> SimplePlaylist(playlist.id, playlist.name, playlist.description, playlist.image, userToSimpleUser(playlist.author), dateFormattedDate(playlist.lastModifiedDate), likesToSimpleUsers(playlist.likes), playlist.duration())}
            return playlistsTransformed
        }
    }


}
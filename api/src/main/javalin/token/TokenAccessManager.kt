package org.api.token

import io.javalin.core.security.AccessManager
import io.javalin.core.security.RouteRole
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.UnauthorizedResponse
import main.Roles
import org.github.unqui.User
import token.JwtController

class TokenAccessManager(private val jwtController: JwtController) : AccessManager {

    private fun getUser(token: String): User {
        return jwtController.validate(token)
    }

    override fun manage(handler: Handler, ctx: Context, roles: MutableSet<RouteRole>) {
        val token = ctx.header("Authorization")
        when {
            roles.contains(Roles.ANYONE) -> handler.handle(ctx)
            roles.contains(Roles.USER) -> {
                try {
                    if (token === null) { throw UnauthorizedResponse("Token Not Found")}
                    val user = getUser(token)
                    ctx.attribute("id", user)
                    handler.handle(ctx)
                }
                catch(e: UnauthorizedResponse){
                    ctx.status(401).json(mapOf("message" to e.message))
                }
            }
        }
    }
}
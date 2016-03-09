import static ratpack.groovy.Groovy.ratpack
import ratpack.server.BaseDir
import ratpack.jackson.Jackson

ratpack {
    handlers {
        get ("classifier-api/v1/groups/:id") {ctx->
            ctx.response.getHeaders().add('Content-Type','application/json')
            ctx.render(file("assets/classifier-api/v1/_groups/${pathTokens.id}"))
        }

        fileSystem "assets", { f ->
            f.files()
        }
    }

}
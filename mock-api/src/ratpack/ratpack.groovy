import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader

import static ratpack.groovy.Groovy.ratpack
import ratpack.server.BaseDir
import ratpack.jackson.Jackson

ratpack {
    handlers {
        path("classifier-api/v1/groups/:id") { ctx ->
            byMethod { m ->

                m.named("get") {
                    println("get request for ${pathTokens.id}")
                    ctx.response.getHeaders().add('Content-Type', 'application/json')
                    ctx.render(file("assets/classifier-api/v1/_groups/${pathTokens.id}"))
                }
                m.named("post") {
                    println("post request for ${pathTokens.id}")
                    ctx.response.getHeaders().add('Content-Type', 'application/json')

                    def json = new ObjectMapper()

                    //have to do this because file render will force 405 in response to POST
                    def file = new File("src/ratpack/assets/classifier-api/v1/_groups/${pathTokens.id}")
                    println "file ${file.absolutePath}"
                    def data = json.readValue(file, Map)
                    ctx.render(Jackson.json(data))
                }
            }
        }

        fileSystem "assets", { f ->
            f.files()
        }
    }

}
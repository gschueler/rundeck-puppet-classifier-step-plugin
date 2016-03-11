import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.jackson.Jackson

import static ratpack.groovy.Groovy.ratpack

ratpack {
    handlers {
        path("classifier-api/v1/update-classes") { ctx ->
            byMethod { m ->
                m.post {
                    if (request.getHeaders().get("X-Authentication") == 'badtoken') {
                        //simulate authentication failure
                        response.status(500)
                        ctx.response.getHeaders().add('Content-Type', 'application/json')
                        ctx.render(Jackson.json([
                                kind   : 'unexpected-response',
                                msg    : 'an error ocurred blah blah...',
                                details: [
                                        url    : 'blah',
                                        status : 500,
                                        headers: [],
                                        body   : 'blah'
                                ]
                        ]
                        )
                        )
                        return
                    }
                    response.status(201)
                    render('')
                }
            }
        }
        path("classifier-api/v1/groups/:id") { ctx ->
            byMethod { m ->

                m.named("get") {
                    println("get request for ${pathTokens.id}")
                    if (request.getHeaders().get("X-Authentication") == 'badtoken') {
                        //simulate authentication failure
                        response.status(401)
                        ctx.response.getHeaders().add('Content-Type', 'application/json')
                        ctx.render(Jackson.json([msg: 'unauthorized']))
                        return
                    }
                    ctx.response.getHeaders().add('Content-Type', 'application/json')
                    ctx.render(file("assets/classifier-api/v1/_groups/${pathTokens.id}"))
                }
                m.named("post") {
                    println("post request for ${pathTokens.id}")
                    ctx.response.getHeaders().add('Content-Type', 'application/json')

                    def json = new ObjectMapper()
                    if (request.getHeaders().get("X-Authentication") == 'badtoken2') {
                        //simulate authentication failure
                        response.status(401)
                        ctx.response.getHeaders().add('Content-Type', 'application/json')
                        ctx.render(Jackson.json([message: 'unauthenticated']))
                        return
                    }

                    if (pathTokens.id == 'fc500c43-5065-469b-91fc-37ed0e500e81') {
                        //fake error response
                        ctx.response.status(400)
                        ctx.render(Jackson.json(
                                [
                                        "kind"   : "conflicting-ids",
                                        "msg"      : "monkey business",
                                        "details": [
                                                submitted: 'fc500c43-5065-469b-91fc-37ed0e500e81',
                                                fromUrl  : 'fc500c43-5065-469b-91fc-37ed0e500e81'
                                        ]
                                ]
                        )
                        )
                    } else {
                        //have to do this because file render will force 405 in response to POST
                        def file = new File("src/ratpack/assets/classifier-api/v1/_groups/${pathTokens.id}")
                        println "file ${file.absolutePath}"
                        def data = json.readValue(file, Map)
                        ctx.render(Jackson.json(data))
                    }
                }
            }
        }

        fileSystem "assets", { f ->
            f.files()
        }
    }

}
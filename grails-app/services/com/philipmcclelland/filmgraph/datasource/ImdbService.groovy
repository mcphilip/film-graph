package com.philipmcclelland.filmgraph.datasource

import com.philipmcclelland.filmgraph.datasource.types.ImdbMovie
import com.philipmcclelland.filmgraph.datasource.types.RelationshipTypes
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.Node

public class ImdbService {
    ///////////////////////////////////////////////////
    //  Dependency Injections
    ///////////////////////////////////////////////////

    def cypherQueryService
    def cypherExecutionService

    ///////////////////////////////////////////////////
    //  Properties
    ///////////////////////////////////////////////////

    static transactional = false
    private final String FIND_ACTOR_BY_NAME_URL_TEMPLATE = "http://www.imdb.com/find?q=\$actorName&s=nm&exact=true&ref_=fn_nm_ex"

    ///////////////////////////////////////////////////
    //  Public Methods
    ///////////////////////////////////////////////////

    public Node loadImdbMovieNode(ImdbMovie imdbMovie) {
        log.trace("[createImdbMovieNode] params: [imdbMovie: $imdbMovie")

        Node imdbMovieNode = cypherQueryService.createInstanceOfLabel(ImdbMovie.NEO4J_LABEL, imdbMovie.toMap(true))


        createActorRelationships(imdbMovie, imdbMovieNode)
        createGenreRelationships(imdbMovie, imdbMovieNode)
        createWriterRelationships(imdbMovie, imdbMovieNode)
        createDirectorRelationships(imdbMovie, imdbMovieNode)

        return imdbMovieNode
    }

    public void createDirectorRelationships(ImdbMovie imdbMovie, Node imdbMovieNode) {
        log.trace("[createDirectorRelationships()] params: [imdbMovie: $imdbMovie, imdbMovieNode: $imdbMovieNode]")

        imdbMovie.directors.each({String directorName ->
            Map directorProperties = [name: directorName]

            Node directorNode = cypherQueryService.createInstanceOfLabel("director", directorProperties)

            String cypherQuery = """
                MATCH
                    m, d
                WHERE
                    ID(m) = { movieNodeId } AND
                    ID(d) = { directorNodeId }
                CREATE UNIQUE
                    (m)<-[:$RelationshipTypes.DIRECTOR_OF]-(d)
            """

            ExecutionResult executionResult = cypherExecutionService.executeCypher(cypherQuery, [movieNodeId: imdbMovieNode.id, directorNodeId: directorNode.id])
        })
    }

    public void createWriterRelationships(ImdbMovie imdbMovie, Node imdbMovieNode) {
        log.trace("[createWriterRelationships()] params: [imdbMovie: $imdbMovie, imdbMovieNode: $imdbMovieNode]")

        imdbMovie.writers.each({String writerName ->
            Map writerProperties = [name: writerName]

            Node writerNode = cypherQueryService.createInstanceOfLabel("writer", writerProperties)

            String cypherQuery = """
                MATCH
                    m, w
                WHERE
                    ID(m) = { movieNodeId } AND
                    ID(w) = { writerNodeId }
                CREATE UNIQUE
                    (m)<-[:$RelationshipTypes.WRITER_OF]-(w)
            """

            ExecutionResult executionResult = cypherExecutionService.executeCypher(cypherQuery, [movieNodeId: imdbMovieNode.id, writerNodeId: writerNode.id])
        })
    }

    public void createGenreRelationships(ImdbMovie imdbMovie, Node imdbMovieNode) {
        log.trace("[createGenreRelationships()] params: [imdbMovie: $imdbMovie, imdbMovieNode: $imdbMovieNode]")

        imdbMovie.genres.each({String genreName ->
            Map genreProperties = [name: genreName]

            Node genreNode = cypherQueryService.createInstanceOfLabel("genre", genreProperties)

            String cypherQuery = """
                MATCH
                    m, a
                WHERE
                    ID(m) = { movieNodeId } AND
                    ID(a) = { genreNodeId }
                CREATE UNIQUE
                    (m)<-[:$RelationshipTypes.GENRE_OF]-(a)
            """

            ExecutionResult executionResult = cypherExecutionService.executeCypher(cypherQuery, [movieNodeId: imdbMovieNode.id, genreNodeId: genreNode.id])
        })
    }

    public void createActorRelationships(ImdbMovie imdbMovie, Node imdbMovieNode) {
        log.trace("[createActorRelationships()] params: [imdbMovie: $imdbMovie, imdbMovieNode: $imdbMovieNode]")

        imdbMovie.actors.each({String actorName ->
            Map actorProperties = [name: actorName]

            Node actorNode = cypherQueryService.createInstanceOfLabel("actor", actorProperties)

            String cypherQuery = """
                MATCH
                    m, a
                WHERE
                    ID(m) = { movieNodeId } AND
                    ID(a) = { actorNodeId }
                CREATE UNIQUE
                    (m)<-[:$RelationshipTypes.ACTED_IN]-(a)
            """

            ExecutionResult executionResult = cypherExecutionService.executeCypher(cypherQuery, [movieNodeId: imdbMovieNode.id, actorNodeId: actorNode.id])
        })
    }

    ///////////////////////////////////////////////////
    //  Private Methods
    ///////////////////////////////////////////////////
}
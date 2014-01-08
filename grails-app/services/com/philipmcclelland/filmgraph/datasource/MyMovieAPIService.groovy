package com.philipmcclelland.filmgraph.datasource

import com.philipmcclelland.filmgraph.datasource.types.AfiTop100Movie
import com.philipmcclelland.filmgraph.datasource.types.ImdbMovie
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONElement
import org.neo4j.graphdb.Node
import org.neo4j.cypher.javacompat.ExecutionResult

class MyMovieAPIService {
    ///////////////////////////////////////////////////
    //  Dependency Injections
    ///////////////////////////////////////////////////

    def resourceLoaderService
    def cypherQueryService
    def imdbService
    def cypherExecutionService

    ///////////////////////////////////////////////////
    //  Properties
    ///////////////////////////////////////////////////

    static transactional = false

    private final String API_URL_TEMPLATE = "http://mymovieapi.com/?type=json&plot=simple&episode=0&lang=en-US&aka=simple&release=simple&business=0&tech=0&ids=\$ids"
    private final String DATA_SOURCE_FOLDER_LOCATION = "data-sources/my-movie-API/"
    private static final String JSON_RESPONSE_FILE_NAME_TEMPLATE = "n\$startRank-n\$endRank.json"
    private List<Range<Integer>> MY_MOVIE_API_RESPONSE_JSON_FILE_START_AND_END_RANGES = [
            1..10,
            11..20,
            21..30,
            31..40,
            41..50,
            51..60,
            61..70,
            71..80,
            81..90,
            91..100
    ]


    ///////////////////////////////////////////////////
    //  Public Methods
    ///////////////////////////////////////////////////

    public List<String> loadAPIDataForAfiTop100Movies() {
        log.trace("[loadAPIDataForAfiTop100Movies()] params: []")

        if( !areJSONResponsesSavedToFileSystem() ) {
            log.info("Request data from MyMovieAPI...")

            cypherQueryService.pageThroughInstancesOfLabel(AfiTop100Movie.NEO4J_LABEL, loadJSONForAfiMovies, 10 )
        }

        if( !areImdbMoviesLoaded() ) {
            log.info("Loading IMDB movie information...")

            loadImdbMoviesFromMyMovieApiJson()
        }

        log.info("MyMovieAPI data received")

    }


    def loadJSONForAfiMovies = { List<Node> afiMovieNodes ->
        log.trace("[apiDataClosure()] params: [afiMovieNodes: $afiMovieNodes]")

        String idsUrlEncoded = afiMovieNodes.collect({ return it.getProperty("imdbId")}).join(",").encodeAsURL()

        URLConnection myMovieAPIConnection = API_URL_TEMPLATE.replace('$ids', idsUrlEncoded).toURL().openConnection()

        JsonSlurper jsonSlurper = new JsonSlurper()
        Object jsonResult = jsonSlurper.parseText(myMovieAPIConnection.content.text)

        String prettyJSON = (jsonResult as JSON).toString(true)
        log.info("[apiDataClosure()] jsonResult: $prettyJSON")

        Integer startRank = (Integer)afiMovieNodes.get(0).getProperty("rank")
        Integer endRank = (Integer)afiMovieNodes.get(afiMovieNodes.size() - 1).getProperty("rank")

        String fileName = JSON_RESPONSE_FILE_NAME_TEMPLATE.replace('$startRank', startRank.toString()).replace('$endRank', endRank.toString())

        FileWriter fileWriter = new FileWriter(fileName)
        fileWriter.write(prettyJSON)
        fileWriter.flush()
        fileWriter.close()
    }

    ///////////////////////////////////////////////////
    //  Private Methods
    ///////////////////////////////////////////////////

    private void loadImdbMoviesFromMyMovieApiJson() {
        log.trace("[loadImdbMoviesFromMyMovieApiJson()] params: []")

        MY_MOVIE_API_RESPONSE_JSON_FILE_START_AND_END_RANGES.each({Range<Integer> range ->
            String fileName = getJsonResponseFileNameFromRange(range)

            File apiResponseFile = resourceLoaderService.loadFile(fileName)

            JSONElement myMovieApiJsonResponse = JSON.parse(new FileInputStream(apiResponseFile), "UTF-8")

            myMovieApiJsonResponse.each({JSONElement myMovieApiMovieJsonElement ->
                ImdbMovie imdbMovie = ImdbMovie.loadFromMyMovieApiJson(myMovieApiMovieJsonElement)
                
                setAFIRanking(imdbMovie)

                Node imdbMovieNode = imdbService.loadImdbMovieNode(imdbMovie)
            })

        })

    }
    
    private void setAFIRanking(ImdbMovie imdbMovie) {
        Integer afiRank = 0

        String cypherQuery = """
            MATCH
                (m)
            WHERE
                HAS(m.rank)
                AND HAS(m.imdbId)
            WITH
                m as afiMovie
            WHERE
                afiMovie.imdbId = { currentImdbId }
            RETURN
                afiMovie.rank as afiRank
        """

        ExecutionResult result = cypherExecutionService.executeCypher(cypherQuery, [currentImdbId: imdbMovie.imdbId])

        afiRank = (Integer)cypherQueryService.getFirstCypherIteratorResultColumnValue(result, "afiRank") ?: -1
        
        log.trace("[setAFIRanking] afiRank: ${afiRank}, imdbMovie.imdbId: ${imdbMovie.imdbId}")

        imdbMovie.afiRank = afiRank
    }

    private boolean areImdbMoviesLoaded() {
        log.trace("[areImdbMoviesLoaded()] params: []")

        boolean moviesLoaded = false

        Integer imdbMovieCount = cypherQueryService.countInstancesOfLabel(ImdbMovie.NEO4J_LABEL)

        if( imdbMovieCount >= 100 ) {
            moviesLoaded = true
        }

        return moviesLoaded
    }

    private boolean areJSONResponsesSavedToFileSystem() {
        log.trace("[areJSONResponsesSavedToFileSystem()] params: []")

        boolean areJSONResponsesSaved = false

        try {
            MY_MOVIE_API_RESPONSE_JSON_FILE_START_AND_END_RANGES.each({Range<Integer> range ->
                String fileName = getJsonResponseFileNameFromRange(range)

                resourceLoaderService.loadFile(fileName)
            })

            areJSONResponsesSaved = true
        }
        catch(ex) {

        }


        return areJSONResponsesSaved
    }

    private String getJsonResponseFileNameFromRange(Range<Integer> range) {
        String fileName = DATA_SOURCE_FOLDER_LOCATION + JSON_RESPONSE_FILE_NAME_TEMPLATE.replace('$startRank', range.from.toString()).replace('$endRank', range.to.toString())

        return fileName
    }
}
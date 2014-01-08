package com.philipmcclelland.filmgraph.datasource

import com.philipmcclelland.filmgraph.datasource.types.AfiTop100Movie
import com.philipmcclelland.filmgraph.util.HttpUtils
import org.apache.commons.io.IOUtils
import org.neo4j.graphdb.Node

class AfiTop100Service {
    ///////////////////////////////////////////////////
    //  Dependency Injections
    ///////////////////////////////////////////////////

    def cypherQueryService
    def resourceLoaderService

    ///////////////////////////////////////////////////
    //  Properties
    ///////////////////////////////////////////////////

    static transactional = false
    private final String AFI_TOP_100_HTML_URL = "http://www.imdb.com/list/RWlAaSYvw-8"
    private final String DATA_SOURCES_FOLDER = "data-sources/afi-top-100-movies/"
    private final String AFI_TOP_100_HTML_RIP_FILE_NAME = "afi-top-100-movies-imdb-list.html"


    ///////////////////////////////////////////////////
    //  Public Methods
    ///////////////////////////////////////////////////

    public void initializeAfiTop100Movies() {
        log.debug("[initializeAfiTop100Movies()] params: []")

        if( isInitializationRequired() ) {
            log.info("Scraping AFI top 100 movies from website...")

            def htmlParser = chooseHtmlParser()

            List<AfiTop100Movie> top100Movies = scrapeTop100Movies(htmlParser)

            top100Movies.each({AfiTop100Movie movie ->
                createMovieNode(movie)
            })
        }

        log.info("AFI top 100 movie ranks and imdbIds loaded")
    }

    ///////////////////////////////////////////////////
    //  Private Methods
    ///////////////////////////////////////////////////

    private def chooseHtmlParser() {
        String rippedHtmlFilePath = DATA_SOURCES_FOLDER + AFI_TOP_100_HTML_RIP_FILE_NAME

        InputStream inputStream

        if( resourceLoaderService.doesFileExist(rippedHtmlFilePath) ) {
            File rippedHtmlFile = resourceLoaderService.loadFile(rippedHtmlFilePath)

            inputStream = new FileInputStream(rippedHtmlFile)
        }
        else {
            inputStream = HttpUtils.loadHttpGetResponseInputStream(AFI_TOP_100_HTML_URL)

            String htmlAsText = IOUtils.toString(inputStream, "UTF-8")

            FileWriter fileWriter = new FileWriter(AFI_TOP_100_HTML_RIP_FILE_NAME)
            fileWriter.write(htmlAsText)
            fileWriter.flush()
            fileWriter.close()
        }

        return HttpUtils.loadHtmlParser(inputStream)
    }

    private void createMovieNode(AfiTop100Movie movie) {
        Node movieNode = cypherQueryService.createInstanceOfLabel(AfiTop100Movie.NEO4J_LABEL, movie.toMap())
    }

    private List<AfiTop100Movie> scrapeTop100Movies(def htmlParser) {
        log.trace("[scrapeTop100Movies()] params: []")

        List<AfiTop100Movie> scrapedMovies = []

        def imdbIDs = htmlParser.'**'.findAll({return it.'@data-const' != ''}).'@data-const'*.text()

        imdbIDs.eachWithIndex({String imdbID, int idx ->
            AfiTop100Movie movie = new AfiTop100Movie()

            movie.imdbId = imdbID
            movie.rank = idx + 1

            scrapedMovies.add(movie)
        })

        return scrapedMovies
    }

    private boolean isInitializationRequired() {
        log.trace("[isInitializationRequired()] params: []")

        boolean initializationRequired = true

        Integer count = cypherQueryService.countInstancesOfLabel(AfiTop100Movie.NEO4J_LABEL)

        if( count == 100 ) {
            initializationRequired = false
        }

        return initializationRequired
    }
}
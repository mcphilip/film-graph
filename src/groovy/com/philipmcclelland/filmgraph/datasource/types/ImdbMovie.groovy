package com.philipmcclelland.filmgraph.datasource.types

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONElement

public class ImdbMovie implements Serializable {
    public static final String NEO4J_LABEL = "imdbMovie"

    private static final Log log = LogFactory.getLog(ImdbMovie.class)

    public String imdbId
    public Float imdbRating
    public String imdbUrl
    public String title
    public Integer afiRank

    public Integer releaseDate //YYYYMMDD format

    public List<String> actors = []
    public List<String> directors = []
    public List<String> genres = []
    public List<String> writers = []

    public String toString() {
        return "[imdbId: $imdbId, title: $title]"
    }

    public Map toMap(boolean onlyIncludePrimitives = true) {
        Map map = [:]

        map.imdbId = this.imdbId
        map.imdbRating = this.imdbRating
        map.imdbUrl = this.imdbUrl
        map.title = this.title
        map.afiRank = this.afiRank

        map.releaseDate = this.releaseDate

        if( !onlyIncludePrimitives ) {
            map.actors = this.actors
            map.directors = this.directors
            map.genres = this.genres
            map.writers = this.writers
        }

        return map
    }

    public static ImdbMovie loadFromMyMovieApiJson(JSONElement movieJson) {
        log.trace("[fromMyMovieApiJson()] params: [movieJson: $movieJson]")

        ImdbMovie movie = new ImdbMovie()

        movie.imdbId = movieJson["imdb_id"]
        movie.imdbRating = movieJson["rating"].toString().isFloat() ? (Float)movieJson["rating"] : -1F
        movie.imdbUrl = movieJson["imdb_url"]
        movie.title = movieJson["title"]

        movie.releaseDate = (Integer)movieJson["release_date"]

        movie.actors = (List<String>)movieJson["actors"]
        movie.directors = (List<String>)movieJson["directors"]
        movie.genres = (List<String>)movieJson["genres"]
        movie.writers = (List<String>)movieJson["writers"]

        return movie
    }
}
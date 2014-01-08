package com.philipmcclelland.filmgraph.datasource.types

public class AfiTop100Movie implements Serializable {
    public static final String NEO4J_LABEL = "afiTop100Movie"

    public Integer rank
    public String imdbId

    public String toString() {
        return "[rank: ${this.rank}, imdbId: ${this.imdbId}]"
    }

    public Map toMap() {
        Map map = [:]

        map["rank"] = this.rank
        map["imdbId"] = this.imdbId

        return map
    }
}
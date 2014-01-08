package com.philipmcclelland.filmgraph.datasource

import com.philipmcclelland.filmgraph.datasource.types.ImdbMovie
import com.philipmcclelland.filmgraph.datasource.types.RelationshipTypes
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship

public class ReleaseDateService {
    ///////////////////////////////////////////////////
    //  Dependency Injections
    ///////////////////////////////////////////////////

    def cypherQueryService

    ///////////////////////////////////////////////////
    //  Properties
    ///////////////////////////////////////////////////

    static transactional = true
    private Map<String, Long> _yearToYearNodeId = [:]
    private Map<String, Long> _monthToMonthNodeId = [:]
    private Map<String, Long> _dayToDayNodeId = [:]

    ///////////////////////////////////////////////////
    //  Public Methods
    ///////////////////////////////////////////////////

    public void initialize() {
        initializeDateComponents()
        initializeImdbMovieReleaseDates()
    }

    ///////////////////////////////////////////////////
    //  Private Methods
    ///////////////////////////////////////////////////

    private void initializeImdbMovieReleaseDates() {
        def imdbMoviePaginationLoop = { List<Node> imdbMovieNodes ->
            imdbMovieNodes.each({ Node imdbMovieNode ->
                Relationship existingRelationship = imdbMovieNode.getSingleRelationship(DynamicRelationshipType.withName(RelationshipTypes.RELEASED_ON.toString()), Direction.OUTGOING)

                if( !existingRelationship ) {
                    Integer releaseDate = (Integer)imdbMovieNode.getProperty("releaseDate")

                    Integer releaseYear = releaseDate.toString().substring(0, 3).toInteger()
                    Integer releaseMonth = releaseDate.toString().substring(4, 5).toInteger()
                    Integer releaseDay = releaseDate.toString().substring(6, 7).toInteger()

                    Long yearNodeId = getYearToYearNodeId()[releaseYear.toString()]
                    Long monthNodeId = getMonthToMonthNodeId()[releaseMonth.toString()]
                    Long dayNodeId = getDayToDayNodeId()[releaseDay.toString()]


                }
            })
        }
    }

    private Map<String, Long> getYearToYearNodeId() {
        if( _yearToYearNodeId.size() == 0 ) {

            def dateComponentPaginationLoop = { List<Node> dateComponentNodes ->
                dateComponentNodes.each({ Node dateComponentNode ->
                    Integer year = (Integer)dateComponentNode.getProperty("value")
                    _yearToYearNodeId[year.toString()] = dateComponentNode.id
                })
            }

            cypherQueryService.pageThroughInstancesOfLabel("year", dateComponentPaginationLoop)
        }

        return _yearToYearNodeId
    }

    private Map<String, Long> getMonthToMonthNodeId() {
        if( _monthToMonthNodeId.size() == 0 ) {

            def dateComponentPaginationLoop = { List<Node> dateComponentNodes ->
                dateComponentNodes.each({ Node dateComponentNode ->
                    Integer month = (Integer)dateComponentNode.getProperty("value")
                    _monthToMonthNodeId[month.toString()] = dateComponentNode.id
                })
            }

            cypherQueryService.pageThroughInstancesOfLabel("month", dateComponentPaginationLoop)
        }

        return _monthToMonthNodeId
    }

    private Map<String, Long> getDayToDayNodeId() {
        if( _dayToDayNodeId.size() == 0 ) {

            def dateComponentPaginationLoop = { List<Node> dateComponentNodes ->
                dateComponentNodes.each({ Node dateComponentNode ->
                    Integer day = (Integer)dateComponentNode.getProperty("value")
                    _dayToDayNodeId[day.toString()] = dateComponentNode.id
                })
            }

            cypherQueryService.pageThroughInstancesOfLabel("day", dateComponentPaginationLoop)
        }

        return _dayToDayNodeId
    }

    private void initializeDateComponents() {
        Integer yearNodeCount = cypherQueryService.countInstancesOfLabel("year")

        if( yearNodeCount == 0 ) {
            150.times({Integer idx ->
                cypherQueryService.createInstanceOfLabel("year", [value: idx + 1900])
            })
        }

        Integer monthCount = cypherQueryService.countInstancesOfLabel("month")

        if( monthCount == 0 ) {
            12.times({Integer idx ->
                cypherQueryService.createInstanceOfLabel("month", [value: idx + 1])
            })
        }

        Integer dayCount = cypherQueryService.countInstancesOfLabel("day")

        if( dayCount == 0) {
            31.times({Integer idx ->
                cypherQueryService.createInstanceOfLabel("day", [value: idx + 1])
            })
        }
    }

}
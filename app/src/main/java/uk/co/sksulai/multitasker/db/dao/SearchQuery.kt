package uk.co.sksulai.multitasker.db.dao

abstract class QueryBuilder {
    var anyStart: Boolean = false
    var anyEnd: Boolean   = false
    var any: Boolean
        get() = anyStart && anyEnd
        set(value) {
            anyStart = value
            anyEnd   = value
        }
}

private class QueryBuilderImpl : QueryBuilder() {
    fun process(query: String): String {
        return "${if(anyStart) '%' else ""}$query${if(anyEnd) '%' else ""}"
    }
}

object SearchQuery {
    fun local(query: String, builder: QueryBuilder.() -> Unit = {}): String {
        val queryBuilder = QueryBuilderImpl().apply(builder)
        return queryBuilder.process(query)
    }

    fun remote(query: String, builder: QueryBuilder.() -> Unit = {}): String {
        return query
    }
}

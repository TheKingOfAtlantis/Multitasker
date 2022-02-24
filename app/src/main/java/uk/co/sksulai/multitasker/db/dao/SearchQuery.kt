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

private class LocalQueryBuilderImpl : QueryBuilder() {
    fun process(query: String): String {
        return "${if(anyStart) '%' else ""}$query${if(anyEnd) '%' else ""}"
    }
}
private class RemoteQueryBuilderImpl : QueryBuilder() {
    fun process(query: String): String {
        // We effectively do the same as LocalQueryBuilderImpl but ignore anyStart
        return "$query${if(anyEnd) '%' else ""}"
    }
}

object SearchQuery {
    fun local(query: String, builder: QueryBuilder.() -> Unit = {}) =
        LocalQueryBuilderImpl()
            .apply(builder)
            .process(query)

    fun remote(query: String, builder: QueryBuilder.() -> Unit = {}) =
        RemoteQueryBuilderImpl()
            .apply(builder)
            .process(query)

}

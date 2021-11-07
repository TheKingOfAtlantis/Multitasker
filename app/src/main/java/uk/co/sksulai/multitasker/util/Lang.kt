package uk.co.sksulai.multitasker.util

fun String.ifNotEmpty(operation: (String) -> String) : String =
    if(isNotEmpty()) operation(this) else ""

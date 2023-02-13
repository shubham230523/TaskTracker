package com.shubham.tasktrackerapp.util

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class LocalDateSerializer : JsonSerializer<LocalDate>{
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    override fun serialize(
        src: LocalDate?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(formatter.format(src))
    }
}

class LocalTimeSerializer: JsonSerializer<LocalTime>{
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss")
    override fun serialize(
        src: LocalTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(formatter.format(src))
    }

}

class LocalDateDeserializer : JsonDeserializer<LocalDate> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDate {
        return LocalDate.parse(
            json?.asString ,
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withLocale(Locale.ENGLISH)
        )
    }
}

class LocalTimeDeserializer : JsonDeserializer<LocalTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalTime {
        return LocalTime.parse(
            json?.asString,
            DateTimeFormatter.ofPattern("hh:mm:ss" , Locale.ENGLISH)
        )
    }
}
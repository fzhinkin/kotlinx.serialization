/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.json.kotlinx.io

import kotlinx.io.*
import kotlinx.serialization.*
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.*
import kotlinx.serialization.json.kotlinx.io.internal.JsonToOkioStreamWriter
import kotlinx.serialization.json.internal.decodeToSequenceByReader
import kotlinx.serialization.json.kotlinx.io.internal.OkioSerialReader

/**
 * Serializes the [value] with [serializer] into a [sink] using JSON format and UTF-8 encoding.
 *
 * @throws [SerializationException] if the given value cannot be serialized to JSON.
 * @throws [okio.IOException] If an I/O error occurs and sink can't be written to.
 */
@ExperimentalSerializationApi
public fun <T> Json.encodeToBufferedSink(
    serializer: SerializationStrategy<T>,
    value: T,
    sink: Sink
) {
    val writer = JsonToOkioStreamWriter(sink)
    try {
        encodeByWriter(this, writer, serializer, value)
    } finally {
        writer.release()
    }
}

/**
 * Serializes given [value] to a [sink] using UTF-8 encoding and serializer retrieved from the reified type parameter.
 *
 * @throws [SerializationException] if the given value cannot be serialized to JSON.
 * @throws [okio.IOException] If an I/O error occurs and sink can't be written to.
 */
@ExperimentalSerializationApi
public inline fun <reified T> Json.encodeToBufferedSink(
    value: T,
    sink: Sink
): Unit = encodeToBufferedSink(serializersModule.serializer(), value, sink)


/**
 * Deserializes JSON from [source] using UTF-8 encoding to a value of type [T] using [deserializer].
 *
 * Note that this functions expects that exactly one object would be present in the source
 * and throws an exception if there are any dangling bytes after an object.
 *
 * @throws [SerializationException] if the given JSON input cannot be deserialized to the value of type [T].
 * @throws [okio.IOException] If an I/O error occurs and source can't be read from.
 */
@ExperimentalSerializationApi
public fun <T> Json.decodeFromBufferedSource(
    deserializer: DeserializationStrategy<T>,
    source: Source
): T {
    return decodeByReader(this, deserializer, OkioSerialReader(source))
}

/**
 * Deserializes the contents of given [source] to the value of type [T] using UTF-8 encoding and
 * deserializer retrieved from the reified type parameter.
 *
 * Note that this functions expects that exactly one object would be present in the stream
 * and throws an exception if there are any dangling bytes after an object.
 *
 * @throws [SerializationException] if the given JSON input cannot be deserialized to the value of type [T].
 * @throws [okio.IOException] If an I/O error occurs and source can't be read from.
 */
@ExperimentalSerializationApi
public inline fun <reified T> Json.decodeFromBufferedSource(source: Source): T =
    decodeFromBufferedSource(serializersModule.serializer(), source)


/**
 * Transforms the given [source] into lazily deserialized sequence of elements of type [T] using UTF-8 encoding and [deserializer].
 * Unlike [decodeFromBufferedSource], [source] is allowed to have more than one element, separated as [format] declares.
 *
 * Elements must all be of type [T].
 * Elements are parsed lazily when resulting [Sequence] is evaluated.
 * Resulting sequence is tied to the stream and can be evaluated only once.
 *
 * **Resource caution:** this method neither closes the [source] when the parsing is finished nor provides a method to close it manually.
 * It is a caller responsibility to hold a reference to a source and close it. Moreover, because source is parsed lazily,
 * closing it before returned sequence is evaluated completely will result in [Exception] from decoder.
 *
 * @throws [SerializationException] if the given JSON input cannot be deserialized to the value of type [T].
 * @throws [okio.IOException] If an I/O error occurs and source can't be read from.
 */
@ExperimentalSerializationApi
public fun <T> Json.decodeBufferedSourceToSequence(
    source: Source,
    deserializer: DeserializationStrategy<T>,
    format: DecodeSequenceMode = DecodeSequenceMode.AUTO_DETECT
): Sequence<T> {
    return decodeToSequenceByReader(this, OkioSerialReader(source), deserializer, format)
}

/**
 * Transforms the given [source] into lazily deserialized sequence of elements of type [T] using UTF-8 encoding and deserializer retrieved from the reified type parameter.
 * Unlike [decodeFromBufferedSource], [source] is allowed to have more than one element, separated as [format] declares.
 *
 * Elements must all be of type [T].
 * Elements are parsed lazily when resulting [Sequence] is evaluated.
 * Resulting sequence is tied to the stream and constrained to be evaluated only once.
 *
 * **Resource caution:** this method does not close [source] when the parsing is finished neither provides method to close it manually.
 * It is a caller responsibility to hold a reference to a source and close it. Moreover, because source is parsed lazily,
 * closing it before returned sequence is evaluated fully would result in [Exception] from decoder.
 *
 * @throws [SerializationException] if the given JSON input cannot be deserialized to the value of type [T].
 * @throws [okio.IOException] If an I/O error occurs and source can't be read from.
 */
@ExperimentalSerializationApi
public inline fun <reified T> Json.decodeBufferedSourceToSequence(
    source: Source,
    format: DecodeSequenceMode = DecodeSequenceMode.AUTO_DETECT
): Sequence<T> = decodeBufferedSourceToSequence(source, serializersModule.serializer(), format)

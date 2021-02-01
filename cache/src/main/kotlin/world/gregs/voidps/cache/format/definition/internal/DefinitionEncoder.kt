@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package world.gregs.voidps.cache.format.definition.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.internal.TaggedEncoder
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.format.definition.Definition
import world.gregs.voidps.cache.format.definition.MetaData

internal open class DefinitionEncoder(
    private val encodeDefaults: Boolean,
    private val descriptor: SerialDescriptor
) : TaggedEncoder<Int>() {

    private val writer: Writer = BufferWriter()
    private lateinit var indexCache: Map<Int, IntArray>
    private lateinit var setterCache: Map<Int, Long>
    private lateinit var metaCache: Map<Int, MetaData>

    init {
        populateCache(descriptor)
    }

    private fun populateCache(descriptor: SerialDescriptor) {
        val elements = descriptor.elementsCount
        val indices = mutableMapOf<Int, MutableList<Int>>()
        val setters = mutableMapOf<Int, Long>()
        val types = mutableMapOf<Int, MetaData>()
        for (index in 0 until elements) {
            val code = descriptor.getOperationOrNull(index) ?: -1
            if (code != -1) {
                indices.getOrPut(code) { mutableListOf() }.add(index)
            }
            val value = descriptor.getSetterOrNull(index) ?: -1
            if (value != -1L) {
                setters[index] = value
            }
            val dataType = descriptor.getDataOrNull(index)
            if (dataType != null) {
                types[index] = dataType
            }
        }
        indexCache = indices.mapValues { (_, value) -> value.reversed().toIntArray() }
        setterCache = setters
        metaCache = types
    }

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int) = encodeDefaults

    private fun encodeOperation(index: Int, block: () -> Unit) {
        val code = descriptor.getOperation(index)
        val first = indexCache[code]?.firstOrNull() == index
        if (first) {
            writer.writeByte(code)
        }
        if (!setterCache.containsKey(index)) {
            block()
        }
    }

    private fun writeType(index: Int, type: DataType, value: Number) {
        val data = metaCache[index]
        writer.write(data?.type ?: type, value, data?.modifier ?: Modifier.NONE, data?.endian ?: Endian.BIG)
    }

    override fun encodeTaggedBoolean(tag: Int, value: Boolean) = encodeTaggedByte(tag, if (value) 1 else 0)

    override fun encodeTaggedByte(tag: Int, value: Byte) = encodeOperation(tag) { writeType(tag, DataType.BYTE, value) }

    override fun encodeTaggedShort(tag: Int, value: Short) = encodeOperation(tag) { writeType(tag, DataType.SHORT, value) }

    override fun encodeTaggedInt(tag: Int, value: Int) = encodeOperation(tag) { writeType(tag, DataType.INT, value) }

    override fun encodeTaggedLong(tag: Int, value: Long) = encodeOperation(tag) { writeType(tag, DataType.LONG, value) }

    override fun encodeTaggedString(tag: Int, value: String) = encodeOperation(tag) { writer.writeString(value) }

    internal fun toByteArray(): ByteArray = writer.toArray()

    override fun SerialDescriptor.getTag(index: Int): Int = index
}
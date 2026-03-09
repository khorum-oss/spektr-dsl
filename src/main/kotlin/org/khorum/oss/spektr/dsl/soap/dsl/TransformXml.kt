package org.khorum.oss.spektr.dsl.soap.dsl

interface TransformXml {
    var prettyPrint: Boolean
    var indent: String

    fun StringBuilder.addIndentIfPrettyPrinted() {
        if (prettyPrint) {
            append(indent)
        }
    }

    fun StringBuilder.addIndent(depth: Int): StringBuilder = append(indent.repeat(depth))

    fun StringBuilder.addTag(tag: String, attributes: String = "", block: StringBuilder.() -> Unit) {
        val space = " ".takeIf { attributes.isNotEmpty() } ?: ""

        append("<$tag$space$attributes>")
        block()
        append("</$tag>")
    }

    fun addAsXml(sb: StringBuilder, depth: Int = 0, prefix: String? = null)
}

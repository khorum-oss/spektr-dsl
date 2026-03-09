package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementHolder

/**
 * Builder for the SOAP Header section.
 *
 * Extends [SoapElementHolder] to allow adding child elements, and implements
 *
 * Example:
 * ```kotlin
 * header {
 *     element("ns:Authentication") {
 *         element("token") { content = "abc123" }
 *     }
 *     element("ns:Timestamp") { content = "2024-01-15T10:30:00Z" }
 * }
 * ```
 */
class SoapHeaderBuilder(
    override var prettyPrint: Boolean = false,
    override var indent: String = ""
) : SoapElementHolder(), TransformXml {
    /** The envelope prefix to use for the Header element tag. */
    var prefix: String? = null

    /**
     * Returns compact XML without indentation.
     *
     * @return The serialized header as a single-line XML string.
     */
    override fun toString(): String = buildString {
        addAsXml(depth = 0)
    }

    /**
     * Serializes the header to the given StringBuilder.
     *
     * @receiver The StringBuilder to append to.
     * @param depth The current nesting depth.
     */
    internal fun StringBuilder.addAsXml(depth: Int) {
        append(" ".repeat(depth))
        append("<$prefix:Header>")
        addIndentIfPrettyPrinted()
        addChildContent(this, depth + 1)
        append(indent.repeat(depth))
        append("</$prefix:Header>")
        addIndentIfPrettyPrinted()
    }

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        sb.addAsXml(depth)
    }
}

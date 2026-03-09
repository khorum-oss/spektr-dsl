package org.khorum.oss.spektr.dsl.soap.dsl.content

/**
 * Builder for list container elements that hold repeating child elements.
 *
 * Renders as a wrapper element containing zero or more child elements.
 * When empty, renders as a self-closing tag.
 *
 * Example:
 * ```kotlin
 * list("users") {
 *     element("user") { content = "Alice" }
 *     element("user") { content = "Bob" }
 *     element("user") { content = "Charlie" }
 * }
 * // Produces:
 * // <users>
 * //   <user>Alice</user>
 * //   <user>Bob</user>
 * //   <user>Charlie</user>
 * // </users>
 * ```
 *
 * @property name The list container element tag name.
 */
class SoapListBuilder(
    private val name: String,
    prettyPrint: Boolean,
    indent: String
) : SoapElementHolder(prettyPrint = prettyPrint, indent = indent), SoapChild {

    /**
     * Returns compact XML without indentation.
     *
     * @return The serialized list as a single-line XML string.
     */
    override fun toString(): String = buildString {
        addAsXml(0)
    }

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        sb.addAsXml(depth)
    }

    /**
     * Serializes this list to the given StringBuilder.
     *
     * @receiver The StringBuilder to append to.
     * @param depth The current nesting depth.
     */
    internal fun StringBuilder.addAsXml(depth: Int) {
        if (children.isEmpty()) {
            addIndent(depth)
            append("<$name/>")
            addIndentIfPrettyPrinted()
            return
        }

        addIndent(depth)
        addTag(name) {
            addIndentIfPrettyPrinted()
            addChildContent(this, depth + 1)
            addIndent(depth)
        }
        addIndentIfPrettyPrinted()
    }
}

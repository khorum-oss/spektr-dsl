package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.TransformXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXmlAttr

/**
 * Builder for individual XML elements within a SOAP message.
 *
 * Supports text content, CDATA sections, raw XML, nested elements, and attributes.
 * Elements can be marked as optional (omitted when empty) or nillable (rendered with
 * `xsi:nil="true"` when empty).
 *
 * Example:
 * ```kotlin
 * element("ns:User") {
 *     attribute("id", "123")
 *     element("name") { content = "John Doe" }
 *     element("bio") { cdata = "Likes <coding> & 'testing'" }
 *     optional("nickname") { } // Omitted - no content
 *     nillable("middleName") { } // Renders as <middleName xsi:nil="true"/>
 * }
 * ```
 *
 * @property name The element tag name (may include namespace prefix).
 * @property optional If true, element is omitted entirely when it has no content.
 * @property nillable If true, element renders as self-closing with `xsi:nil="true"` when empty.
 */
class SoapElementBuilder(
    private var name: String? = null,
    private val optional: Boolean = false,
    private val nillable: Boolean = false,
    override var prettyPrint: Boolean = false,
    override var indent: String = ""
) : SoapElementHolder(), SoapChild, TransformXml {
    /** Text content for this element. Mutually exclusive with [cData] and [rawXml]. */
    var content: Any? = null

    /**
     * Returns compact XML without indentation.
     *
     * @return The element as a single-line XML string.
     */
    override fun toString(): String = buildString {
        addContent(this, 0)
    }

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        addContent(sb, depth)
    }

    /**
     * Adds this element to the given StringBuilder.
     *
     * @param sb The StringBuilder to append to.
     * @param depth The current nesting depth.
     */
    internal fun addContent(sb: StringBuilder, depth: Int) {
        val name = name ?: return

        if (optional && !hasContent()) return

         if (nillable && !hasContent()) {
            sb.addNillableElement(name, depth)
        } else {
            sb.addStandardElement(name, depth)
        }
    }

    private fun StringBuilder.addNillableElement(name: String, depth: Int) {
        addIndent(depth)
        append("<$name xsi:nil=\"true\"/>")
        addIndentIfPrettyPrinted()
    }

    private fun StringBuilder.addStandardElement(name: String, depth: Int) {
        addIndent(depth)
        append("<$name")
        attributes.forEach { (k, v) -> append(" $k=\"${escapeXmlAttr(v)}\"") }

        if (hasContent()) {
            addContent(name, depth)
        } else {
            append("/>")
            addIndentIfPrettyPrinted()
        }
    }

    fun hasContent(): Boolean =
        content != null || cData != null || rawXml != null || children.isNotEmpty()

    private fun StringBuilder.addContent(name: String, depth: Int) {
        append(">")

        val hasChildren = children.isNotEmpty()
        val hasCdata = cData != null
        val hasRawXml = rawXml != null

        when {
            hasCdata -> cData?.addAsXml(this)
            hasRawXml -> rawXml?.addAsXml(this, depth + 1)
            hasChildren -> appendChildren(depth + 1)
            else -> appendText(content)
        }
        append("</$name>")
        addIndentIfPrettyPrinted()
    }

    private fun StringBuilder.appendChildren(depth: Int) {
        if (prettyPrint) appendLine()
        addChildContent(this, depth + 1)
        append(indent.repeat(depth))
    }

    private fun StringBuilder.appendText(text: Any?) {
        append(escapeXml(text.toString()))
    }
}

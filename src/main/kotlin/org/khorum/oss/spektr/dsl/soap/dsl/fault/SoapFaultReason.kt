package org.khorum.oss.spektr.dsl.soap.dsl.fault

import org.khorum.oss.spektr.dsl.soap.dsl.TransformXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml

/**
 * Data class for SOAP 1.2 fault reasons.
 *
 * Contains the human-readable fault description and its language tag.
 *
 * Example:
 * ```kotlin
 * reason {
 *     text = "The requested resource was not found"
 *     lang = "en"
 * }
 * // Produces: <env:Text xml:lang="en">The requested resource was not found</env:Text>
 * ```
 */
class SoapFaultReason : TransformXml {
    /** The human-readable fault description. */
    var text: String? = null

    /** The language tag for the text (default: "en" if not specified). */
    var lang: String? = null

    override var prettyPrint: Boolean = false
    override var indent: String = ""

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        sb.apply {
            addIndent(depth)
            addTag("$prefix:Reason") {
                addIndentIfPrettyPrinted()
                addIndent(depth + 1)
                addTag("$prefix:Text", "xml:lang=\"${lang ?: "en"}\"") {
                    append(escapeXml(text ?: ""))
                }
                addIndentIfPrettyPrinted()
                addIndent(depth)
            }
            addIndentIfPrettyPrinted()
        }
    }
}

package org.khorum.oss.spektr.dsl.soap.dsl.fault

import org.khorum.oss.spektr.dsl.soap.dsl.TransformXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml

/**
 * Builder for SOAP 1.2 fault codes.
 *
 * Supports hierarchical fault codes with a primary value and optional subcodes.
 * Subcodes can be nested to provide increasingly specific error information.
 *
 * Example:
 * ```kotlin
 * code {
 *     value("env:Sender")
 *     subcode("ns:ValidationError")
 *     subcode("ns:MissingField")
 * }
 * // Produces:
 * // <env:Code>
 * //   <env:Value>env:Sender</env:Value>
 * //   <env:Subcode>
 * //     <env:Value>ns:ValidationError</env:Value>
 * //     <env:Subcode>
 * //       <env:Value>ns:MissingField</env:Value>
 * //     </env:Subcode>
 * //   </env:Subcode>
 * // </env:Code>
 * ```
 */
class SoapFaultCode : TransformXml {
    private var value: String? = null
        get(): String? = field
    private val subcodes: MutableList<String> = mutableListOf()

    override var prettyPrint: Boolean = false
    override var indent: String = ""

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        val code = value
        sb.apply {
            addIndent(depth)
            addTag("$prefix:Code") {
                addIndentIfPrettyPrinted()
                addIndent(depth + 1)

                code?.let { v ->
                    addIndent(depth + 1)
                    addTag("$prefix:Value") {
                        append(escapeXml(v))
                    }
                    addIndentIfPrettyPrinted()
                }

                addSubcodes(subcodes, depth + 1, prefix)
                addIndent(depth)
            }
            addIndentIfPrettyPrinted()
        }
    }

    private fun StringBuilder.addSubcodes(
        subcodes: List<String>,
        depth: Int,
        prefix: String?
    ) {
        if (subcodes.isEmpty()) return

        addIndent(depth)
        addTag("$prefix:Subcode") {
            addIndentIfPrettyPrinted()
            addIndent(depth + 1)
            addTag("$prefix:Value") {
                append(escapeXml(subcodes.first()))
            }
            addIndentIfPrettyPrinted()
            addIndent(depth)
        }
        addIndentIfPrettyPrinted()
    }

    /**
     * Sets the primary fault code value.
     *
     * Standard values include:
     * - `env:VersionMismatch` - Invalid SOAP version
     * - `env:MustUnderstand` - Required header not understood
     * - `env:DataEncodingUnknown` - Unsupported encoding
     * - `env:Sender` - Client-side error
     * - `env:Receiver` - Server-side error
     *
     * @param code The fault code value.
     */
    fun value(code: String) { value = code }

    /**
     * Sets the primary fault code value with a namespace prefix.
     *
     * @param namespace The namespace prefix.
     * @param code The local code name.
     */
    fun value(namespace: String, code: String) = value("$namespace:$code")

    /**
     * Adds a subcode to the fault code hierarchy.
     *
     * Subcodes are nested in order of addition, providing increasingly
     * specific error categorization.
     *
     * @param code The subcode value.
     */
    fun subcode(code: String) { subcodes.add(code) }

    /**
     * Adds a subcode with a namespace prefix.
     *
     * @param namespace The namespace prefix.
     * @param code The local code name.
     */
    fun subcode(namespace: String, code: String) = subcode("$namespace:$code")
}

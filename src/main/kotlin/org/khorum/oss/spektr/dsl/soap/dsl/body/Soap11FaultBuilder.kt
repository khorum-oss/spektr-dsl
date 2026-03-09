package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml

/**
 * SOAP 1.1 fault builder.
 *
 * Builds fault elements according to the SOAP 1.1 specification using
 * unprefixed child elements: `faultcode`, `faultstring`, `faultactor`, and `detail`.
 *
 * Example:
 * ```kotlin
 * fault {
 *     faultCode("soap:Server")
 *     faultString("Internal server error")
 *     faultActor("http://example.com/service")
 *     detail {
 *         element("errorCode") { content = "ERR_500" }
 *     }
 * }
 * ```
 */
class Soap11FaultBuilder(
    prettyPrint: Boolean,
    indent: String
) : SoapFaultBuilder(prettyPrint, indent) {
    private var faultCode: String? = null
    private var faultString: String? = null
    private var faultActor: String? = null

    /**
     * Sets the fault code.
     *
     * Common values include `soap:Server` for server errors and `soap:Client` for client errors.
     *
     * @param code The fault code value.
     */
    override fun faultCode(code: String) { faultCode = code }

    /**
     * Sets the human-readable fault description.
     *
     * @param reason The fault message.
     */
    override fun faultString(reason: String) { faultString = reason }

    /**
     * Sets the URI of the actor that caused the fault.
     *
     * @param actor The actor URI.
     */
    override fun faultActor(actor: String) { faultActor = actor }

    override fun addFaultContent(sb: StringBuilder, prefix: String, depth: Int) {
        faultCode?.also { sb.addFaultTag("faultcode", it, depth) }
        faultString?.also { sb.addFaultTag("faultstring", it, depth) }
        faultActor?.also { sb.addFaultTag("faultactor", it, depth) }
        sb.addDetailElementIfPresent(prefix, usePrefixedDetail = false, depth)
    }

    private fun StringBuilder.addFaultTag(tag: String, content: String, depth: Int) {
        addIndent(depth)
        addTag(tag) {
            append(escapeXml(content))
        }
        addIndentIfPrettyPrinted()
    }
}

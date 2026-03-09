package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapBodyBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapBodyContent
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapFaultBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultScope

/**
 * Builder for constructing SOAP envelopes.
 *
 * This is the root builder class for the SOAP DSL, providing methods to configure the envelope's
 * version, namespaces, header, and body sections.
 *
 * Example:
 * ```kotlin
 * val envelope = SoapEnvelopeBuilder().apply {
 *     version = SoapVersion.V1_2
 *     namespaces { ns("xmlns:ns" to "http://example.com") }
 *     body { element("ns:Response") { content = "OK" } }
 * }
 * println(envelope.toPrettyString())
 * ```
 */
class SoapEnvelopeBuilder(
        override var prettyPrint: Boolean = false,
        override var indent: String = ""
) : TransformXml {
    /** The SOAP version to use (default: SOAP 1.2). */
    var version: SoapVersion = SoapVersion.V1_2

    /** The XML prefix for SOAP envelope elements (default: "soapenv"). */
    var envelopePrefix: String = "soapenv"

    /** Optional custom schema location to override the default namespace URI. */
    var schemasLocation: String? = null

    private var namespaces: SoapNamespacesBuilder? = null
    private var header: SoapHeaderBuilder? = null
    private var body: SoapBodyContent? = null

    /**
     * Configures custom XML namespaces for the envelope.
     *
     * @param block Configuration block for defining namespace prefixes and URIs.
     */
    @SoapDslMarker
    fun namespaces(block: SoapNamespacesBuilder.() -> Unit) {
        namespaces = SoapNamespacesBuilder().apply(block)
    }

    /**
     * Configures the SOAP header section.
     *
     * @param block Configuration block for adding header elements.
     */
    @SoapDslMarker
    fun header(block: SoapHeaderBuilder.() -> Unit) {
        header = SoapHeaderBuilder(prettyPrint).apply(block)
    }

    /**
     * Configures the SOAP body section with elements.
     *
     * Cannot be called if [fault] has already been called on this envelope.
     *
     * @param block Configuration block for adding body elements.
     * @throws IllegalStateException if body has already been set.
     */
    @SoapDslMarker
    fun body(block: SoapBodyBuilder.() -> Unit) {
        checkBodyNotSet()
        body = SoapBodyBuilder(version, prettyPrint).apply(block)
    }

    /**
     * Configures the SOAP body as a fault response.
     *
     * Cannot be called if [body] has already been called on this envelope. The fault structure
     * depends on the configured [version].
     *
     * @param block Configuration block for defining the fault details.
     * @throws IllegalStateException if body has already been set.
     */
    @SoapDslMarker
    fun fault(block: SoapFaultScope.() -> Unit) {
        checkBodyNotSet()
        body = version.faultBuilder(prettyPrint, indent).apply(block)
    }

    private fun checkBodyNotSet() {
        check(body == null) { "Body already set" }
    }

    /**
     * Returns compact XML without indentation or newlines.
     *
     * @return The serialized SOAP envelope as a single-line XML string.
     */
    override fun toString(): String = buildString { addAsXml() }

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        sb.addAsXml()
    }

    private fun StringBuilder.addAsXml() {
        val soapNs = schemasLocation ?: SOAP_NAMESPACES[version]

        append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        addIndentIfPrettyPrinted()

        addIndent(DEFAULT_SERIALIZED_DEPTH)

        val namespaceAttributes =
                namespaces
                        ?.getNamespaces()
                        ?.map { (attr, uri) -> "$attr=\"$uri\"" }
                        ?.joinToString(" ")
        val spacing = " ".takeIf { namespaceAttributes != null } ?: ""
        val attributes = """xmlns:$envelopePrefix="$soapNs"$spacing${namespaceAttributes ?: ""}"""

        addTag("$envelopePrefix:Envelope", attributes) {
            addIndentIfPrettyPrinted()

            header?.prefix = envelopePrefix
            header?.addAsXml(this, DEFAULT_SERIALIZED_DEPTH + 1)

            serializeBody()

            addIndent(DEFAULT_SERIALIZED_DEPTH)
        }
        addIndentIfPrettyPrinted()
    }

    private fun StringBuilder.serializeBody() {
        addIndent(DEFAULT_BODY_DEPTH)
        addTag("$envelopePrefix:Body") {
            addIndentIfPrettyPrinted()

            val b = body

            if (b is SoapBodyBuilder) {
                b.addChildContent(this, DEFAULT_BODY_DEPTH + 1)
                b.getFault()?.addAsXml(this, DEFAULT_BODY_DEPTH + 1, envelopePrefix)
            } else if (b is SoapFaultBuilder) {
                b.addAsXml(this, DEFAULT_BODY_DEPTH + 1, envelopePrefix)
            }

            addIndent(DEFAULT_BODY_DEPTH)
        }
        addIndentIfPrettyPrinted()
    }

    companion object {
        private const val DEFAULT_SERIALIZED_DEPTH = 0
        private const val DEFAULT_BODY_DEPTH = 1

        private val SOAP_NAMESPACES =
                mapOf(
                        SoapVersion.V1_1 to "http://schemas.xmlsoap.org/soap/envelope/",
                        SoapVersion.V1_2 to "http://www.w3.org/2003/05/soap-envelope"
                )
    }
}

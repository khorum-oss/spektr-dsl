package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.SoapDslMarker
import org.khorum.oss.spektr.dsl.soap.dsl.SoapVersion
import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementHolder
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultScope

/**
 * Builder for the SOAP Body section.
 *
 * Extends [SoapElementHolder] to allow adding child elements. Can optionally include a fault
 * element for error responses.
 *
 * Example:
 * ```kotlin
 * body {
 *     element("ns:GetUserResponse") {
 *         element("user") {
 *             element("name") { content = "John Doe" }
 *             element("email") { content = "john@example.com" }
 *         }
 *     }
 * }
 * ```
 *
 * @property version The SOAP version, used to create the appropriate fault builder.
 */
class SoapBodyBuilder(private val version: SoapVersion, prettyPrint: Boolean = false) :
        SoapBodyContent, SoapElementHolder(prettyPrint = prettyPrint) {
    private var fault: SoapFaultBuilder? = null
        get(): SoapFaultBuilder? = field

    /**
     * Adds a fault element to this body.
     *
     * The fault structure depends on the SOAP version configured on the envelope.
     *
     * @param block Configuration block for defining the fault details.
     */
    @SoapDslMarker
    fun fault(block: SoapFaultScope.() -> Unit) {
        fault = version.faultBuilder(prettyPrint, indent).apply(block)
    }

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        addChildContent(sb, depth)
        fault?.addAsXml(sb, depth, prefix)
    }
}

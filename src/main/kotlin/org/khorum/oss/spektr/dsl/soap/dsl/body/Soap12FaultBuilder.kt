package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultCode
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultReason

/**
 * SOAP 1.2 fault builder.
 *
 * Builds fault elements according to the SOAP 1.2 specification using
 * prefixed child elements: `Code`, `Reason`, `Node`, `Role`, and `Detail`.
 *
 * Example:
 * ```kotlin
 * fault {
 *     code {
 *         value("env:Sender")
 *         subcode("ns:InvalidInput")
 *     }
 *     reason {
 *         text = "Invalid request parameters"
 *         lang = "en"
 *     }
 *     node("http://example.com/service")
 *     role("http://example.com/role")
 *     detail {
 *         element("ns:errorCode") { content = "ERR_400" }
 *     }
 * }
 * ```
 */
class Soap12FaultBuilder(
    prettyPrint: Boolean,
    indent: String
) : SoapFaultBuilder(prettyPrint, indent) {
    private var code: SoapFaultCode? = null
    private var reason: SoapFaultReason? = null
    private var node: FaultNode? = null
    private var role: FaultRole? = null

    /**
     * Sets the fault code using a simple string value.
     *
     * Creates a [SoapFaultCode] with just the value, no subcodes.
     *
     * @param value The fault code value (e.g., "env:Sender", "env:Receiver").
     */
    override fun code(value: String) {
        code = SoapFaultCode().apply { value(value) }
    }

    /**
     * Configures the fault code with subcodes.
     *
     * @param block Configuration block for the fault code.
     */
    override fun code(block: SoapFaultCode.() -> Unit) {
        code = SoapFaultCode().apply(block)
    }

    /**
     * Configures the fault reason.
     *
     * @param block Configuration block for the reason text and language.
     */
    override fun reason(block: SoapFaultReason.() -> Unit) {
        reason = SoapFaultReason().apply(block)
    }

    /**
     * Sets the URI of the SOAP node that generated the fault.
     *
     * @param node The node URI.
     */
    override fun node(node: String) {
        this.node = FaultNode(node)
    }

    /**
     * Sets the URI of the role the node was operating in when the fault occurred.
     *
     * @param role The role URI.
     */
    override fun role(role: String) {
        this.role = FaultRole(role)
    }

    override fun addFaultContent(sb: StringBuilder, prefix: String, depth: Int) {
        code?.addAsXml(sb, depth, prefix)
        reason?.addAsXml(sb, depth, prefix)
        node?.addAsXml(sb, depth, prefix)
        role?.addAsXml(sb, depth, prefix)

        sb.addDetailElementIfPresent(prefix, usePrefixedDetail = true, depth)
    }

}

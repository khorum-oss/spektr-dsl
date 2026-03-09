package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.TransformXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml

class FaultNode(val value: String) : TransformXml {
    override var prettyPrint: Boolean = false
    override var indent: String = ""

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        sb.addIndent(depth)
        sb.addTag("$prefix:Node") {
            append(escapeXml(value))
        }
        sb.addIndentIfPrettyPrinted()
    }
}

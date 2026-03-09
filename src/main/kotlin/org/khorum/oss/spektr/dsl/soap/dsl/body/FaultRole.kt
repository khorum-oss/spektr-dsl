package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.TransformXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml

class FaultRole(val value: String) : TransformXml {
    override var prettyPrint: Boolean = false
    override var indent: String = ""

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        sb.addIndent(depth)
        sb.addTag("$prefix:Role") {
            append(escapeXml(value))
        }
        sb.addIndentIfPrettyPrinted()
    }
}

fun FaultRole?.addIfAvailable(sb: StringBuilder, depth: Int, prefix: String?) {
    this?.addAsXml(sb, depth, prefix)
}

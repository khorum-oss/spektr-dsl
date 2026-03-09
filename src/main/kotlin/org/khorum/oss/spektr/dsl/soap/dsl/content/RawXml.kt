package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.TransformXml

class RawXml(val value: String) : TransformXml {
    override var prettyPrint: Boolean = false
    override var indent: String = ""

    override fun addAsXml(sb: StringBuilder, depth: Int, prefix: String?) {
        sb.apply {
            addIndentIfPrettyPrinted()
            append(value)
            addIndentIfPrettyPrinted()
            addIndent(depth)
        }
    }
}

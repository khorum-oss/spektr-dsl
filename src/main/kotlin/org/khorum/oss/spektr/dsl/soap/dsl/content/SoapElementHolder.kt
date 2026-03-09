package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.TransformXml
import org.khorum.oss.spektr.dsl.soap.dsl.SoapDslMarker

/**
 * Abstract base class for SOAP elements that can contain child elements.
 *
 * Provides common functionality for managing attributes, child elements, CDATA sections,
 * and raw XML content. This class is extended by [SoapElementBuilder], [SoapListBuilder],
 * [SoapHeaderBuilder][org.khorum.oss.spektr.dsl.soap.dsl.SoapHeaderBuilder], and
 * [SoapBodyBuilder][org.khorum.oss.spektr.dsl.soap.dsl.body.SoapBodyBuilder].
 */
@Suppress("TooManyFunctions")
abstract class SoapElementHolder(
    override var prettyPrint: Boolean = false,
    override var indent: String = ""
) : TransformXml {
    /** Attributes to be rendered on this element. */
    protected val attributes: MutableMap<String, String> = mutableMapOf()

    /** Ordered list of child elements and lists. */
    protected val children: MutableList<SoapChild> = mutableListOf()

    /** CDATA content to wrap in `<![CDATA[...]]>`. Mutually exclusive with [rawXml] and text content. */
    var cData: CData? = null

    /** Raw XML string to include without escaping. Mutually exclusive with [cData] and text content. */
    var rawXml: RawXml? = null

    fun cData(data: String) {
        cData = CData(data)
    }

    fun rawXml(data: String) {
        rawXml = RawXml(data)
    }

    /**
     * Serializes all child elements in insertion order.
     *
     * @param sb The StringBuilder to append to.
     * @param depth The current nesting depth.
     */
    internal fun addChildContent(sb: StringBuilder, depth: Int) {
        for (child in children) {
            when (child) {
                is SoapElementBuilder -> child.addContent(sb, depth)
                is SoapListBuilder -> child.addAsXml(sb, depth)
            }
        }
    }

    /**
     * Adds a child element with the given name.
     *
     * @param name The element tag name (may include namespace prefix).
     * @param block Configuration block for the element.
     */
    @SoapDslMarker
    fun element(name: String, block: SoapElementBuilder.() -> Unit) {
        children.add(SoapElementBuilder(name, prettyPrint = prettyPrint).apply(block))
    }

    /**
     * Adds a child element with a namespace prefix.
     *
     * @param namespace The namespace prefix.
     * @param name The element local name.
     * @param block Configuration block for the element.
     */
    @SoapDslMarker
    fun element(
        namespace: String,
        name: String,
        block: SoapElementBuilder.() -> Unit
    ) = element("$namespace:$name", block)

    /**
     * Adds a single attribute to this element.
     *
     * @param name The attribute name (may include namespace prefix).
     * @param value The attribute value (will be converted to string).
     */
    fun attribute(name: String, value: Any) {
        attributes[name] = value.toString()
    }

    /**
     * Adds a namespaced attribute to this element.
     *
     * @param namespace The namespace prefix.
     * @param name The attribute local name.
     * @param value The attribute value.
     */
    fun attribute(namespace: String, name: String, value: String) = attribute(
        "$namespace:$name", value
    )

    /**
     * Adds multiple attributes to this element.
     *
     * @param pairs Vararg of name-value pairs.
     */
    fun attributes(vararg pairs: Pair<String, String>) {
        attributes.putAll(pairs)
    }

    /**
     * Adds an optional child element that is omitted if it has no content.
     *
     * @param name The element tag name.
     * @param block Configuration block for the element.
     */
    @SoapDslMarker
    fun optional(name: String, block: SoapElementBuilder.() -> Unit) {
        children.add(SoapElementBuilder(name, optional = true, prettyPrint = prettyPrint).apply(block))
    }

    /**
     * Adds an optional namespaced child element.
     *
     * @param namespace The namespace prefix.
     * @param name The element local name.
     * @param block Configuration block for the element.
     */
    @SoapDslMarker
    fun optional(namespace: String, name: String, block: SoapElementBuilder.() -> Unit) =
        optional("$namespace:$name", block)

    /**
     * Adds a nillable child element that renders as `xsi:nil="true"` when empty.
     *
     * @param name The element tag name.
     * @param block Configuration block for the element.
     */
    @SoapDslMarker
    fun nillable(name: String, block: SoapElementBuilder.() -> Unit) {
        children.add(SoapElementBuilder(name, nillable = true, prettyPrint = prettyPrint).apply(block))
    }

    /**
     * Adds a nillable namespaced child element.
     *
     * @param namespace The namespace prefix.
     * @param name The element local name.
     * @param block Configuration block for the element.
     */
    @SoapDslMarker
    fun nillable(namespace: String, name: String, block: SoapElementBuilder.() -> Unit) =
        nillable("$namespace:$name", block)

    /**
     * Adds a list container element for repeating child elements.
     *
     * @param name The list element tag name.
     * @param block Configuration block for adding list items.
     */
    @SoapDslMarker
    fun list(name: String, block: SoapListBuilder.() -> Unit) {
        children.add(SoapListBuilder(name, prettyPrint = prettyPrint, indent = indent).apply(block))
    }

    /**
     * Adds a namespaced list container element.
     *
     * @param namespace The namespace prefix.
     * @param name The list element local name.
     * @param block Configuration block for adding list items.
     */
    @SoapDslMarker
    fun list(namespace: String, name: String, block: SoapListBuilder.() -> Unit) {
        children.add(SoapListBuilder("$namespace:$name", prettyPrint = prettyPrint, indent = indent).apply(block))
    }
}

/*
 * Copyright (c) 2010-2011 TIBCO Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.genxdm.axes;

import org.genxdm.nodes.Informer;

/**
 * Provides the various "axes" by which nodes can be traversed, as defined by
 * XPath 2.0.
 * 
 * @see http://www.w3.org/TR/xpath20/#axes
 */
public interface AxisNavigator
{
    /**
     * Returns the nodes along the ancestor axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getAncestorAxis();

    /**
     * Returns the nodes along the ancestor-or-self axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getAncestorOrSelfAxis();

    /**
     * Returns the nodes along the attribute axis using the specified node as the origin.
     * 
     * <br/>
     * Corresponds to the <a href="http://www.w3.org/TR/xpath-datamodel/#acc-summ-attributes">
     * dm:attributes</a> accessor in the XDM.
     * 
     * @param inherit
     *            Determines whether attributes in the XML namespace will be inherited. The standard value for this
     *            parameter is <code>false</code>.
     * 
     * @see http://www.w3.org/TR/xpath-datamodel/#acc-summ-attributes
     */
    Iterable<Informer> getAttributeAxis(boolean inherit);

    /**
     * Returns the nodes along the child axis using the specified node as the origin.
     * 
     * <br/>
     * 
     * Corresponds to the <a href="http://www.w3.org/TR/xpath-datamodel/#acc-summ-children">
     * dm:children</a> accessor in the XDM.
     * 
     * 
     * @see http://www.w3.org/TR/xpath-datamodel/#acc-summ-children
     */
    Iterable<Informer> getChildAxis();

    /**
     * Returns all the child element along the child axis.
     * 
     */
    Iterable<Informer> getChildElements();

    /**
     * Returns all the child element along the child axis whose names match the arguments supplied.
     * 
     * @param namespaceURI
     *            The namespace-uri to be matched.
     * @param localName
     *            The local-name to be matched.
     */
    Iterable<Informer> getChildElementsByName(String namespaceURI, String localName);

    /**
     * Returns the nodes along the descendant axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getDescendantAxis();

    /**
     * Returns the nodes along the descendant-or-self axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getDescendantOrSelfAxis();

    /**
     * Returns the nodes along the following axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getFollowingAxis();

    /**
     * Returns the nodes along the following-sibling axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getFollowingSiblingAxis();

    /**
     * Returns the nodes along the namespace axis using the specified node as the origin.
     * 
     * <p>
     * The namespace axis contains the namespace nodes of the context node; the axis will be empty unless the context
     * node is an element.
     * </p>
     * 
     * <p>Corresponds to the <a href="http://www.w3.org/TR/xpath-datamodel/#acc-summ-namespace-nodes">
     * dm:namespace-nodes</a> of XDM.</p>
     * 
     * @param inherit
     *            Determines whether in-scope prefix mappings will be included in the result. The standard setting for
     *            this parameter is <code>true</code>.
     * 
     * @see http://www.w3.org/TR/xpath-datamodel/#acc-summ-namespace-nodes
     */
    Iterable<Informer> getNamespaceAxis(boolean inherit);

    /**
     * Returns the nodes along the preceding axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getPrecedingAxis();

    /**
     * Returns the nodes along the preceding-sibling axis using the specified node as the origin.
     * 
     */
    Iterable<Informer> getPrecedingSiblingAxis();

}

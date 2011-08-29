/*
 * Copyright (c) 2010 TIBCO Software Inc.
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
package org.genxdm.nodes;

import javax.xml.namespace.QName;

/** Additional methods, extending NodeInformer, for querying type
 * annotations and typed values in a tree.
 * 
 * @param <N> the Node handle
 * @param <A> the Atom handle
 */
public interface TypedNodeInformer<N, A>
{

    /**
     * Returns the type name of the attribute node with the specified expanded-QName.
     * This is equivalent to retrieving the attribute node and then its type name.
     * 
     * @param parent
     *            The node that is the parent of the attribute node. Must not
     *            be null.
     * @param namespaceURI
     *            The namespace-uri part of the attribute name.  Must not be null,
     *            but may be the empty string (and typically is).
     * @param localName
     *            The local-name part of the attribute name.  Must not be null.
     * @return the QName representing the type name for the designated attribute,
     * if that attribute exists in this location.  Null if the attribute does not
     * exist.  untyped-atomic if the attribute exists, but has no type annotation.
     */
    QName getAttributeTypeName(N parent, String namespaceURI, String localName);

    /**
     * Returns the dm:typed-value of the attribute node with the specified expanded-QName.
     * This is equivalent to retrieving the attribute node and then its typed value.
     * 
     * @param parent
     *            The node that is the parent of the attribute node.
     *            Must not be null.
     * @param namespaceURI
     *            The namespace-uri part of the attribute name.  Must not be null,
     *            but may be the empty string (and typically is).
     * @param localName
     *            The local-name part of the attribute name.
     *            Must not be null.
     * @return a sequence of atoms, representing the typed value of the designated
     * attribute, if that attribute exists in this location.  Null if the attribute
     * does not exist.  untyped-atomic if the attribute exists, but is not validated.
     */
    Iterable<? extends A> getAttributeValue(N parent, String namespaceURI, String localName);

    /**
     * Gets the type name of an element or attribute node.
     * Returns <code>null</code> for all other node kinds.
     * Corresponds to the <a href="http://www.w3.org/TR/xpath-datamodel/#acc-summ-type-name">
     * dm:type-name</a> accessor in the XDM.
     * 
     * @param node
     *            The node for which the type name is required.  If null is
     *            supplied, null is returned.
     *            
     * @return the type name, if the context node is an element or attribute;
     * otherwise null.  If the context node is an element or attribute, but is
     * not validated, returns an xs:untyped or xs:untyped-atomic.
     * @see http://www.w3.org/TR/xpath-datamodel/#acc-summ-type-name
     */
    QName getTypeName(N node);

    /**
     * Returns the <a href="http://www.w3.org/TR/xpath-datamodel/#acc-summ-typed-value">
     * dm:typed-value</a> property of the node.
     * 
     * Applies to all node kinds.
     * 
     * If the node argument is <code>null</code>, then <code>null</code> is returned.
     * 
     * @param node
     *            The node for which dm:typed-value is required.  If null is supplied,
     *            null is returned.
     * 
     * @return a sequence of atoms representing the typed-value of the supplied
     * node; null if null has been supplied.  The typed-value of a node may be
     * rather complex (for documents and elements particularly), or very simple.
     * returns an xs:untyped-atomic (or string) if the node is not validated (or
     * for comment and processing instruction nodes).
     * @see http://www.w3.org/TR/xpath-datamodel/#acc-summ-typed-value
     */
    Iterable<? extends A> getValue(N node);

}

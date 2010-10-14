/**
 * Copyright (c) 2009-2010 TIBCO Software Inc.
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
package org.gxml.processor.w3c.xs.validation.impl;

import javax.xml.namespace.QName;

import org.genxdm.xs.components.SmElement;
import org.genxdm.xs.enums.SmProcessContentsMode;
import org.genxdm.xs.exceptions.SmAbortException;
import org.genxdm.xs.exceptions.SmException;
import org.genxdm.xs.exceptions.SmExceptionHandler;
import org.genxdm.xs.types.SmComplexType;
import org.genxdm.xs.types.SmSimpleType;
import org.genxdm.xs.types.SmType;
import org.gxml.processor.w3c.xs.exception.CvcNoRootElementException;
import org.gxml.processor.w3c.xs.exception.SrcFrozenLocation;
import org.gxml.processor.w3c.xs.validation.api.VxMetaBridge;
import org.gxml.processor.w3c.xs.validation.api.VxPSVI;


final class ModelAnalyzerImpl<A> implements ModelAnalyzer<A>
{
	private ModelPSVI<A> m_currentPSVI;
	private final ModelPSVI<A> m_documentPSVI;
	private SmExceptionHandler m_errors = SmExceptionThrower.SINGLETON;

	// Document state flag. Have we received the document element?
	private boolean m_rootStartDone;

	public ModelAnalyzerImpl(final VxMetaBridge<A> metaBridge, final ValidationCache<A> cache)
	{
		this.m_currentPSVI = this.m_documentPSVI = new ModelPSVI<A>(SmProcessContentsMode.Strict, metaBridge, cache);
	}

	public void attribute(final QName name, final SmSimpleType<A> type, final A value)
	{
		// TODO: shall we keep track of which attributes are passed to us between startElement calls?
	}

	public void endDocument() throws SmAbortException
	{
		if (!m_rootStartDone)
		{
			m_errors.error(new CvcNoRootElementException(new SrcFrozenLocation(-1, -1, -1, null, null)));
		}
	}

	public VxPSVI<A> endElement() throws SmAbortException
	{
		final ModelPSVI<A> elementItem = m_currentPSVI;
		try
		{
			m_currentPSVI.checkForUnexpectedEndOfContent(m_errors);
		}
		finally
		{
			m_currentPSVI = m_currentPSVI.pop();
		}
		return elementItem;
	}

	public void setExceptionHandler(SmExceptionHandler handler)
	{
		m_errors = PreCondition.assertArgumentNotNull(handler, "handler");
	}

	public void startDocument()
	{
		m_currentPSVI = m_documentPSVI;

		// So we know that a root object was found.
		m_rootStartDone = false;
	}

	public ModelPSVI<A> startElement(final QName elementName, final SmType<A> localType, final Boolean explicitNil) throws SmAbortException
	{
		final ModelPSVI<A> parentItem = m_currentPSVI;
		m_currentPSVI = parentItem.push(elementName);

		// Figure out the appropriate decl and type for this element.
		if (!m_rootStartDone)
		{
			try
			{
				m_currentPSVI.recoverPSVI(localType, m_errors);
			}
			catch (final SmException e)
			{
				m_errors.error(e);
			}

			m_rootStartDone = true;
		}
		else
		{
			final SmElement<A> declaration = parentItem.getDeclaration();
			if (null != declaration)
			{
				ValidationRules.checkValueConstraintAllowsElementChild(declaration, m_currentPSVI.getName(), m_currentPSVI, m_errors);
			}

			// Validate the child element in the context of the parent and make annotation.
			if (parentItem.step(elementName, m_currentPSVI, m_errors))
			{
				try
				{
					// This assignment includes parts of the Validation Rule: Element Locally Valid (Element).
					ModelPSVI.assignPSVI(m_currentPSVI, localType, m_errors);
				}
				catch (final SmException e)
				{
					m_errors.error(e);
				}
			}
			else
			{
				// We got no matches, possibly because we are a descendant under a wildcard.
				if (!parentItem.declExists())
				{
					try
					{
						m_currentPSVI.recoverPSVI(localType, m_errors);
					}
					catch (final SmException e)
					{
						m_errors.error(e);
					}
				}
			}
		}

		// More of the Validation Rule: Element Locally Valid (Element).
		m_currentPSVI.setNilled(m_currentPSVI.computeNilled(explicitNil, m_errors));

		final SmType<A> elementType = m_currentPSVI.getType();
		if (elementType instanceof SmComplexType<?>)
		{
			ValidationRules.checkComplexTypeNotAbstract((SmComplexType<A>)elementType, elementName, m_errors);
		}

		return m_currentPSVI;
	}
}

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
package org.genxdm.bridgekit.xs;

import org.genxdm.NodeKind;
import org.genxdm.xs.types.NamespaceNodeType;
import org.genxdm.xs.types.PrimeChoiceType;
import org.genxdm.xs.types.PrimeType;
import org.genxdm.xs.types.PrimeTypeKind;
import org.genxdm.xs.types.SequenceTypeVisitor;

final class NamespaceNodeTypeImpl<A> extends AbstractLeafNodeType<A> implements NamespaceNodeType<A>
{
	NamespaceNodeTypeImpl(final SchemaCacheImpl<A> cache)
	{
		super(NodeKind.NAMESPACE, cache);
	}

	public void accept(final SequenceTypeVisitor<A> visitor)
	{
		visitor.visit(this);
	}

	public PrimeTypeKind getKind()
	{
		return PrimeTypeKind.NAMESPACE;
	}

	public NamespaceNodeType<A> prime()
	{
		return this;
	}

	public boolean subtype(final PrimeType<A> rhs)
	{
		switch (rhs.getKind())
		{
			case CHOICE:
			{
				final PrimeChoiceType<A> choiceType = (PrimeChoiceType<A>)rhs;
				return subtype(choiceType.getLHS()) || subtype(choiceType.getRHS());
			}
			case NAMESPACE:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
}

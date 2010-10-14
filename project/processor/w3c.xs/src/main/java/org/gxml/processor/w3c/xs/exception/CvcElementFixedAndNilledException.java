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
package org.gxml.processor.w3c.xs.exception;

import org.genxdm.xs.components.SmElement;
import org.genxdm.xs.resolve.SmLocation;

@SuppressWarnings("serial")
public final class CvcElementFixedAndNilledException extends CvcElementException
{
	public CvcElementFixedAndNilledException(final SmElement<?> decl, final SmLocation location)
	{
		super(PART_FIXED_AND_NILLED, decl, location);
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof CvcElementFixedAndNilledException)
		{
			final CvcElementFixedAndNilledException e = (CvcElementFixedAndNilledException)obj;
			return getElementDeclaration().equals(e.getElementDeclaration());
		}
		else
		{
			return false;
		}
	}

	@Override
	public String getMessage()
	{
		return "The element information item '" + getElementDeclaration() + "' cannot be nilled because it has a fixed {value constraint}.";
	}
}

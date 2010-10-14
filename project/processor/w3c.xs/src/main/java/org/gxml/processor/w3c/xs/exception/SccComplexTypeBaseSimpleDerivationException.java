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

import java.util.Set;

import org.genxdm.xs.enums.SmDerivationMethod;
import org.genxdm.xs.exceptions.SmComponentConstraintException;
import org.genxdm.xs.types.SmType;


@SuppressWarnings("serial")
public final class SccComplexTypeBaseSimpleDerivationException extends SccTypeDerivationOKComplexException
{
	public SccComplexTypeBaseSimpleDerivationException(final SmType<?> typeName, final SmType<?> baseName, final Set<SmDerivationMethod> subset, final SmComponentConstraintException cause)
	{
		super(PART_BASE_SIMPLE, typeName, baseName, subset, cause);
	}

	@Override
	public String getMessage()
	{
		return "The {type definition} of " + getDerivedType().getName() + " must be validly derived from " + getBaseName() + ".";
	}
}

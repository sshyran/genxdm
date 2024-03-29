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
package org.genxdm.bridgekit.atoms;

import org.genxdm.xs.types.NativeType;

/**
 * Corresponds to the W3C XML Schema <a href="http://www.w3.org/TR/xmlschema-2/#int">int</a>.
 */
public final class XmlInt extends XmlAbstractAtom
{
    private final int intValue;

    public XmlInt(final int intValue)
    {
        this.intValue = intValue;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof XmlInt)
        {
            return intValue == ((XmlInt)obj).intValue;
        }
        else
        {
            return false;
        }
    }

    public String getC14NForm()
    {
        return Integer.toString(intValue);
    }

    public NativeType getNativeType()
    {
        return NativeType.INT;
    }

    @Override
    public int hashCode()
    {
        return intValue;
    }

    /**
     * Returns this value as an <code>int</code>.
     */
    public int getIntValue()
    {
        return intValue;
    }

    public boolean isWhiteSpace()
    {
        return false;
    }
}

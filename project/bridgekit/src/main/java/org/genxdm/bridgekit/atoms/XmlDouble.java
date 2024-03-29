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
 * Corresponds to the W3C XML Schema <a href="http://www.w3.org/TR/xmlschema-2/#double">double</a>.
 */
public final class XmlDouble extends XmlAbstractAtom
{
    private final double doubleValue;

    public XmlDouble(final double doubleValue)
    {
        this.doubleValue = doubleValue;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof XmlDouble)
        {
            return doubleValue == ((XmlDouble)obj).doubleValue;
        }
        else
        {
            return false;
        }
    }

    public String getC14NForm()
    {
        return NumericSupport.formatDoubleC14N(doubleValue);
    }

    /**
     * Returns this value as a <code>double</code>.
     */
    public double getDoubleValue()
    {
        return doubleValue;
    }

    public NativeType getNativeType()
    {
        return NativeType.DOUBLE;
    }

    @Override
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(doubleValue);
        return (int)(bits ^ (bits >>> 32));
    }

    public boolean isWhiteSpace()
    {
        return false;
    }
}

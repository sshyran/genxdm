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
 * Corresponds to the W3C XML Schema <a href="http://www.w3.org/TR/xmlschema-2/#NCName">NCName</a>.
 */
public final class XmlNCName extends XmlAbstractAtom
{
    private final String stringValue;
    private final int hashCode;

    /**
     * Intentionally package protected because this must only be created through the name bridge.
     * 
     * @param stringValue
     */
    public XmlNCName(final String stringValue)
    {
        this.stringValue = stringValue;
        // The caching of the hashCode has the side effect of detecting a null stringValue early.
        this.hashCode = stringValue.hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof XmlNCName)
        {
            return stringValue.equals(((XmlNCName)obj).stringValue);
        }
        else
        {
            return false;
        }
    }

    public String getC14NForm()
    {
        return stringValue;
    }

    public NativeType getNativeType()
    {
        return NativeType.NCNAME;
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    public boolean isWhiteSpace()
    {
        return false;
    }
}

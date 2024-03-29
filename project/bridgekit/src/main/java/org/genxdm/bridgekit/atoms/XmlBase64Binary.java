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

import java.util.Arrays;

import org.genxdm.exceptions.PreCondition;
import org.genxdm.xs.types.NativeType;

/**
 * Corresponds to the W3C XML Schema <a href="http://www.w3.org/TR/xmlschema-2/#base64Binary">base64Binary</a>.
 */
public final class XmlBase64Binary extends XmlAbstractAtom
{
    private final byte[] bytes;

    public XmlBase64Binary(final byte[] bytes)
    {
        this.bytes = PreCondition.assertArgumentNotNull(bytes, "bytes");
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof XmlBase64Binary)
        {
            return Arrays.equals(bytes, ((XmlBase64Binary)obj).getByteArrayValue());
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns this value as a byte array.
     */
    public byte[] getByteArrayValue()
    {
        return bytes;
    }

    public String getC14NForm()
    {
        return Base64BinarySupport.encodeBase64(bytes, true);
    }

    public NativeType getNativeType()
    {
        return NativeType.BASE64_BINARY;
    }

    @Override
    public int hashCode()
    {
        return bytes.hashCode();
    }

    public boolean isWhiteSpace()
    {
        return false;
    }
}

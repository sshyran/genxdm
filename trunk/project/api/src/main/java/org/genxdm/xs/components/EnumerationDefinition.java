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
package org.genxdm.xs.components;

import java.util.List;

import org.genxdm.typed.types.AtomBridge;

/**
 * A list of atomic values specified by an xs:enumeration.
 */
public interface EnumerationDefinition
    extends ForeignAttributes
{
    /**
     * A set of values from the value space of the base type definition.
     */
    <A> List<A> getValue(AtomBridge<A> bridge);
}
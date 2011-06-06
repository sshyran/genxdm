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
package org.genxdm.xpath.v10;

import org.genxdm.xpath.v10.expressions.ExprException;
import org.genxdm.xpath.v10.variants.Variant;

public interface ExtensionContext<N>
{
	boolean available(String name);

	Variant<N> call(String name, N contextItem, Variant<N>[] args) throws ExprException;
}
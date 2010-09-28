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
package org.gxml.bridgetest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class ValidationTestBase<N> 
    extends GxTestBase<N>
{
	public void testDummy()
	{
	    // TODO well, *something*!!
	}

	private URI baseURI()
	{
		try
		{
			return new URI("c:/work/gxml/data/xmlschema2006-11-06/msData/additional/");
		}
		catch (final URISyntaxException e)
		{
			throw new AssertionError(e);
		}
	}

	@SuppressWarnings("unused")
	private InputStream fileInputStream(final String fileName) throws FileNotFoundException
	{
		final File file = new File(baseURI().toString().concat(fileName));
		return new FileInputStream(file);
	}
}
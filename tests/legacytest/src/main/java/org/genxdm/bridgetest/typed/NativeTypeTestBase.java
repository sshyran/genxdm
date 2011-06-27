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
package org.genxdm.bridgetest.typed;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.genxdm.ProcessingContext;
import org.genxdm.bridgetest.GxTestBase;
import org.genxdm.exceptions.GxmlAtomCastException;
import org.genxdm.exceptions.PreCondition;
import org.genxdm.exceptions.SpillagePolicy;
import org.genxdm.names.NameSource;
import org.genxdm.typed.TypedContext;
import org.genxdm.typed.types.AtomBridge;
import org.genxdm.typed.types.CastingContext;
import org.genxdm.typed.types.Emulation;
import org.genxdm.xs.enums.DerivationMethod;
import org.genxdm.xs.enums.KeeneQuantifier;
import org.genxdm.xs.enums.WhiteSpacePolicy;
import org.genxdm.xs.facets.Facet;
import org.genxdm.xs.facets.FacetKind;
import org.genxdm.xs.facets.FractionDigits;
import org.genxdm.xs.types.AtomicType;
import org.genxdm.xs.types.NativeType;
import org.genxdm.xs.types.SimpleType;
import org.genxdm.xs.types.Type;

public abstract class NativeTypeTestBase<N, A> 
    extends GxTestBase<N>
{
	private static final boolean CHECK_SOURCE_TYPE = false;

	private void assertSourceType(final NativeType expect, final QName actual)
	{
		if (CHECK_SOURCE_TYPE)
		{
			assertEquals(expect.toQName(), actual);
		}
	}

	private void assertYearMonthDurationFail(final String lexical, final TypedContext<N, A> pcx)
	{
		final AtomBridge<A> atomBridge = pcx.getAtomBridge();
		try
		{
			atomBridge.compile(lexical, NativeType.DURATION_YEARMONTH);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			assertEquals(lexical, e.getSourceValue());
			assertEquals(NativeType.UNTYPED_ATOMIC.toQName(), e.getSourceType());
			assertEquals(NativeType.DURATION_YEARMONTH.toQName(), e.getTargetType());
			assertEquals(new QName("http://www.w3.org/2005/xqt-errors/", "FORG0001"), e.getErrorCode());
		}
	}

	private void assertYearMonthDurationPass(final String canonical, final int months, final String lexical, final TypedContext<N, A> pcx)
	{
		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		// Verify that the months value round-trips.
		{
			final A atom = atomBridge.createYearMonthDuration(months);
			assertEquals(months, atomBridge.getDurationTotalMonths(atom));
		}

		// Verify that the lexical representation parses to yield the months value and the canonical representation.
		try
		{
			final A atom = atomBridge.compile(lexical, NativeType.DURATION_YEARMONTH);
			assertEquals(months, atomBridge.getDurationTotalMonths(atom));
			assertEquals(canonical, atomBridge.getC14NForm(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private CastingContext<A> castingContext(final SpillagePolicy spillagePolicy, final Emulation emulation)
	{
		PreCondition.assertArgumentNotNull(spillagePolicy, "spillagePolicy");
		PreCondition.assertArgumentNotNull(emulation, "emulation");
		return new CastingContext<A>()
		{
			public Emulation getEmulation()
			{
				return emulation;
			}

			public SpillagePolicy getSpillagePolicy()
			{
				return spillagePolicy;
			}
		};
	}

	private void checkAtomicType(final SimpleType<A> atomicType, AtomBridge<A> atomBridge)
	{
		// final AtomicType<A> prime = atomicType.prime();
		// assertEquals(prime.getName(), atomicType.getName());
		// assertFalse(atomicType.isNone());
		assertEquals(KeeneQuantifier.EXACTLY_ONE, atomicType.quantifier());
		// assertEquals(PrimeTypeKind.ATOM, atomicType.getKind());

		final NameSource nameBridge = atomBridge.getNameBridge();

		if (atomicType.getName().equals(nameBridge.nativeType(NativeType.NOTATION)))
		{
			assertTrue(atomicType.toString(), atomicType.isAbstract());
		}
		else
		{
			assertFalse(atomicType.toString(), atomicType.isAbstract());
		}
		assertFalse(atomicType.toString(), atomicType.isAnonymous());
		assertFalse(atomicType.toString(), atomicType.isAtomicUrType());

		if (atomicType.hasFacets())
		{
			final Iterator<Facet<A>> facets = atomicType.getFacets().iterator();
			assertTrue(facets.hasNext());
			for (final Facet<A> facet : atomicType.getFacets())
			{
				facet.getKind();
			}
		}
		else
		{
			final Iterator<Facet<A>> facets = atomicType.getFacets().iterator();
			assertFalse(facets.hasNext());
		}
	}

	private void checkAtomicValue(final A atom, final NativeType nativeType, final AtomBridge<A> atomBridge)
	{
		PreCondition.assertArgumentNotNull(atom, "atom");
		PreCondition.assertTrue(atomBridge.isAtom(atom));
		PreCondition.assertArgumentNotNull(atomBridge, "atomBridge");

		assertEquals(nativeType, atomBridge.getNativeType(atom));

		final String className = atom.getClass().getName();

		switch (atomBridge.getNativeType(atom))
		{
		case BOOLEAN:
		{
			assertEquals(atom, atomBridge.createBoolean(atomBridge.getBoolean(atom)));
			assertEquals(className, atomBridge.createBoolean(true).hashCode(), atomBridge.getBooleanTrue().hashCode());
			assertEquals(className, atomBridge.createBoolean(false).hashCode(), atomBridge.getBooleanFalse().hashCode());
			assertFalse(className, atomBridge.createBoolean(true).hashCode() == atomBridge.getBooleanFalse().hashCode());
			assertFalse(className, atomBridge.createBoolean(false).hashCode() == atomBridge.getBooleanTrue().hashCode());
		}
			break;
		case BASE64_BINARY:
		{
			assertEquals(className, atom, atomBridge.createBase64Binary(atomBridge.getBase64Binary(atom)));
		}
			break;
		case HEX_BINARY:
		{
			assertEquals(className, atom, atomBridge.createHexBinary(atomBridge.getHexBinary(atom)));
		}
			break;
		case GDAY:
		{
			assertEquals(atom, atomBridge.createDay(atomBridge.getDayOfMonth(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case GMONTH:
		{
			assertEquals(atom, atomBridge.createMonth(atomBridge.getMonth(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case GMONTHDAY:
		{
			assertEquals(atom, atomBridge.createMonthDay(atomBridge.getMonth(atom), atomBridge.getDayOfMonth(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case GYEAR:
		{
			assertEquals(atom, atomBridge.createYear(atomBridge.getYear(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case GYEARMONTH:
		{
			assertEquals(atom, atomBridge.createYearMonth(atomBridge.getYear(atom), atomBridge.getMonth(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case DATE:
		{
			assertEquals(className, atom, atomBridge.createDate(atomBridge.getYear(atom), atomBridge.getMonth(atom), atomBridge.getDayOfMonth(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case DATETIME:
		{
			assertEquals(atom, atomBridge.createDateTime(atomBridge.getYear(atom), atomBridge.getMonth(atom), atomBridge.getDayOfMonth(atom), atomBridge.getHourOfDay(atom),
					atomBridge.getMinute(atom), atomBridge.getIntegralSecondPart(atom), 0, atomBridge.getFractionalSecondPart(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case TIME:
		{
			assertEquals(atom, atomBridge.createTime(atomBridge.getHourOfDay(atom), atomBridge.getMinute(atom), atomBridge.getIntegralSecondPart(atom), 0, atomBridge
					.getFractionalSecondPart(atom), atomBridge.getGmtOffset(atom)));
		}
			break;
		case DURATION:
		{
			assertEquals(className, atom, atomBridge.createDuration(atomBridge.getDurationTotalMonths(atom), atomBridge.getDurationTotalSeconds(atom)));
		}
			break;
		case DURATION_DAYTIME:
		{
			assertEquals(className, atom, atomBridge.createDayTimeDuration(atomBridge.getDurationTotalSeconds(atom)));
		}
			break;
		case DURATION_YEARMONTH:
		{
			assertEquals(className, atom, atomBridge.createYearMonthDuration(atomBridge.getDurationTotalMonths(atom)));
		}
			break;
		case DECIMAL:
		{
			assertEquals(className, atom, atomBridge.createDecimal(atomBridge.getDecimal(atom)));
			assertEquals(className, atomBridge.getDecimal(atom).hashCode(), atom.hashCode());
		}
			break;
		case DOUBLE:
		{
			assertEquals(className, atom, atomBridge.createDouble(atomBridge.getDouble(atom)));
			assertEquals(className, Double.valueOf(atomBridge.getDouble(atom)).hashCode(), atom.hashCode());
		}
			break;
		case FLOAT:
		{
			assertEquals(className, atom, atomBridge.createFloat(atomBridge.getFloat(atom)));
			assertEquals(className, Float.valueOf(atomBridge.getFloat(atom)).hashCode(), atom.hashCode());
		}
			break;
		case INTEGER:
		{
			assertEquals(atom, atomBridge.createInteger(atomBridge.getInteger(atom)));
			assertEquals(className, atomBridge.getInteger(atom).hashCode(), atom.hashCode());
		}
			break;
		case LONG:
		{
			assertEquals(className, atom, atomBridge.createLong(atomBridge.getLong(atom)));
			assertEquals(className, Long.valueOf(atomBridge.getLong(atom)).hashCode(), atom.hashCode());
		}
			break;
		case INT:
		{
			assertEquals(className, atom, atomBridge.createInt(atomBridge.getInt(atom)));
			assertEquals(className, Integer.valueOf(atomBridge.getInt(atom)).hashCode(), atom.hashCode());
		}
			break;
		case SHORT:
		{
			assertEquals(className, atom, atomBridge.createShort(atomBridge.getShort(atom)));
			assertEquals(className, Short.valueOf(atomBridge.getShort(atom)).hashCode(), atom.hashCode());
		}
			break;
		case BYTE:
		{
			assertEquals(className, atom, atomBridge.createByte(atomBridge.getByte(atom)));
			assertEquals(className, Byte.valueOf(atomBridge.getByte(atom)).hashCode(), atom.hashCode());
		}
			break;
		case NEGATIVE_INTEGER:
		case POSITIVE_INTEGER:
		case NON_NEGATIVE_INTEGER:
		case NON_POSITIVE_INTEGER:
		case UNSIGNED_LONG:
		case UNSIGNED_INT:
		case UNSIGNED_SHORT:
		case UNSIGNED_BYTE:
		{
			assertEquals(className, atom, atomBridge.createIntegerDerived(atomBridge.getInteger(atom), nativeType));
			assertEquals(className, atomBridge.getInteger(atom).hashCode(), atom.hashCode());
		}
			break;
		case STRING:
		{
			assertEquals(className, atom, atomBridge.createString(atomBridge.getString(atom)));
			assertEquals(className, atomBridge.getString(atom).hashCode(), atom.hashCode());
		}
			break;
		case NORMALIZED_STRING:
		case TOKEN:
		case LANGUAGE:
		case NAME:
		case NCNAME:
		case NMTOKEN:
		case ID:
		case IDREF:
		case ENTITY:
		{
			assertEquals(className, atom, atomBridge.createStringDerived(atomBridge.getString(atom), nativeType));
			assertEquals(className, atomBridge.getString(atom).hashCode(), atom.hashCode());
		}
			break;
		case ANY_URI:
		{
			assertEquals(className, atom, atomBridge.createURI(atomBridge.getURI(atom)));
			assertEquals(className, atomBridge.getURI(atom).hashCode(), atom.hashCode());
		}
			break;
		case QNAME:
		{
			final QName qname = atomBridge.getQName(atom);
			final String namespaceURI = qname.getNamespaceURI();
			final String localName = qname.getLocalPart();
			final String prefix = qname.getPrefix();
			final A qnameAtom = atomBridge.createQName(namespaceURI, localName, copy(prefix));
			assertEquals(namespaceURI, atomBridge.getNamespaceFromQName(qnameAtom));
			assertEquals(localName, atomBridge.getLocalNameFromQName(qnameAtom));
			assertEquals(prefix, atomBridge.getPrefixFromQName(qnameAtom));
			assertEquals(className, atom, qnameAtom);
			assertEquals(className, atomBridge.getQName(atom).hashCode(), atom.hashCode());
		}
			break;
		case UNTYPED_ATOMIC:
		{
			assertEquals(className, atom, atomBridge.createUntypedAtomic(atomBridge.getC14NForm(atom)));
			assertEquals(className, atomBridge.getC14NForm(atom).hashCode(), atom.hashCode());
		}
			break;
		default:
		{
			throw new AssertionError(atomBridge.getNativeType(atom));
		}
		}

		try
		{
			assertEquals(atomBridge.getC14NForm(atom), atomBridge.getString(atomBridge.castAs(atom, NativeType.STRING, new CastingContext<A>()
			{

				public Emulation getEmulation()
				{
					return Emulation.C14N;
				}

				public SpillagePolicy getSpillagePolicy()
				{
					return SpillagePolicy.DO_THE_RIGHT_THING;
				}
			})));
		}
		catch (final GxmlAtomCastException e1)
		{
			fail();
		}

		if (atomBridge.getC14NForm(atom).trim().length() > 0)
		{
			assertFalse(atomBridge.isWhiteSpace(atom));
		}
		else
		{
			assertTrue(atomBridge.isWhiteSpace(atom));
		}

		final List<A> atoms = atomBridge.wrapAtom(atom);

		try
		{
			atoms.add(0, atom);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.add(atom);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.addAll(atoms);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.addAll(0, atoms);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.clear();

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.remove(atom);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.remove(0);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.removeAll(atoms);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.retainAll(atoms);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		try
		{
			atoms.set(0, atom);

			fail();
		}
		catch (final UnsupportedOperationException e)
		{
			// Expected
		}
		assertFalse(atoms.isEmpty());
		{
			final Iterator<A> it = atoms.iterator();
			assertTrue(it.hasNext());
			final A alias = it.next();
			assertEquals(atomBridge.getDataType(atom), atomBridge.getDataType(alias));
			assertEquals(atomBridge.getNativeType(atom), atomBridge.getNativeType(alias));
			assertEquals(atomBridge.getC14NForm(atom), atomBridge.getC14NForm(alias));
			assertFalse(it.hasNext());
		}
		assertEquals(atomBridge.getC14NForm(atom), atomBridge.getC14NString(atoms));

		{
			final Object[] array = atoms.toArray();
			assertNotNull(array);
		}
		{
			final A[] array = atoms.toArray(atomBridge.atomArray(1));
			assertEquals(1, array.length);
			assertSame(atom, array[0]);
		}
	}

	/**
	 * Invariants common to all native atomic types
	 */
	public void test00001()
	{
	    final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		for (final NativeType nativeType : NativeType.values())
		{
			switch (nativeType)
			{
			case ANY_TYPE:
			case ANY_SIMPLE_TYPE:
			case ANY_ATOMIC_TYPE:
			case UNTYPED:
			{
				// TODO: xs:anyType, xs:anySimpleType...
			}
				break;
			case IDREFS:
			case NMTOKENS:
			case ENTITIES:
			{
				// These are not atomic.
			}
				break;
			default:
			{
				final Type<A> type = pcx.getTypeDefinition(nativeType);
				assertNotNull(type);
				assertTrue(nativeType.name(), type.isAtomicType());
				final SimpleType<A> atomicType = (SimpleType<A>)type;
				assertEquals(nativeType.name() + ".getNativeType()", nativeType, atomicType.getNativeType());
				assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, atomicType.getTargetNamespace().toString());
				assertEquals(nativeType.name() + ".getLocalName()", nativeType.toQName().getLocalPart(), atomicType.getLocalName().toString());
				assertTrue(atomicType.isNative());
				assertFalse(atomicType.isSimpleUrType());
				final Type<A> baseType = atomicType.getBaseType();
				assertNotNull(baseType);
				assertFalse(atomicType.hasPatterns());
				assertFalse(atomicType.hasEnumerations());
				assertFalse(atomicType.derivedFromType(pcx.getTypeDefinition(NativeType.NOTATION), EnumSet.of(DerivationMethod.Restriction)));
				assertFalse(atomicType.isIDREFS());
				assertEquals(nativeType.name() + ".getNativeTypeDefinition()", nativeType, atomicType.getNativeTypeDefinition().getNativeType());
				assertFalse(atomicType.isAnonymous());
				assertTrue(atomicType.getFinal().isEmpty());

				checkAtomicType(atomicType, pcx.getAtomBridge());
			}
			}
		}
	}

	public void testXsAnyURI()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final SimpleType<A> atomicType = (SimpleType<A>)pcx.getTypeDefinition(NativeType.ANY_URI);
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " http://www.example.com  ";
		final String mangled = "http://www.example.com";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile(initial, NativeType.ANY_URI);
			assertEquals(mangled, atomBridge.getC14NForm(atom));
			try
			{
				final URI uri = new URI(mangled);
				final A x = atomBridge.createURI(uri);
				assertEquals(mangled, atomBridge.getC14NForm(x));
				assertTrue(uri.equals(atomBridge.getURI(x)));
			}
			catch (final URISyntaxException e)
			{
				fail();
			}
			assertNull(atomBridge.createStringDerived(null, NativeType.ANY_URI));
		}
		catch (final GxmlAtomCastException e)
		{
			fail(e.getMessage());
		}
		try
		{
			final URI uri = new URI(mangled);
			checkAtomicValue(atomBridge.createURI(uri), NativeType.ANY_URI, atomBridge);
		}
		catch (final URISyntaxException e)
		{
			fail();
		}
	}

	public void testXsBase64Binary()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.BASE64_BINARY);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		final byte[] expectBytes = "Hello, World!".getBytes();
		try
		{
			final A atom = atomBridge.createBase64Binary(expectBytes);
			final String initial = "   SGVsbG8sIFdvcmxkIQ==    ";
			final String mangled = "SGVsbG8sIFdvcmxkIQ==";
			assertEquals(mangled, atomBridge.getC14NForm(atom));
			final byte[] actualBytes = atomBridge.getBase64Binary(atomBridge.compile(initial, NativeType.BASE64_BINARY));
			assertEquals(expectBytes.length, actualBytes.length);
			for (int i = 0; i < expectBytes.length; i++)
			{
				assertEquals(expectBytes[i], actualBytes[i]);
			}
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createBase64Binary(expectBytes), NativeType.BASE64_BINARY, atomBridge);
	}

	public void testXsBoolean()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.BOOLEAN);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("true", atomicType.normalize("   true     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			assertEquals(true, atomBridge.getBoolean(atomBridge.compile("true", NativeType.BOOLEAN)));
			assertEquals(false, atomBridge.getBoolean(atomBridge.compile("false", NativeType.BOOLEAN)));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		assertEquals(true, atomBridge.getBoolean(atomBridge.createBoolean(true)));
		assertEquals(false, atomBridge.getBoolean(atomBridge.createBoolean(false)));
		assertEquals(true, atomBridge.getBoolean(atomBridge.getBooleanTrue()));
		assertEquals(false, atomBridge.getBoolean(atomBridge.getBooleanFalse()));

		checkAtomicValue(atomBridge.createBoolean(true), NativeType.BOOLEAN, atomBridge);
		checkAtomicValue(atomBridge.createBoolean(false), NativeType.BOOLEAN, atomBridge);
		checkAtomicValue(atomBridge.getBooleanFalse(), NativeType.BOOLEAN, atomBridge);
	}

	public void testXsByte()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.BYTE);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("short", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		final byte byteValue = 123;
		try
		{
			final A atom = atomBridge.compile("123", NativeType.BYTE);
			assertEquals(byteValue, atomBridge.getByte(atom));
			assertEquals(byteValue, atomBridge.getShort(atom));
			assertEquals(byteValue, atomBridge.getInt(atom));
			assertEquals(byteValue, atomBridge.getLong(atom));
			assertEquals(BigInteger.valueOf(byteValue), atomBridge.getInteger(atom));
			assertEquals(BigDecimal.valueOf(byteValue), atomBridge.getDecimal(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			assertEquals(127, atomBridge.getByte(atomBridge.compile("127", NativeType.BYTE)));
			assertEquals(-128, atomBridge.getByte(atomBridge.compile("-128", NativeType.BYTE)));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			atomBridge.compile("128", NativeType.BYTE);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
		try
		{
			atomBridge.compile("-129", NativeType.BYTE);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}

		checkAtomicValue(atomBridge.createByte(byteValue), NativeType.BYTE, atomBridge);
	}

	public void testXsDate()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.DATE);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("2009-10-18", atomicType.normalize("   2009-10-18     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("2009-10-18-05:00", NativeType.DATE);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		{
			final A gregorian = atomBridge.createDate(2009, 10, 18, -300);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createDate(2009, 10, 18, 0), NativeType.DATE, atomBridge);
	}

	public void testXsDateTime()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.DATETIME);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("2009-10-18T08:00:00", atomicType.normalize("   2009-10-18T08:00:00     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("2009-10-18T03:30:59.999-05:00", NativeType.DATETIME);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(3, atomBridge.getHourOfDay(gregorian));
			assertEquals(30, atomBridge.getMinute(gregorian));
			assertEquals(59, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ONE.movePointRight(3).subtract(BigDecimal.ONE).movePointLeft(3), atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		{
			final A gregorian = atomBridge.createDateTime(2009, 10, 18, 3, 30, 59, 0, BigDecimal.valueOf(999).movePointLeft(3), -300);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(3, atomBridge.getHourOfDay(gregorian));
			assertEquals(30, atomBridge.getMinute(gregorian));
			assertEquals(59, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ONE.movePointRight(3).subtract(BigDecimal.ONE).movePointLeft(3), atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createDateTime(2009, 10, 18, 8, 0, 0, 0, BigDecimal.ZERO, 0), NativeType.DATETIME, atomBridge);
		try
		{
			atomBridge.compile("2009-10-18T24:00:00.001-05:00", NativeType.DATETIME);
			// TODO: If hours = 24, seconds must be zero.
			// fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
		try
		{
			final A gregorian = atomBridge.compile("2009-10-18T24:00:00-05:00", NativeType.DATETIME);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			// TODO: Should rollover 24 hrs to next day.
			// assertEquals(19, atomBridge.getDayOfMonth(gregorian));
			// assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(0, atomBridge.getFractionalSecondPart(gregorian).intValueExact());
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			// TODO: XMLGregorianCalendar fails in JDK 1.5
			// fail();
		}
	}

	public void testXsDayTimeDuration()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.DURATION_DAYTIME);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("duration", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("PT1S", atomicType.normalize("   PT1S     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final BigDecimal seconds = BigDecimal.ONE;
			final A atom = atomBridge.compile("PT1S", NativeType.DURATION_DAYTIME);
			assertEquals(seconds, atomBridge.getDurationTotalSeconds(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			final BigDecimal seconds = BigDecimal.valueOf(1004199059);
			final A atom = atomBridge.compile("PT1004199059S", NativeType.DURATION_DAYTIME);
			assertEquals(seconds, atomBridge.getDurationTotalSeconds(atom));
			assertEquals("P11622DT16H10M59S", atomBridge.getC14NForm(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			final BigDecimal seconds = BigDecimal.valueOf(130);
			final A atom = atomBridge.compile("PT130S", NativeType.DURATION_DAYTIME);
			assertEquals(seconds, atomBridge.getDurationTotalSeconds(atom));
			assertEquals("PT2M10S", atomBridge.getC14NForm(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createDayTimeDuration(BigDecimal.TEN), NativeType.DURATION_DAYTIME, atomBridge);
		// We don't want to be accepting an xs:yearMonthDuration.
		try
		{
			atomBridge.compile("P3Y4M", NativeType.DURATION_DAYTIME);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
		// This should be OK, but we don't want any gibberish on the end.
		try
		{
			final BigDecimal seconds = BigDecimal.valueOf(295200);
			final A atom = atomBridge.compile("P3DT10H", NativeType.DURATION_DAYTIME);
			assertEquals(seconds, atomBridge.getDurationTotalSeconds(atom));
			assertEquals("P3DT10H", atomBridge.getC14NForm(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			atomBridge.compile("P3DT10H+08:00", NativeType.DURATION_DAYTIME);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
		// If the 'T' separator is specified then there must be at least one hour, minute or second component.
		try
		{
			atomBridge.compile("P1DT", NativeType.DURATION_DAYTIME);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
		// Standalone test to verify the exception arguments.
		try
		{
			atomBridge.castAs(atomBridge.createInteger(0), NativeType.DURATION_DAYTIME, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N));
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			assertSourceType(NativeType.INTEGER, e.getSourceType());
			assertEquals(NativeType.DURATION_DAYTIME.toQName(), e.getTargetType());
		}
	}

	public void testXsDecimal()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.DECIMAL);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());
		final Iterator<Facet<A>> facets = atomicType.getFacets().iterator();
		assertFalse(facets.hasNext());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final BigDecimal decval = BigDecimal.valueOf(123);
			final A atom = atomBridge.compile("123", NativeType.DECIMAL);
			assertEquals(decval, atomBridge.getDecimal(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createDecimal(10), NativeType.DECIMAL, atomBridge);
	}

	public void testXsDouble()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.DOUBLE);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final double dblval = 123;
			final A atom = atomBridge.compile("123", NativeType.DOUBLE);
			assertEquals(dblval, atomBridge.getDouble(atom));
			assertEquals(atomBridge.createString("1.23E2"), atomBridge.castAs(atom, NativeType.STRING, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			// TODO: Cx and DOM Bridges can't handle this yet because the cast over the SmAtomBridge (deprecated).
			// assertEquals(atomBridge.createString("123"), atomBridge.castAs(atom, NativeType.STRING,
			// castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.MODERN)));
			// assertEquals(atomBridge.createString("123"), atomBridge.castAs(atom, NativeType.STRING,
			// castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.LEGACY)));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createDouble(123), NativeType.DOUBLE, atomBridge);
	}

	public void testXsDuration()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.DURATION);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("P3YT1S", atomicType.normalize("   P3YT1S     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final BigDecimal seconds = new BigDecimal("278430.123");
			final int months = 14;
			final A atom = atomBridge.compile("P1Y2M3DT5H20M30.123S", NativeType.DURATION);
			assertEquals(seconds, atomBridge.getDurationTotalSeconds(atom));
			assertEquals(months, atomBridge.getDurationTotalMonths(atom));
			assertEquals("P1Y2M3DT5H20M30.123S", atomBridge.getC14NForm(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createDuration(1, BigDecimal.ONE), NativeType.DURATION, atomBridge);
	}

	public void testXsENTITY()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.ENTITY);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("NCName", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " Snoopy  ";
		final String mangled = "Snoopy";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.ENTITY), NativeType.ENTITY, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.ENTITY);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.ENTITY)));
			assertNull(atomBridge.createStringDerived(null, NativeType.ENTITY));
		}
		catch (final GxmlAtomCastException e)
		{
			fail(e.getMessage());
		}
	}

	public void testXsFloat()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.FLOAT);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final float fltval = 123;
			final A atom = atomBridge.compile("123", NativeType.FLOAT);
			assertEquals(fltval, atomBridge.getFloat(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createFloat(123), NativeType.FLOAT, atomBridge);
	}

	public void testXsgDay()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.GDAY);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("---01", atomicType.normalize("   ---01     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("---18-05:00", NativeType.GDAY);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(1, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		{
			final A gregorian = atomBridge.createDay(18, -300);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(1, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createDay(18, 0), NativeType.GDAY, atomBridge);
	}

	public void testXsgMonth()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.GMONTH);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("--10", atomicType.normalize("   --10     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("--10-05:00", NativeType.GMONTH);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			// TODO: XMLGregorianCalendar fails in JDK 1.5
			// fail();
		}
		{
			final A gregorian = atomBridge.createMonth(10, -300);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createMonth(10, 0), NativeType.GMONTH, atomBridge);
	}

	public void testXsgMonthDay()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.GMONTHDAY);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("--10-18", atomicType.normalize("   --10-18     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("--10-18-05:00", NativeType.GMONTHDAY);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		{
			final A gregorian = atomBridge.createMonthDay(10, 18, -300);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(18, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createMonthDay(10, 18, 0), NativeType.GMONTHDAY, atomBridge);
	}

	public void testXsgYear()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.GYEAR);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("2009", atomicType.normalize("   2009     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("2009-05:00", NativeType.GYEAR);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(1, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		{
			final A gregorian = atomBridge.createYear(2009, -300);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(1, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createYear(2009, 0), NativeType.GYEAR, atomBridge);
		try
		{
			atomBridge.compile("02004", NativeType.GYEAR);
			// TODO: If year part has more than 4 digits, leading zeros are prohibited.
			// fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
	}

	public void testXsgYearMonth()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.GYEARMONTH);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("2009-10", atomicType.normalize("   2009-10     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("2009-10-05:00", NativeType.GYEARMONTH);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		{
			final A gregorian = atomBridge.createYearMonth(2009, 10, -300);
			assertEquals(2009, atomBridge.getYear(gregorian));
			assertEquals(10, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(0, atomBridge.getHourOfDay(gregorian));
			assertEquals(0, atomBridge.getMinute(gregorian));
			assertEquals(0, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ZERO, atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createYearMonth(2009, 10, 0), NativeType.GYEARMONTH, atomBridge);
	}

	public void testXsHexBinary()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.HEX_BINARY);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		final byte[] expectBytes = "Hello, World!".getBytes();
		try
		{
			final A atom = atomBridge.createHexBinary(expectBytes);
			final String initial = "   48656C6C6F2C20576F726C6421    ";
			final String mangled = "48656C6C6F2C20576F726C6421";
			assertEquals(mangled, atomBridge.getC14NForm(atom));
			final byte[] actualBytes = atomBridge.getHexBinary(atomBridge.compile(initial, NativeType.HEX_BINARY));
			assertEquals(expectBytes.length, actualBytes.length);
			for (int i = 0; i < expectBytes.length; i++)
			{
				assertEquals(expectBytes[i], actualBytes[i]);
			}
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createHexBinary(expectBytes), NativeType.HEX_BINARY, atomBridge);
	}

	public void testXsID()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.ID);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("NCName", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " Snoopy  ";
		final String mangled = "Snoopy";
		assertEquals(mangled, atomicType.normalize(initial));
		assertTrue(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.ID), NativeType.ID, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.ID);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.ID)));
			assertNull(atomBridge.createStringDerived(null, NativeType.ID));
		}
		catch (final GxmlAtomCastException e)
		{
			fail(e.getMessage());
		}
	}

	public void testXsIDREF()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.IDREF);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("NCName", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " Snoopy  ";
		final String mangled = "Snoopy";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertTrue(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.IDREF), NativeType.IDREF, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.IDREF);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.IDREF)));
			assertNull(atomBridge.createStringDerived(null, NativeType.IDREF));
		}
		catch (final GxmlAtomCastException e)
		{
			fail(e.getMessage());
		}
	}

	public void testXsInt()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.INT);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("long", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final int intValue = 123;
			final A atom = atomBridge.compile("123", NativeType.INT);
			assertEquals(intValue, atomBridge.getInt(atom));
			assertEquals(intValue, atomBridge.getLong(atom));
			assertEquals(BigInteger.valueOf(intValue), atomBridge.getInteger(atom));
			assertEquals(BigDecimal.valueOf(intValue), atomBridge.getDecimal(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createInt(123), NativeType.INT, atomBridge);
	}

	public void testXsInteger()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.INTEGER);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("decimal", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		/**
		 * fractionDigits with value zero.
		 */
		assertTrue(atomicType.hasFacets());
		final Iterator<Facet<A>> facets = atomicType.getFacets().iterator();
		assertTrue(facets.hasNext());
		final Facet<A> facet = facets.next();
		assertEquals(FacetKind.FractionDigits, facet.getKind());
		final FractionDigits<A> fractionDigits = (FractionDigits<A>)facet;
		assertEquals(0, fractionDigits.getFractionDigits());
		assertFalse(facets.hasNext());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final BigInteger ival = BigInteger.valueOf(123);
			final A atom = atomBridge.compile("123", NativeType.INTEGER);
			assertEquals(ival, atomBridge.getInteger(atom));
			assertEquals(new BigDecimal(ival), atomBridge.getDecimal(atom));
			assertEquals(atomBridge.createString("123"), atomBridge.castAs(atom, NativeType.STRING, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createInteger(BigInteger.valueOf(123)), atomBridge.castAs(atom, NativeType.INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING,
					Emulation.C14N)));
			assertEquals(atomBridge.createInteger(123), atomBridge.castAs(atom, NativeType.INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createLong(123), atomBridge.castAs(atom, NativeType.LONG, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createInt(123), atomBridge.castAs(atom, NativeType.INT, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createShort((short)123), atomBridge.castAs(atom, NativeType.SHORT, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createByte((byte)123), atomBridge.castAs(atom, NativeType.BYTE, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(BigInteger.valueOf(-123), NativeType.NON_POSITIVE_INTEGER), atomBridge.castAs(atomBridge.createInteger(-123),
					NativeType.NON_POSITIVE_INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(BigInteger.valueOf(-123), NativeType.NEGATIVE_INTEGER), atomBridge.castAs(atomBridge.createInteger(-123),
					NativeType.NEGATIVE_INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(BigInteger.valueOf(123), NativeType.NON_NEGATIVE_INTEGER), atomBridge.castAs(atomBridge.createInteger(123),
					NativeType.NON_NEGATIVE_INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(BigInteger.valueOf(123), NativeType.POSITIVE_INTEGER), atomBridge.castAs(atomBridge.createInteger(123),
					NativeType.POSITIVE_INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(BigInteger.valueOf(123), NativeType.UNSIGNED_LONG), atomBridge.castAs(atomBridge.createInteger(123),
					NativeType.UNSIGNED_LONG, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(123, NativeType.UNSIGNED_INT), atomBridge.castAs(atomBridge.createInteger(123), NativeType.UNSIGNED_INT,
					castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(123, NativeType.UNSIGNED_SHORT), atomBridge.castAs(atomBridge.createInteger(123), NativeType.UNSIGNED_SHORT,
					castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(123, NativeType.UNSIGNED_BYTE), atomBridge.castAs(atomBridge.createInteger(123), NativeType.UNSIGNED_BYTE,
					castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
		}
		catch (final GxmlAtomCastException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}

		// Standalone test to verify the exception arguments.
		try
		{
			atomBridge.castAs(atomBridge.createInteger(0), NativeType.LANGUAGE, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N));
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			assertSourceType(NativeType.INTEGER, e.getSourceType());
			assertEquals(NativeType.LANGUAGE.toQName(), e.getTargetType());
		}

		checkAtomicValue(atomBridge.createInteger(BigInteger.TEN), NativeType.INTEGER, atomBridge);
	}

	public void testXsLanguage()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.LANGUAGE);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("token", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " en-GB  ";
		final String mangled = "en-GB";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.LANGUAGE), NativeType.LANGUAGE, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.LANGUAGE);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.LANGUAGE)));
			assertNull(atomBridge.createStringDerived(null, NativeType.LANGUAGE));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
	}

	public void testXsLong()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.LONG);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("integer", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final long longValue = 123;
			final A atom = atomBridge.compile("123", NativeType.LONG);
			assertEquals(longValue, atomBridge.getLong(atom));
			assertEquals(BigInteger.valueOf(longValue), atomBridge.getInteger(atom));
			assertEquals(BigDecimal.valueOf(longValue), atomBridge.getDecimal(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createLong(123), NativeType.LONG, atomBridge);
	}

	public void testXsName()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NAME);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("token", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " _1950-10-04_10:00  ";
		final String mangled = "_1950-10-04_10:00";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.NAME), NativeType.NAME, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.NAME);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.NAME)));
			assertNull(atomBridge.createStringDerived(null, NativeType.NAME));
		}
		catch (final GxmlAtomCastException e)
		{
			fail(e.getMessage());
		}
	}

	public void testXsNCName()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NCNAME);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("Name", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " Snoopy  ";
		final String mangled = "Snoopy";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.NCNAME), NativeType.NCNAME, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.NCNAME);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.NCNAME)));
			assertNull(atomBridge.createStringDerived(null, NativeType.NCNAME));
		}
		catch (final GxmlAtomCastException e)
		{
			fail(e.getMessage());
		}
	}

	public void testXsNegativeInteger()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NEGATIVE_INTEGER);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("nonPositiveInteger", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("-123", atomicType.normalize("   -123     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("-123", NativeType.NEGATIVE_INTEGER);
			assertEquals(BigInteger.valueOf(-123), atomBridge.getInteger(atom));
			assertEquals(atomBridge.createIntegerDerived(-123, NativeType.INTEGER).getClass(), atomBridge.castAs(
					atomBridge.createIntegerDerived(-123, NativeType.NEGATIVE_INTEGER), NativeType.INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N))
					.getClass());
			assertEquals(atomBridge.createIntegerDerived(-123, NativeType.INTEGER), atomBridge.castAs(atomBridge.createIntegerDerived(-123, NativeType.NEGATIVE_INTEGER),
					NativeType.INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createIntegerDerived(-123, NativeType.NON_POSITIVE_INTEGER), atomBridge.castAs(atomBridge.createIntegerDerived(-123,
					NativeType.NEGATIVE_INTEGER), NativeType.NON_POSITIVE_INTEGER, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
		}
		catch (final GxmlAtomCastException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}

		{
			final A atom = atomBridge.createIntegerDerived(-123, NativeType.NEGATIVE_INTEGER);
			assertEquals(BigInteger.valueOf(-123), atomBridge.getInteger(atom));
		}
		{
			final A atom = atomBridge.createIntegerDerived(BigInteger.valueOf(-123), NativeType.NEGATIVE_INTEGER);
			assertEquals(BigInteger.valueOf(-123), atomBridge.getInteger(atom));
		}

		checkAtomicValue(atomBridge.createIntegerDerived(BigInteger.valueOf(-123), NativeType.NEGATIVE_INTEGER), NativeType.NEGATIVE_INTEGER, atomBridge);
	}

	public void testXsNMTOKEN()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NMTOKEN);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("token", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " 007  ";
		final String mangled = "007";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.NMTOKEN), NativeType.NMTOKEN, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.NMTOKEN);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.NMTOKEN)));
			assertNull(atomBridge.createStringDerived(null, NativeType.NMTOKEN));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
	}

	public void testXsNonNegativeInteger()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NON_NEGATIVE_INTEGER);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("integer", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("123", atomicType.normalize("   123     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("123", NativeType.NON_NEGATIVE_INTEGER);
			assertEquals(BigInteger.valueOf(123), atomBridge.getInteger(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		{
			final A atom = atomBridge.createIntegerDerived(123, NativeType.NON_NEGATIVE_INTEGER);
			assertEquals(BigInteger.valueOf(123), atomBridge.getInteger(atom));
		}
		{
			final A atom = atomBridge.createIntegerDerived(BigInteger.valueOf(123), NativeType.NON_NEGATIVE_INTEGER);
			assertEquals(BigInteger.valueOf(123), atomBridge.getInteger(atom));
		}

		checkAtomicValue(atomBridge.createIntegerDerived(BigInteger.valueOf(123), NativeType.NON_NEGATIVE_INTEGER), NativeType.NON_NEGATIVE_INTEGER, atomBridge);
	}

	public void testXsNonPositiveInteger()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NON_POSITIVE_INTEGER);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("integer", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("-123", atomicType.normalize("   -123     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("-123", NativeType.NON_POSITIVE_INTEGER);
			assertEquals(BigInteger.valueOf(-123), atomBridge.getInteger(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		{
			final A atom = atomBridge.createIntegerDerived(BigInteger.valueOf(-123), NativeType.NON_POSITIVE_INTEGER);
			assertEquals(BigInteger.valueOf(-123), atomBridge.getInteger(atom));
		}
		{
			final A atom = atomBridge.createIntegerDerived(BigInteger.valueOf(-123), NativeType.NON_POSITIVE_INTEGER);
			assertEquals(BigInteger.valueOf(-123), atomBridge.getInteger(atom));
		}

		checkAtomicValue(atomBridge.createIntegerDerived(BigInteger.valueOf(-123), NativeType.NON_POSITIVE_INTEGER), NativeType.NON_POSITIVE_INTEGER, atomBridge);
	}

	public void testXsNormalizedString()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NORMALIZED_STRING);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("string", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.REPLACE, atomicType.getWhiteSpacePolicy());

		final String initial = " 1\t2\r3\n4  5  ";
		final String mangled = " 1 2 3 4  5  ";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final String strval = mangled;
			final A atom = atomBridge.compile(initial, NativeType.NORMALIZED_STRING);
			assertEquals(strval, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.NORMALIZED_STRING)));
			assertNull(atomBridge.createStringDerived(null, NativeType.NORMALIZED_STRING));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.NORMALIZED_STRING), NativeType.NORMALIZED_STRING, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived("123", NativeType.NORMALIZED_STRING), NativeType.NORMALIZED_STRING, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived("", NativeType.NORMALIZED_STRING), NativeType.NORMALIZED_STRING, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived("  ", NativeType.NORMALIZED_STRING), NativeType.NORMALIZED_STRING, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived("\t\r\n", NativeType.NORMALIZED_STRING), NativeType.NORMALIZED_STRING, atomBridge);

		try
		{
			final A atom = atomBridge.compile(Long.toString(Long.MAX_VALUE), NativeType.LONG);
			atomBridge.castAs(atom, NativeType.NORMALIZED_STRING, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
	}

	public void testXsNOTATION()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.NOTATION);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("p:foo", atomicType.normalize("   p:foo     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());
		assertTrue(atomicType.isAbstract());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();
		{
			final A atom = atomBridge.createQName("http://www.example.com", "foo", "p");
			final QName name = atomBridge.getQName(atom);
			assertEquals("http://www.example.com", name.getNamespaceURI());
			assertEquals("foo", name.getLocalPart());
			assertEquals("p", name.getPrefix());
		}

		checkAtomicValue(atomBridge.createQName("http://www.example.com", "foo", "p"), NativeType.QNAME, atomBridge);
		try
		{
			@SuppressWarnings("unused")
			final A atom = atomBridge.compile("p:foo", NativeType.NOTATION);
			// fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
	}

	public void testXsPositiveInteger()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.POSITIVE_INTEGER);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("nonNegativeInteger", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("123", atomicType.normalize("   123     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("123", NativeType.POSITIVE_INTEGER);
			assertEquals(BigInteger.valueOf(123), atomBridge.getInteger(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		{
			final A atom = atomBridge.createIntegerDerived(123, NativeType.POSITIVE_INTEGER);
			assertEquals(BigInteger.valueOf(123), atomBridge.getInteger(atom));
		}
		{
			final A atom = atomBridge.createIntegerDerived(BigInteger.valueOf(123), NativeType.POSITIVE_INTEGER);
			assertEquals(BigInteger.valueOf(123), atomBridge.getInteger(atom));
		}

		checkAtomicValue(atomBridge.createIntegerDerived(BigInteger.valueOf(123), NativeType.POSITIVE_INTEGER), NativeType.POSITIVE_INTEGER, atomBridge);
	}

	public void testXsQName()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.QNAME);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals(copy("anyAtomicType"), baseType.getLocalName());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("p:foo", atomicType.normalize("   p:foo     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();
		{
			final A atom = atomBridge.createQName(copy("http://www.example.com"), copy("foo"), "p");
			assertEquals(copy("http://www.example.com"), atomBridge.getNamespaceFromQName(atom));
			assertEquals(copy("foo"), atomBridge.getLocalNameFromQName(atom));
			assertEquals("p", atomBridge.getPrefixFromQName(atom));
			final QName name = atomBridge.getQName(atom);
			assertEquals("http://www.example.com", name.getNamespaceURI());
			assertEquals("foo", name.getLocalPart());
			assertEquals("p", name.getPrefix());
		}

		checkAtomicValue(atomBridge.createQName("http://www.example.com", "foo", "p"), NativeType.QNAME, atomBridge);
		try
		{
			@SuppressWarnings("unused")
			final A atom = atomBridge.compile("p:foo", NativeType.QNAME);
			// fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
	}

	public void testXsShort()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.SHORT);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("int", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("6", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		final short shortValue = 123;
		try
		{
			final A atom = atomBridge.compile("123", NativeType.SHORT);
			assertEquals(shortValue, atomBridge.getShort(atom));
			assertEquals(shortValue, atomBridge.getInt(atom));
			assertEquals(shortValue, atomBridge.getLong(atom));
			assertEquals(BigInteger.valueOf(shortValue), atomBridge.getInteger(atom));
			assertEquals(BigDecimal.valueOf(shortValue), atomBridge.getDecimal(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createShort(shortValue), NativeType.SHORT, atomBridge);
	}

	public void testXsString()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.STRING);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.PRESERVE, atomicType.getWhiteSpacePolicy());

		assertEquals("   6     ", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createString("123"), NativeType.STRING, atomBridge);
		checkAtomicValue(atomBridge.createString(""), NativeType.STRING, atomBridge);
		checkAtomicValue(atomBridge.createString("    "), NativeType.STRING, atomBridge);
		checkAtomicValue(atomBridge.createString("\t\r\n"), NativeType.STRING, atomBridge);
		try
		{
			final String strval = "123";
			final A atom = atomBridge.compile("123", NativeType.STRING);
			assertEquals(strval, atomBridge.getString(atom));
			assertNull(atomBridge.createString(null));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
	}

	public void testXsTime()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.TIME);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("08:00:00", atomicType.normalize("   08:00:00     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A gregorian = atomBridge.compile("03:30:59.999-05:00", NativeType.TIME);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(1, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(3, atomBridge.getHourOfDay(gregorian));
			assertEquals(30, atomBridge.getMinute(gregorian));
			assertEquals(59, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ONE.movePointRight(3).subtract(BigDecimal.ONE).movePointLeft(3), atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		{
			final A gregorian = atomBridge.createTime(3, 30, 59, 0, BigDecimal.valueOf(999).movePointLeft(3), -300);
			assertEquals(1970, atomBridge.getYear(gregorian));
			assertEquals(1, atomBridge.getMonth(gregorian));
			assertEquals(1, atomBridge.getDayOfMonth(gregorian));
			assertEquals(3, atomBridge.getHourOfDay(gregorian));
			assertEquals(30, atomBridge.getMinute(gregorian));
			assertEquals(59, atomBridge.getIntegralSecondPart(gregorian));
			assertEquals(BigDecimal.ONE.movePointRight(3).subtract(BigDecimal.ONE).movePointLeft(3), atomBridge.getFractionalSecondPart(gregorian));
			assertEquals(-300, atomBridge.getGmtOffset(gregorian));
		}

		checkAtomicValue(atomBridge.createTime(8, 0, 0, 0, BigDecimal.ZERO, 0), NativeType.TIME, atomBridge);
	}

	public void testXsToken()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.TOKEN);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("normalizedString", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		final String initial = " 1\t2\r3\n4  5  ";
		final String mangled = "1 2 3 4 5";
		assertEquals(mangled, atomicType.normalize(initial));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.TOKEN), NativeType.TOKEN, atomBridge);
		try
		{
			final A atom = atomBridge.compile(initial, NativeType.TOKEN);
			assertEquals(mangled, atomBridge.getString(atom));
			assertEquals(mangled, atomBridge.getC14NForm(atomBridge.createStringDerived(initial, NativeType.TOKEN)));
			assertNull(atomBridge.createStringDerived(null, NativeType.TOKEN));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			final A atom = atomBridge.compile("123", NativeType.TOKEN);
			assertEquals("123", atomBridge.getString(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			final A atom = atomBridge.compile("", NativeType.TOKEN);
			assertEquals("", atomBridge.getString(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		checkAtomicValue(atomBridge.createStringDerived(initial, NativeType.TOKEN), NativeType.TOKEN, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived("123", NativeType.TOKEN), NativeType.TOKEN, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived("", NativeType.TOKEN), NativeType.TOKEN, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived(" ", NativeType.TOKEN), NativeType.TOKEN, atomBridge);
		checkAtomicValue(atomBridge.createStringDerived("\t\r\n", NativeType.TOKEN), NativeType.TOKEN, atomBridge);
	}

	public void testXsUnsignedByte()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.UNSIGNED_BYTE);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("unsignedShort", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("+005", atomicType.normalize("   +005     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("255", NativeType.UNSIGNED_BYTE);
			assertEquals(255, atomBridge.getUnsignedByte(atom));
			assertEquals(255, atomBridge.getUnsignedShort(atom));
			assertEquals(255, atomBridge.getUnsignedInt(atom));
			assertEquals(BigInteger.valueOf(255), atomBridge.getInteger(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		{
			final A atom = atomBridge.createIntegerDerived(255, NativeType.UNSIGNED_BYTE);
			assertEquals(BigInteger.valueOf(255), atomBridge.getInteger(atom));
		}

		try
		{
			final A atom = atomBridge.compile("0", NativeType.UNSIGNED_BYTE);
			assertEquals(BigInteger.valueOf(0), atomBridge.getInteger(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		try
		{
			atomBridge.compile("256", NativeType.UNSIGNED_BYTE);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
		try
		{
			atomBridge.compile("-1", NativeType.UNSIGNED_BYTE);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}

		checkAtomicValue(atomBridge.createIntegerDerived(255, NativeType.UNSIGNED_BYTE), NativeType.UNSIGNED_BYTE, atomBridge);
	}

	public void testXsUnsignedInt()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.UNSIGNED_INT);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("unsignedLong", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("+004294967295", atomicType.normalize("   +004294967295     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("4294967295", NativeType.UNSIGNED_INT);
			assertEquals(BigInteger.valueOf(4294967295l), atomBridge.getInteger(atom));
			assertEquals(4294967295l, atomBridge.getUnsignedInt(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		{
			final A atom = atomBridge.createIntegerDerived(4294967295l, NativeType.UNSIGNED_INT);
			assertEquals(BigInteger.valueOf(4294967295l), atomBridge.getInteger(atom));
		}
		try
		{
			final A atom = atomBridge.compile(Long.toString(Long.MAX_VALUE), NativeType.LONG);
			atomBridge.castAs(atom, NativeType.UNSIGNED_INT, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N));
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}

		checkAtomicValue(atomBridge.createIntegerDerived(4294967295l, NativeType.UNSIGNED_INT), NativeType.UNSIGNED_INT, atomBridge);
	}

	public void testXsUnsignedLong()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.UNSIGNED_LONG);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("nonNegativeInteger", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("+004294967295", atomicType.normalize("   +004294967295     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("4294967295", NativeType.UNSIGNED_LONG);
			assertEquals(BigInteger.valueOf(4294967295l), atomBridge.getInteger(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		{
			final A atom = atomBridge.createIntegerDerived(BigInteger.valueOf(4294967295l), NativeType.UNSIGNED_LONG);
			assertEquals(BigInteger.valueOf(4294967295l), atomBridge.getInteger(atom));
		}

		checkAtomicValue(atomBridge.createIntegerDerived(BigInteger.valueOf(4294967295l), NativeType.UNSIGNED_LONG), NativeType.UNSIGNED_LONG, atomBridge);
	}

	public void testXsUnsignedShort()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.UNSIGNED_SHORT);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("unsignedInt", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("+0065535", atomicType.normalize("   +0065535     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final A atom = atomBridge.compile("65535", NativeType.UNSIGNED_SHORT);
			assertEquals(65535, atomBridge.getUnsignedShort(atom));
			assertEquals(65535, atomBridge.getUnsignedInt(atom));
			assertEquals(BigInteger.valueOf(65535), atomBridge.getInteger(atom));
			assertEquals(atomBridge.createString("65535"), atomBridge.castAs(atom, NativeType.STRING, castingContext(SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createInteger(123), atomBridge.castAs(atomBridge.createIntegerDerived(123, NativeType.UNSIGNED_SHORT), NativeType.INTEGER, castingContext(
					SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
			assertEquals(atomBridge.createLong(123), atomBridge.castAs(atomBridge.createIntegerDerived(123, NativeType.UNSIGNED_SHORT), NativeType.LONG, castingContext(
					SpillagePolicy.DO_THE_RIGHT_THING, Emulation.C14N)));
		}
		catch (final GxmlAtomCastException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}

		{
			final A atom = atomBridge.createIntegerDerived(65535, NativeType.UNSIGNED_SHORT);
			assertEquals(BigInteger.valueOf(65535), atomBridge.getInteger(atom));
		}

		checkAtomicValue(atomBridge.createIntegerDerived(65535, NativeType.UNSIGNED_SHORT), NativeType.UNSIGNED_SHORT, atomBridge);
	}

	public void testXsUntypedAtomic()
	{
        final ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.UNTYPED_ATOMIC);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("anyAtomicType", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.PRESERVE, atomicType.getWhiteSpacePolicy());

		assertEquals("   6     ", atomicType.normalize("   6     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		try
		{
			final String strval = "123";
			final A atom = atomBridge.compile("123", NativeType.UNTYPED_ATOMIC);
			assertEquals(strval, atomBridge.getC14NForm(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}

		checkAtomicValue(atomBridge.createUntypedAtomic("123"), NativeType.UNTYPED_ATOMIC, atomBridge);
	}

	public void testXsYearMonthDuration()
	{
	    ProcessingContext<N> ctx = newProcessingContext();
        final TypedContext<N, A> pcx = ctx.getTypedContext();

		final Type<A> type = pcx.getTypeDefinition(NativeType.DURATION_YEARMONTH);
		final AtomicType<A> atomicType = (AtomicType<A>)type;
		final Type<A> baseType = atomicType.getBaseType();
		assertNotNull(baseType);
		assertEquals("duration", baseType.getLocalName().toString());
		assertEquals(WhiteSpacePolicy.COLLAPSE, atomicType.getWhiteSpacePolicy());

		assertEquals("P3Y4M", atomicType.normalize("   P3Y4M     "));
		assertFalse(atomicType.isID());
		assertFalse(atomicType.isIDREF());

		assertFalse(atomicType.hasFacets());

		final AtomBridge<A> atomBridge = pcx.getAtomBridge();

		assertYearMonthDurationPass("P1Y2M", 14, "P1Y2M", pcx);
		assertYearMonthDurationPass("P1Y2M", 14, "P0Y14M", pcx);
		assertYearMonthDurationPass("P1Y2M", 14, "P14M", pcx);

		assertYearMonthDurationPass("P1Y1M", 13, "P1Y1M", pcx);
		assertYearMonthDurationPass("P1Y", 12, "P1Y", pcx);
		assertYearMonthDurationPass("P11M", 11, "P11M", pcx);
		assertYearMonthDurationPass("P10M", 10, "P10M", pcx);
		assertYearMonthDurationPass("P9M", 9, "P9M", pcx);
		assertYearMonthDurationPass("P8M", 8, "P8M", pcx);
		assertYearMonthDurationPass("P7M", 7, "P7M", pcx);
		assertYearMonthDurationPass("P6M", 6, "P6M", pcx);
		assertYearMonthDurationPass("P5M", 5, "P5M", pcx);
		assertYearMonthDurationPass("P4M", 4, "P4M", pcx);
		assertYearMonthDurationPass("P3M", 3, "P3M", pcx);
		assertYearMonthDurationPass("P2M", 2, "P2M", pcx);
		assertYearMonthDurationPass("P1M", 1, "P1M", pcx);

		assertYearMonthDurationPass("P0M", 0, "P0M", pcx);
		assertYearMonthDurationPass("P0M", 0, "P0Y0M", pcx);

		assertYearMonthDurationPass("-P1M", -1, "-P1M", pcx);
		assertYearMonthDurationPass("-P1Y", -12, "-P1Y", pcx);

		assertYearMonthDurationPass("-P1Y1M", -13, "-P1Y1M", pcx);
		assertYearMonthDurationPass("-P1Y1M", -13, "-P0Y13M", pcx);
		assertYearMonthDurationPass("-P1Y1M", -13, "-P13M", pcx);

		assertYearMonthDurationPass("-P1Y2M", -14, "-P1Y2M", pcx);
		assertYearMonthDurationPass("-P1Y2M", -14, "-P0Y14M", pcx);
		assertYearMonthDurationPass("-P1Y2M", -14, "-P14M", pcx);

		assertYearMonthDurationPass("P3Y4M", 40, "P3Y4M", pcx);

		assertYearMonthDurationPass("P1347Y", 1347 * 12, "P1347Y", pcx);
		assertYearMonthDurationPass("P112Y3M", 1347, "P1347M", pcx);
		assertYearMonthDurationPass("-P112Y3M", -1347, "-P1347M", pcx);

		checkAtomicValue(atomBridge.createYearMonthDuration(40), NativeType.DURATION_YEARMONTH, atomBridge);

		assertYearMonthDurationFail("PY43M", pcx);
		assertYearMonthDurationFail("PT0S", pcx);
		assertYearMonthDurationFail("P-1347M", pcx);
		assertYearMonthDurationFail("P24YM", pcx);
		assertYearMonthDurationFail("P1Y2MT", pcx);
		// We don't want to be accepting an xs:yearMonthDuration.
		// Verify that the canonical representation starting from the lexical representation.
		try
		{
			final int months = 255;
			final A atom = atomBridge.compile("P20Y15M", NativeType.DURATION_YEARMONTH);
			assertEquals(months, atomBridge.getDurationTotalMonths(atom));
			assertEquals("P21Y3M", atomBridge.getC14NForm(atom));
		}
		catch (final GxmlAtomCastException e)
		{
			fail();
		}
		// This should be OK, but we don't want any gibberish on the end.
		try
		{
			atomBridge.compile("P20Y15M+08:00", NativeType.DURATION_YEARMONTH);
			fail();
		}
		catch (final GxmlAtomCastException e)
		{
			// OK
		}
	}

	/**
	 * Do anything to manufacture a String that is equal, but not identical (the same), as the original.
	 * <p>
	 * This method has the post-condition that the strings are equal but not the same.
	 * </p>
	 * 
	 * @param original
	 *            The original.
	 * @return A copy of the original string.
	 */
	public String copy(final String original)
	{
		final String copy = original.concat("junk").substring(0, original.length());
		// Post-conditions verify that this is effective.
		assertEquals(original, copy);
		assertNotSame(original, copy);
		// Be Paranoid
		assertTrue(original.equals(copy));
		assertFalse(original == copy);
		// OK. That'll do.
		return copy;
	}
}
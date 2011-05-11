package org.genxdm.samples.performance.bridges;

import java.io.IOException;
import java.io.StringWriter;

import org.genxdm.exceptions.GxmlMarshalException;

public class TestSerialize<N,A> extends BaseBridgePerfTest<N,A> {

	StringWriter m_writer;
	@Override
	public String getName() {
		return "Serialize";
	}
	
	@Override
	public void iterativeSetup() {
		m_writer = new StringWriter();
	}
	
	@Override
	public void execute() {
        try {
			getDocHandler().write(m_writer, getTestNode());
		} 
        catch (GxmlMarshalException e) 
		{
			throw new RuntimeException(e);
		} 
        catch (IOException e) 
		{
			throw new RuntimeException(e);
		}
	}
	@Override
	public Iterable<String> iterativeTeardown() {
		m_writer = null;
		return null; 
	}
}
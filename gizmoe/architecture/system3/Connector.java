package gizmo.architecture.system3;
/******************************************************************************************************************
* File:WildValueExtrapolationFilter.java
* Course: 17655
* Project: Assignment 1
* Versions:
*	1.0 7 February 2013 - Initial code (ukd).
*
* Description:
*
* This class will filter out the "wild" data points. It will replace them with an average of the last
* valid measurement and the next valid measurement. Currently, it will replace the invalid measurement 
* only with the last valid measurement. In effect, there is no look-ahead yet. Additionally, if the first
* value is invalid itself, just replace with zero!
*
* Parameters: 		 
* 	int WildLowBound - The lower bound on the "wild values".
* 	int WildHighBound - The higher bound on the "wild values".
*
* Internal Methods: 
* 	public void run()
* 
* Main author/Owner: 
* 	Upsham K Dawra
*
******************************************************************************************************************/
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Connector extends Thread
{
	protected static final int TIME = 0;
	protected static final int VELOCITY = 1;
	protected static final int ALTITUDE = 2;
	protected static final int PRESSURE = 3;
	protected static final int TEMPERATURE = 4;
	protected static final int ATTITUDE = 5;
	protected static final int CORRECTION_OFFSET = 10;
	protected static final int ERROR_DATA = -10;
	protected PipedInputStream InputReadPort = new PipedInputStream();
	protected PipedOutputStream OutputWritePort = new PipedOutputStream();
	
	protected Connector InputFilter;
	
	/***************************************************************************
	* CONCRETE METHOD:: Connect
	* Purpose: This method connects filters to each other. All connections are
	* through the inputport of each filter. That is each filter's inputport is
	* connected to another filter's output port through this method.
	*
	* Arguments:
	* 	FilterFramework - this is the filter that this filter will connect to.
	*
	* Returns: void
	*
	* Exceptions: IOException
	*
	****************************************************************************/



	 // EndOfInputStream
	 void Connect( SingleOutputFilterFramework Filter )
	{
		try
		{
			// Connect this filter's input to the upstream pipe's output stream

			InputReadPort.connect( Filter.OutputWritePort );
			InputFilter = Filter;

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );

		} // catch

	} // Connect
	void Connect (MultiOutputFilterFramework Filter){
		try
		{
			// Connect this filter's input to the upstream pipe's output stream
				InputReadPort.connect( Filter.OutputWritePort );
				InputFilter = Filter;

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );

		} // catch
	}
	void Connect (MultiOutputFilterFramework Filter, int port){
		try
		{
			// Connect this filter's input to the upstream pipe's output stream
			switch(port){
			case 1:
				InputReadPort.connect( Filter.OutputWritePort );
				InputFilter = Filter;
				break;
			case 2:
				InputReadPort.connect( Filter.OutputWritePort2 );
				InputFilter = Filter;
				break;
			}

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );

		} // catch
	}
	/******************************************************************************
	 * All read methods here, since we never "merge" and support only one input port
	 ******************************************************************************/
	
	/***************************************************************************
	* InnerClass:: EndOfStreamExeception
	* Purpose: This
	*
	*
	*
	* Arguments: none
	*
	* Returns: none
	*
	* Exceptions: none
	*
	****************************************************************************/

	class EndOfStreamException extends Exception {
		
		static final long serialVersionUID = 0; // the version for serializing

		EndOfStreamException () { super(); }

		EndOfStreamException(String s) { super(s); }

	} // class
	/***************************************************************************
	* CONCRETE METHOD:: EndOfInputStream
	* Purpose: This method is used within this framework which is why it is private
	* It returns a true when there is no more data to read on the input port of
	* the instance filter. What it really does is to check if the upstream filter
	* is still alive. This is done because Java does not reliably handle broken
	* input pipes and will often continue to read (junk) from a broken input pipe.
	*
	* Arguments: void
	*
	* Returns: A value of true if the previous filter has stopped sending data,
	*		   false if it is still alive and sending data.
	*
	* Exceptions: none
	*
	****************************************************************************/
	private boolean EndOfInputStream()
	{
		if (InputFilter.isAlive())
		{
			return false;

		} else {

			return true;

		} // if

	}
	
	/***************************************************************************
	* CONCRETE METHOD:: ReadFilterInputPort
	* Purpose: This method reads data from the input port one byte at a time.
	*
	* Arguments: void
	*
	* Returns: byte of data read from the input port of the filter.
	*
	* Exceptions: IOExecption, EndOfStreamException (rethrown)
	*
	****************************************************************************/

	byte ReadFilterInputPort() throws EndOfStreamException
	{
		byte datum = 0;

		/***********************************************************************
		* Since delays are possible on upstream filters, we first wait until
		* there is data available on the input port. We check,... if no data is
		* available on the input port we wait for a quarter of a second and check
		* again. Note there is no timeout enforced here at all and if upstream
		* filters are deadlocked, then this can result in infinite waits in this
		* loop. It is necessary to check to see if we are at the end of stream
		* in the wait loop because it is possible that the upstream filter completes
		* while we are waiting. If this happens and we do not check for the end of
		* stream, then we could wait forever on an upstream pipe that is long gone.
		* Unfortunately Java pipes do not throw exceptions when the input pipe is
		* broken.
		***********************************************************************/

		try
		{
			while (InputReadPort.available()==0 )
			{
				if (EndOfInputStream())
				{
					throw new EndOfStreamException("End of input stream reached");

				} //if

				sleep(250);

			} // while

		} // try

		catch( EndOfStreamException Error )
		{
			throw Error;

		} // catch

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " Error in read port wait loop::" + Error );

		} // catch

		/***********************************************************************
		* If at least one byte of data is available on the input
		* pipe we can read it. We read and write one byte to and from ports.
		***********************************************************************/

		try
		{
			datum = (byte)InputReadPort.read();
			return datum;

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " Pipe read error::" + Error );
			return datum;

		} // catch

	} // ReadFilterPort
	
	int ReadIDFromFilterInputPort() throws EndOfStreamException
	{
		int id = 0;
		int IdLength = 4;

		for (int i=0; i<IdLength; i++ )
		{
			byte datum = ReadFilterInputPort();	// This is where we read the byte from the stream...

			id = id | (datum & 0xFF);		// We append the byte on to ID...

			if (i != IdLength-1)				// If this is not the last byte, then slide the
			{									// previously appended byte to the left by one byte
				id = id << 8;					// to make room for the next byte we append to the ID

			} // if
		} // for
		return id;


	} // ReadFilterPort

	long ReadMeasurementFromFilterInputPort() throws EndOfStreamException{
		long measurement = 0;
		int MeasurementLength = 8;
		for (int i=0; i<MeasurementLength; i++ )
		{
			byte databyte = ReadFilterInputPort();				
			measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

			if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
			{												// previously appended byte to the left by one byte
				measurement = measurement << 8;				// to make room for the next byte we append to the
				// measurement
			} // if

		} // if
		return measurement;
	}
}

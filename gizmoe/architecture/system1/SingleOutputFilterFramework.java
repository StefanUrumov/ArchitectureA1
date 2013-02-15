/*******************************************************************************************************
* File:SingleOutputFilterFramework.java
* Course: 17655
* Project: Assignment 1
* Versions:
*	1.0 15 February 2013 - Initial code (ukd).
*
* Description:
*
* The class defining single output port type filters. Simplified APIs because single output means that
* lesser info is needed from developers :)
*
* Main author/Owner: 
* 	Upsham K Dawra
*
********************************************************************************************************/
package gizmoe.architecture.system1;
import java.io.*;
import java.nio.ByteBuffer;

public class SingleOutputFilterFramework extends Connector
{
	void writeInt(int data) {
		try{
			byte[] bytes = ByteBuffer.allocate(4).putInt(data).array();
			for(byte bite:bytes){
				WriteFilterOutputPort(bite);
			}
		}//try
		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch

		return;
	}

	void writeDouble(double data){
		try{
			byte[] bytes = ByteBuffer.allocate(8).putDouble(data).array();
			for(byte bite:bytes){
				WriteFilterOutputPort(bite);
			}
		}//try
		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch
	}
	
	void writeLong(long data) {
		try{
			byte[] bytes = ByteBuffer.allocate(8).putLong(data).array();
			for(byte bite:bytes){
				WriteFilterOutputPort(bite);
			}
		}//try
		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch
	}

	/***************************************************************************
	* CONCRETE METHOD:: WriteFilterOutputPort
	* Purpose: This method writes data to the output port one byte at a time.
	*
	* Arguments:
	* 	byte datum - This is the byte that will be written on the output port.of
	*	the filter.
	*
	* Returns: void
	*
	* Exceptions: IOException
	*
	****************************************************************************/

	void WriteFilterOutputPort(byte datum)
	{
		try
		{
            OutputWritePort.write((int) datum );
		   	OutputWritePort.flush();

		} // try

		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch

		return;

	} // WriteFilterPort



	/***************************************************************************
	* CONCRETE METHOD:: ClosePorts
	* Purpose: This method is used to close the input and output ports of the
	* filter. It is important that filters close their ports before the filter
	* thread exits.
	*
	* Arguments: void
	*
	* Returns: void
	*
	* Exceptions: IOExecption
	*
	****************************************************************************/

	void ClosePorts()
	{
		try
		{
			InputReadPort.close();
			OutputWritePort.close();
		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch

	} // ClosePorts


	int readID(String fileName, DataInputStream in, int bytesread, int byteswritten) throws IOException {
		int id = 0;
		int IdLength = 4;
		byte databyte = 0;

		try{
			for (int i=0; i<IdLength; i++ )
			{
				databyte = in.readByte();	// This is where we read the byte from the stream...
				bytesread++;
		
				id = id | (databyte & 0xFF);		// We append the byte on to ID...
	
				if (i != IdLength-1)				// If this is not the last byte, then slide the
				{									// previously appended byte to the left by one byte
					id = id << 8;					// to make room for the next byte we append to the ID
	
				} // if
			} // for
		}

	catch ( EOFException eoferr )
		{
			System.out.println("\n" + this.getName() + " " + fileName + "::End of file reached..." );
			try
			{
				id = ERROR_DATA;
				in.close();
				System.out.println( "\n" + this.getName() + " " + fileName +
						"::Read file complete, bytes read::" + bytesread + " bytes written: " + byteswritten );
			}
		/***********************************************************************************
		*	The following exception is raised should we have a problem closing the file.
		***********************************************************************************/
			catch (Exception closeerr)
			{
				System.out.println("\n" + this.getName() + "::Problem closing input data file::" + fileName + closeerr);

			} // catch

		} // catch

		return id;
	}

	long getData(String fileName, DataInputStream in,  int bytesread, int byteswritten)  throws IOException {
		long measurement = 0;
		int MeasurementLength = 8;

		try {
			for (int i=0; i<MeasurementLength; i++ )
			{
				byte databyte = in.readByte();
				bytesread++;										// Increment the byte count
				measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

				if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
				{												// previously appended byte to the left by one byte
					measurement = measurement << 8;				// to make room for the next byte we append to the
					// measurement
				} // if
				
			} // if
		}

		catch ( EOFException eoferr )
		{
			System.out.println("\n" + this.getName() + "End of file reached..." );
			try
			{
				measurement = ERROR_DATA;
				in.close();
				System.out.println( "\n" + this.getName() + "::Read file complete, bytes read::"
						+ bytesread + " bytes written: " + byteswritten );
			}
		/***********************************************************************************
		*	The following exception is raised should we have a problem closing the file.
		***********************************************************************************/
			catch (Exception closeerr)
			{
				System.out.println("\n" + this.getName() + "::Problem closing input data file::" + fileName + closeerr);

			} // catch
		} // catch

		return measurement;
	}

	/***************************************************************************
	* CONCRETE METHOD:: run
	* Purpose: This is actually an abstract method defined by Thread. It is called
	* when the thread is started by calling the Thread.start() method. In this
	* case, the run() method should be overridden by the filter programmer using
	* this framework superclass
	*
	* Arguments: void
	*
	* Returns: void
	*
	* Exceptions: IOExecption
	*
	****************************************************************************/

	public void run()
    {
		// The run method should be overridden by the subordinate class. Please
		// see the example applications provided for more details.

	} // run
	
	
} // FilterFramework class

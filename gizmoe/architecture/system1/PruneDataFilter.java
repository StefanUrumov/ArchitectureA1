package gizmoe.architecture.system1;
/******************************************************************************************************
* File:PruneDataFilter.java
* Course: 17655
* Project: Assignment 1
* Versions:
*	1.0 7 February 2013 - Initial code (ukd).
*
* Description:
*
* This class will remove the data points (both id and measurement) that do not have their ids specified
* in the idsToKeep list. It will just forward the rest of the ids as normal, preserving order. 
*
* Parameters: 		 
* 	List<Integer> idsToKeep - The list of the parameters that we want to keep in stream
*
* Internal Methods: 
* 	public void run()
* 
* Main author/Owner: 
* 	Upsham K Dawra
*
*******************************************************************************************************/
import java.util.Arrays;
import java.util.List;

public class PruneDataFilter extends SingleOutputFilterFramework
{
	private List<Integer> idsToKeep = Arrays.asList(TIME, ALTITUDE, TEMPERATURE, PRESSURE);  
	public void run()
    {
		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		int id;
		long measurement;
		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::PruneDataFilter Reading ");

		while (true)
		{
			try
			{
				id = ReadIDFromFilterInputPort();
				measurement = ReadMeasurementFromFilterInputPort();
				bytesread+=12;
				if(idsToKeep.contains(id)){
					writeInt(id);
					writeLong(measurement);
					byteswritten+=12;
				}
			} // try
			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.println( "\n" + this.getName() + "::PruneDataFilter Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run

} // PruneDataFilter
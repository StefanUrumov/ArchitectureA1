/*****************************************************************************************************
* File:ConvertTemperatureFilter.java
* Course: 17655
* Project: Assignment 1
* Versions:
*	1.0 7 February 2013 - Initial code (ukd).
*
* Description:
*
* Converts temperature units and does not meddle with other data
*
* Internal Methods: 
* 	public void run()
* 
* Main author/Owner: 
* 	Upsham K Dawra
*
*****************************************************************************************************/
package gizmoe.architecture.system1;
public class ConvertTemperatureFilter extends SingleOutputFilterFramework
{
	public void run()
    {
		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		int id;
		double measurement;
		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::ConvertTemperatureFilter Reading ");

		while (true)
		{
			/*************************************************************
			*	Here we read a byte and write a byte
			*************************************************************/

			try
			{
				id = ReadIDFromFilterInputPort();
				measurement = Double.longBitsToDouble(ReadMeasurementFromFilterInputPort());
				bytesread+=12;
				if(id == TEMPERATURE){
						measurement = (measurement-32)/1.8;
				}
				writeInt(id);
				writeDouble(measurement);
				byteswritten+=12;
			} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.println( "\n" + this.getName() + "::ConvertTemperatureFilter Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run

} // MiddleFilter
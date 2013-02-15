package gizmoe.architecture.system2;
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
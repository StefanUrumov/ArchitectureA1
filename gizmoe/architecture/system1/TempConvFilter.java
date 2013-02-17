/******************************************************************************************************************
* File:
* Course: 17655
* Project: Assignment 1
* Versions:
*  1.1 15 February 2013 - Temperature conversion Filter code (ss).
*
* Description:
*
* This class filter will read bytes from the stream. Check for the measurement "temperature",
*  convert it from fahrenheit to celsius and write all the bytes read from input to output.
*
* Parameters:  
*double measurement -	stores the measurement read from the port
*
* Internal Methods: 
* public void run()
* 
* Main author/Owner: 
* Sindhu Satish
*
******************************************************************************************************************/
package gizmoe.architecture.system1;
public class TempConvFilter extends SingleOutputFilterFramework
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

				//here we check if the id read is related to temperature, convert temperature to celsius and write to output port
				if(id == TEMPERATURE){
						measurement = getTempInCel(measurement);
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

	// This method converts temperature from Fahrenheit to Celsius
	public static double getTempInCel(double temperatureF){
		return ((temperatureF - 32) * 5 / 9);
		}
} // TempConvFilter
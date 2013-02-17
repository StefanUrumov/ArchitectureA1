/******************************************************************************************************************
* File:
* Course: 17655
* Project: Assignment 1
* Versions:
*  1.1 15 February 2013 - Altitude conversion filter code (ss).
*
* Description:
*
* This class filter will read bytes from the stream. Check for the measurement "altitude1" and "altitude2",
*  converts them from feet to meters and writes all the bytes read from input to output.
*
* Parameters:  
* 	 double measurement -	stores the measurement read from the port
* Internal Methods: 
* public void run()
* 
* Main author/Owner: 
* Sindhu Satish
*
******************************************************************************************************************/
package gizmoe.architecture.system1;
public class AltConvFilter extends SingleOutputFilterFramework
{
	public void run()
    {
		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		int id;
		double measurement;
		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::ConvertAltitudeFilter Reading ");

		while (true)
		{
			try
			{
				id = ReadIDFromFilterInputPort();
				measurement = Double.longBitsToDouble(ReadMeasurementFromFilterInputPort());
				bytesread+=12;

				////here we check if the id read is related to altitude, convert it to meters and write to output port
				if(id == ALTITUDE){
						measurement = getAltInMet(measurement);;
				}
				writeInt(id);
				writeDouble(measurement);
				byteswritten+=12;
			} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.println( "\n" + this.getName() + "::ConvertAltitudeFilter Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run

	//This method converts Altitude from feet to meters
	public static double getAltInMet(double alt){
		return ((alt * 0.3048));
		}

} // AltConvFilter


package gizmoe.architecture.system2;
/******************************************************************************************************************
* File:WriteToFileFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 8th February 2013 - Initial code (ukd)
*
* Description:
*
* This class will create a filter to write to predefined file. It print a header, and then will print the 
* formatted values from the datastream. Remember to change the header and data to write to suit your needs. 
* Currently prints Time, Temperature and Altitude.
*
* Parameters: 	None
*
* Internal Methods: 
* 	run()
* 
* Main author/Owner: 
* 	Upsham K Dawra
*
******************************************************************************************************************/
import java.util.*;						// This class is used to interpret time words
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class WriteWildValuesToFileFilter extends SingleOutputFilterFramework
{
	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value so that it can be easily printed
		*	to the terminal.
		*************************************************************************************/

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");

		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		String pressureFrameBuffer = "";
		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::WildValueWriter Reading ");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("WildPoints.dat"));
			out.write("Time:\t\t\t\t\t\t    Pressure(psi):");
			out.newLine();
			out.write("---------------------------------------------------------"
					+"----------------------------------------------------------");
			while (true)
			{
				try
				{
					id = ReadIDFromFilterInputPort();

					measurement = ReadMeasurementFromFilterInputPort();
					bytesread+=12;

					if ( id == TIME )
					{
						out.newLine();
						TimeStamp.setTimeInMillis(measurement);
						out.write(TimeStampFormat.format(TimeStamp.getTime())+"\t\t");
					} // if
					else if(id == PRESSURE){
						if(Double.longBitsToDouble(measurement) > 0){
							pressureFrameBuffer = String.format("%10s", String.format("%4.5f",Double.longBitsToDouble(measurement))).replace(' ', '0').replace('.',':');
							out.write(pressureFrameBuffer);
						}else{
							pressureFrameBuffer = String.format("%10s", String.format("%4.5f",Double.longBitsToDouble(measurement))).replace('.',':');
							out.write(pressureFrameBuffer);
						}
					}

				} // try
				catch (EndOfStreamException e)
				{
					ClosePorts();
					out.close();
					System.out.print( "\n" + this.getName() + "::WildValueWriter Exiting; bytes read: " + bytesread );
					break;

				} // catch

			} // while
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

   } // run

} // SingFilter
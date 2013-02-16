package gizmo.architecture.system3;
/******************************************************************************************************************
* File:SourceFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for how to use the SourceFilterTemplate to create a source filter. This particular
* filter is a source filter that reads some input from the FlightData.dat file and writes the bytes up stream.
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/

import java.io.*; // note we must add this here since we use BufferedReader class to read from the keyboard

//import FilterFramework.EndOfStreamException;

public class SourceFilter extends SingleOutputFilterFramework
{
//	private static final int ERROR_DATA = -10;

	public void run()
    {
		String fileName1 = "SubSetA.dat";	// Input data file.
		String fileName2 = "SubSetB.dat";	// Input data file.

		int bytesread1 = 0;					// Number of bytes read from the input file.
		int byteswritten1 = 0;				// Number of bytes written to the stream.
		DataInputStream in1 = null;			// File stream reference.
		
		int bytesread2 = 0;					// Number of bytes read from the input file.
		int byteswritten2 = 0;				// Number of bytes written to the stream.
		DataInputStream in2 = null;			// File stream reference.

		long measurement1 = Long.MAX_VALUE;				// This is the word used to store all measurements - conversions are illustrated.
		int id1 = ERROR_DATA;							// This is the measurement id

		long measurement2 = Long.MAX_VALUE;				// This is the word used to store all measurements - conversions are illustrated.
		int id2 = ERROR_DATA;							// This is the measurement id
		
		boolean read1 = true;
		boolean read2 = true;

		try
		{
			/***********************************************************************************
			*	Here we open the file and write a message to the terminal.
			***********************************************************************************/
			in1 = new DataInputStream(new FileInputStream(fileName1));
			System.out.println("\n" + this.getName() + "::Source reading file..." + fileName1);

			in2 = new DataInputStream(new FileInputStream(fileName2));
			System.out.println("\n" + this.getName() + "::Source reading file..." + fileName2);

			/***********************************************************************************
			*	Here we read the data from the file and send it out the filter's output port one
			* 	byte at a time. The loop stops when it encounters an EOFExecption.
			***********************************************************************************/

			while(true)
			{
				// Read from file1
				if (read1) {
					// Get the ID from file1
					id1 = readID(fileName1, in1, bytesread1, byteswritten1);
					
					if (id1 != ERROR_DATA) {
						bytesread1 += 4;

						if (id1 != 0) {
							//writeInt(id1);
							//byteswritten1 += 4;
						}
						
						// Get value from the extracted token.
						measurement1 = getData(fileName1, in1, bytesread1, byteswritten1);

						if (measurement1 != Long.MAX_VALUE) {
							bytesread1 += 8;
					
							if (id1 != 0) {
								writeInt(id1);
								byteswritten1 += 4;
								writeLong(measurement1);
								byteswritten1 += 8;
							}
						} else {
							// EOF found for file1.
							break;
						}
					} else {
						// EOF found for file1
						break;
					}
				}
 
				// Read from file2 and follow the same steps as above.
				if (read2) {
					id2 = readID(fileName2, in2, bytesread2, byteswritten2);
						
					if (id2 != ERROR_DATA) {
						bytesread2 += 4;
						
						if (id2 != 0) {
							//writeInt(id2);
							//byteswritten2 += 4;
						}

						measurement2 = getData(fileName2, in2, bytesread2, byteswritten2);

						if (measurement2 != Long.MAX_VALUE) {
							bytesread2 += 8;

							if (id2 != 0) {
								writeInt(id2);
								byteswritten2 += 4;
								writeLong(measurement2);
								byteswritten2 += 8;
							}
						} else {
							// EOF found for file2.
							break;
						}
					} else {
						// EOF found for file2.
						break;
					}
				}

				// Compare the time stamp
				if (id1 == 0 && id2 == 0) {
					if (measurement1 <= measurement2) {
						writeInt(id1);
						writeLong(measurement1);
						byteswritten1 += 12;
						read1 = true;
						read2 = false;
					} else {
						writeInt(id2);
						writeLong(measurement2);
						byteswritten2 += 12;
						read1 = false;
						read2 = true;
					}
				}
			} // while

			// Done with file1 but file2 still need to be read.
			if (id1 != ERROR_DATA || measurement1 != Long.MAX_VALUE) {
				// Make sure already read data from file2 is passed to stream.
				if (id2 == 0 && measurement2 != Long.MAX_VALUE) {
					writeInt(id2);
					writeLong(measurement2);
					byteswritten2 += 12;
				}

				// Now just pass the file2 till it EOF is found.
				if (id2 != ERROR_DATA && measurement2 != Long.MAX_VALUE) {
					try 
					{
						while (true) {
							byte databyte = in2.readByte();
							bytesread2++;
							WriteFilterOutputPort(databyte);
							byteswritten2++;
						}
					} // try
	
					catch (EOFException eoferr)
					{
						in2.close();
						ClosePorts();
						System.out.print( "\n" + this.getName() + "::File" + fileName2 + " reading also done::Source Exiting; bytes read: " + bytesread2 + " bytes written: " + byteswritten2 );
					} // catch
				}
			}

			// Done with file2 but file1 is still need to be read.
			if (id2 != ERROR_DATA || measurement2 != Long.MAX_VALUE) {
				// Make sure already read data from file1 is passed to stream.
				if (id1 == 0 && measurement1 != Long.MAX_VALUE) {
					writeInt(id1);
					writeLong(measurement1);
					byteswritten1 += 12;
				}

				// Now just pass the file1 till it EOF is found.
				if (id1 != ERROR_DATA && measurement1 != Long.MAX_VALUE) {
					try
					{
						while (true) {
								byte databyte = in1.readByte();
								bytesread1++;
								WriteFilterOutputPort(databyte);
								byteswritten1++;
						}
					} // try
	
					catch (EOFException eoferr)
					{
						in1.close();
						ClosePorts();
						System.out.print( "\n" + this.getName() + "::File" + fileName1 + " reading also done::Source Exiting; bytes read: " + bytesread1 + " bytes written: " + byteswritten1 );
					} // catch
				}
			}
		} //try

		/***********************************************************************************
		*	The following exception is raised should we have a problem openinging the file.
		***********************************************************************************/
		catch ( IOException iox )
		{
			System.out.println("\n" + this.getName() + "::Problem reading input data file::" + iox );

		} // catch
   } // run
	
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
				measurement = Long.MAX_VALUE;
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
} // SourceFilter
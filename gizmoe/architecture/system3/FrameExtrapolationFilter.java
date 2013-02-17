package gizmo.architecture.system3;
/******************************************************************************************************************
* File:WildValueExtrapolationFilter.java
* Course: 17655
* Project: Assignment 1
* Versions:
*	1.0 7 February 2013.
*
* Description:
*
* This class will Filter out all measurements where: attitude exceeds 10 degrees and pressure exceeds 65 PSI
* and write them to another file. Considerring the invalid data frame are those which violate both pressure
* and attitude limit here is strategy for extrapolation:
* 1) If last-valid and next-valid data frames are found and average values of pressure and attitude do not exceed their
*    limit values it will replace them with an average values.
* 2) If average value is greater then we use the lower value for extrapolation of invalid value.
* 3) If only last-valid data frame is found then that is used for extrapolation.
* 4) If only next-valid data frame is found then that is used for extrapolation.
* 5) If both last-valid and next-valid data frames are found then we use limits values for extrapolation. 
*
* Parameters: 		 
* 	int PressureLimit - The higher bound for pressure values.
* 	int AttitudeLimit - The higher bound attitude values.
*
* Internal Methods: 
* 	public void run()
* 
* Main author/Owner: 
* 	Ashutosh Pandey
*
******************************************************************************************************************/
import java.util.ArrayDeque;

public class FrameExtrapolationFilter extends MultiOutputFilterFramework
{
	private static int PressureLimit = 65;
	private static int AttitudeLimit = 10;

	private ArrayDeque<Bundle> buffer = new ArrayDeque<Bundle>();

	// Methods used to write a single data frame.
	public int writeToPort(Bundle pack) {
		int bytesWritten = 0;
		writeInt(TIME, 1);
		writeLong(pack.time, 1);
		bytesWritten+=12;

		if (pack.velocity != Double.MAX_VALUE) {
			writeInt(VELOCITY, 1);
			writeDouble(pack.velocity, 1);
			bytesWritten+=12;
		}

		if (pack.altitude != Double.MAX_VALUE) {
			writeInt(ALTITUDE, 1);
			writeDouble(pack.altitude, 1);
			bytesWritten+=12;
		}

		if (pack.pressure != Double.MAX_VALUE) {
			writeInt(PRESSURE, 1);
			writeDouble(pack.pressure, 1);

			bytesWritten+=12;
		}

		if (pack.temperature != Double.MAX_VALUE) {
			writeInt(TEMPERATURE, 1);
			writeDouble(pack.temperature, 1);
			bytesWritten+=12;
		}

		if (pack.attitude != Double.MAX_VALUE) {
			writeInt(ATTITUDE, 1);
			writeDouble(pack.attitude, 1);
			bytesWritten+=12;
		}

		return bytesWritten;
	}

	// Methods used to read data frames from buffer and write them to output port.
	public int writeToPort(double latestValidPressure,double latestValidAttitude, double lastValidPressure,double lastValidAttitude) {
		int bytesWritten = 0;
		double avgPresssure = PressureLimit;
		double avgAttitude = AttitudeLimit;
		
		if (buffer.isEmpty()) {
			System.out.println(" Internal ERROR: Buffer should not be empty.");
		}


		if (lastValidPressure != Double.MAX_VALUE && latestValidPressure != Double.MAX_VALUE) {
			avgPresssure = (latestValidPressure + lastValidPressure)/2;
		} else if (lastValidPressure != Double.MAX_VALUE) {
			avgPresssure = lastValidPressure;
		} else  if (latestValidPressure != Double.MAX_VALUE) {
			avgPresssure = latestValidPressure;
		}

		if (avgPresssure > PressureLimit) {
			avgPresssure = (lastValidPressure < latestValidPressure) ? lastValidPressure : latestValidPressure;
		}
			
		if (lastValidAttitude != Double.MAX_VALUE && latestValidAttitude != Double.MAX_VALUE) {
			avgAttitude = (latestValidAttitude + lastValidAttitude)/2;
		} else if (lastValidAttitude != Double.MAX_VALUE) {
			avgAttitude = lastValidAttitude;
		} else if (latestValidAttitude != Double.MAX_VALUE) {
			avgAttitude = latestValidAttitude;
		}

		if (avgAttitude > AttitudeLimit) {
			avgAttitude = (lastValidAttitude < latestValidAttitude) ? lastValidAttitude : latestValidAttitude;
		}
			

		while(!buffer.isEmpty()) {
			Bundle pack = buffer.remove();
			writeInt(TIME, 1);
			writeLong(pack.time, 1);
			bytesWritten+=12;

			if (pack.velocity != Double.MAX_VALUE) {
				writeInt(VELOCITY, 1);
				writeDouble(pack.velocity, 1);
				bytesWritten+=12;
			}

			if (pack.altitude != Double.MAX_VALUE) {
				writeInt(ALTITUDE, 1);
				writeDouble(pack.altitude, 1);
				bytesWritten+=12;
			}

			if (pack.pressure != Double.MAX_VALUE) {
				if (pack.needToExtrapolate()) {
					writeInt(PRESSURE + CORRECTION_OFFSET, 1);
					System.out.println("\n EXTRAPOLATED: Pressure " + pack.pressure +" To "+ avgPresssure);
					writeDouble(avgPresssure, 1);
				} else {
					if (pack.dirtyPressure) {
						writeInt(PRESSURE + CORRECTION_OFFSET, 1);
					} else {
						writeInt(PRESSURE, 1);
					}
				}

				bytesWritten+=12;
				
				// Write the invalid pressure.
				writeInt(TIME, 2);
				writeLong(pack.time, 2);
				writeInt(PRESSURE + CORRECTION_OFFSET, 2);
				writeDouble(pack.pressure, 2);
			}

			if (pack.temperature != Double.MAX_VALUE) {
				writeInt(TEMPERATURE, 1);
				writeDouble(pack.temperature, 1);
				bytesWritten+=12;
			}

			if (pack.attitude != Double.MAX_VALUE) {
				if (pack.needToExtrapolate()) {
					writeInt(ATTITUDE + CORRECTION_OFFSET, 1);
					System.out.println("\n EXTRAPOLATED: Altitude " + pack.attitude +" To "+ avgAttitude + "\n");
					writeDouble(avgAttitude, 1);
				} else {
					writeInt(ATTITUDE, 1);
				}

				bytesWritten+=12;
				
				// Write the invalid attitude.
				writeInt(ATTITUDE + CORRECTION_OFFSET, 2);
				writeDouble(pack.attitude, 2);

			}
		}
		//System.out.print("Ashutosh Bytes written: "+ bytesWritten + "\n");

		return bytesWritten;
	}

	public void run() {
		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		int id = -1;
		double measurement = Double.MAX_VALUE;
		double lastValidPressure = Double.MAX_VALUE;
		double lastValidAttitude = Double.MAX_VALUE;
		double latestValidPressure = Double.MAX_VALUE;
		double latestValidAttitude = Double.MAX_VALUE;

		boolean needToExtrapolate = false;
		// Next we write a message to the terminal to let the world know we are alive...
		Bundle currentFrame =  new Bundle();

		System.out.print( "\n" + this.getName() + "::WildValueExtrapolationFilter Reading ");

		try {
			while (true)
			{
				/*************************************************************
				*	Here we read a byte and write a byte
				*************************************************************/

				id = ReadIDFromFilterInputPort();
				bytesread+=4;
				measurement = Double.longBitsToDouble(ReadMeasurementFromFilterInputPort());
				bytesread+=8;

				if (id == 0 && currentFrame.needToExtrapolate()) {
					needToExtrapolate = true;
				}

				switch (id) {
				case 0: if (needToExtrapolate) {
					// Buffer has some data bundles that need to be extrapolated.
					if (currentFrame.pressure != Double.MAX_VALUE && currentFrame.attitude != Double.MAX_VALUE
							&& (currentFrame.pressure < PressureLimit || currentFrame.attitude < AttitudeLimit)) {
						// A valid data bundle with valid pressure and altitude value found.
						latestValidPressure = currentFrame.pressure;
						latestValidAttitude = currentFrame.attitude;

						// clear the buffer
						byteswritten += writeToPort(latestValidPressure, latestValidAttitude,
								lastValidPressure, lastValidAttitude);

						// FLush the valid value.
						byteswritten += writeToPort(currentFrame);

						// Reset the variables.
						needToExtrapolate = false;
						lastValidPressure = latestValidPressure;
						lastValidAttitude = latestValidAttitude;
						latestValidPressure = Double.MAX_VALUE;
						latestValidAttitude = Double.MAX_VALUE;
					} else {
						// Invalid data bundle found. 
						// Push to buffer.
						buffer.add(currentFrame.clone());
					}
				} else {
					// There is nothing in buffer and a valid data bundle found.
					if (currentFrame.pressure != Double.MAX_VALUE && currentFrame.attitude != Double.MAX_VALUE) {
						lastValidPressure = currentFrame.pressure;
						lastValidAttitude = currentFrame.attitude;
					}

					if (currentFrame.isValid()) {
						// Write to output as it is.
						byteswritten += writeToPort(currentFrame);
					}
				}

				currentFrame.setDefault();
				currentFrame.time = Double.doubleToLongBits(measurement);
				break;
				case 1: currentFrame.velocity = measurement;break;
				case 2:	currentFrame.altitude = measurement;break;
				case 3: currentFrame.pressure = measurement;break;
				case 13:currentFrame.pressure = measurement; currentFrame.dirtyPressure = true; break;
				case 4:	currentFrame.temperature = measurement;break;
				case 5: currentFrame.attitude = measurement;break;
				} // switch
			} // while
		} // try

		catch (EndOfStreamException e) {
			if (currentFrame.isValid()) {
				if (currentFrame.needToExtrapolate()) {
					// Last frame is invalid
					buffer.add(currentFrame.clone());
					needToExtrapolate = true;
					byteswritten += writeToPort(latestValidPressure, latestValidAttitude, lastValidPressure, lastValidAttitude);
				} else {
					// Last frame is valid.
					// First clean the buffer
					latestValidPressure = currentFrame.pressure;
					latestValidAttitude = currentFrame.attitude;

					byteswritten += writeToPort(latestValidPressure, latestValidAttitude, lastValidPressure, lastValidAttitude);

					// Now write the last valid frame.
					byteswritten += writeToPort(currentFrame);
				}
			}

			System.out.print("Ashutosh Correct value bytes: Read = " + bytesread + "   Written = " + byteswritten +"\n");
		}
		
		
		
   } // run
	private static final class Bundle {
		Bundle() {
			time = -1;
			velocity = Double.MAX_VALUE;
			altitude = Double.MAX_VALUE;
			pressure = Double.MAX_VALUE;
			temperature = Double.MAX_VALUE;
			attitude = Double.MAX_VALUE;
			dirtyPressure = false;
		}

		public long time;
		public double velocity;
		public double altitude;
		public double pressure;
		public double temperature;
		public double attitude;
		public boolean dirtyPressure;

		// Hard coded ?????? I hate Java. 
		public boolean needToExtrapolate() {
			return (this.isValid() && pressure > PressureLimit && attitude > AttitudeLimit);
		}
		public boolean isValid() {
			return (time != -1 || velocity != Double.MAX_VALUE || altitude != Double.MAX_VALUE || pressure != Double.MAX_VALUE || temperature != Double.MAX_VALUE || attitude != Double.MAX_VALUE);
		}
		public void setDefault() {
			time = -1;
			velocity = Double.MAX_VALUE;
			altitude = Double.MAX_VALUE;
			pressure = Double.MAX_VALUE;
			temperature = Double.MAX_VALUE;
			attitude = Double.MAX_VALUE;
			dirtyPressure = false;
		}
		public Bundle clone() {
			Bundle dummy = new Bundle();
			dummy.time = time;
			dummy.velocity = velocity;
			dummy.altitude = altitude;
			dummy.pressure = pressure;
			dummy.temperature = temperature;
			dummy.attitude = attitude;
			dummy.dirtyPressure = dirtyPressure;
	
			return dummy;
		}
	}
} // MiddleFilter
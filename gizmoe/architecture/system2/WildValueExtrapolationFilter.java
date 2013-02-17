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
import java.util.ArrayDeque;

public class WildValueExtrapolationFilter extends MultiOutputFilterFramework
{
	private final int WildLowBound = 50;
	private final int WildHighBound = 80;
	private ArrayDeque<Bundle> buffer = new ArrayDeque<Bundle>();
	private final int frameSize = 4;
	public void run()
    {
		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		int byteswrittentoport2 = 0;				// Number of bytes written to the stream.
		int id = -1;
		double measurement = -1;
		String lastvalid = null;
		boolean needToExtrapolate = false;
		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::WildValueExtrapolationFilter Reading ");

		while (true)
		{
			/*************************************************************
			*	Here we read a byte and write a byte
			*************************************************************/

			try
			{
				id = ReadIDFromFilterInputPort();
				bytesread+=4;
				measurement = Double.longBitsToDouble(ReadMeasurementFromFilterInputPort());
				bytesread+=8;
				if(id == PRESSURE){
					if(measurement > WildHighBound || measurement < WildLowBound){
						needToExtrapolate = true;
						buffer.add(new Bundle(id, measurement));
					}else{
						//System.out.println("Pressure filter: Valid Measurement - "+measurement);
						if(needToExtrapolate){
							while(!buffer.isEmpty()){
								Bundle pack = buffer.remove();
								if(pack.id == PRESSURE){
									if(lastvalid == null){
										writeInt(pack.id + CORRECTION_OFFSET, 1);
										writeDouble(measurement, 1);
										byteswritten+=12;
										writeInt(pack.id, 2);
										writeDouble(pack.measurement, 2);
										byteswrittentoport2+=12;
										//System.out.println(" measurement: "+pack.measurement +" bytes: "+byteswritten);
									}else{
										writeInt(pack.id + CORRECTION_OFFSET, 1);
										writeDouble((measurement + Double.parseDouble(lastvalid))/2, 1);
										byteswritten+=12;
										writeInt(pack.id, 2);
										writeDouble(pack.measurement, 2);
										byteswrittentoport2+=12;
										//System.out.println(" measurement: "+pack.measurement +" bytes: "+byteswritten);
									}
								}else{
									writeInt(pack.id, 1);
									writeDouble(pack.measurement, 1);
									byteswritten+=12;
									if(pack.id == TIME && buffer.size() > frameSize){
										writeInt(pack.id, 2);
										writeDouble(pack.measurement, 2);
										byteswrittentoport2+=12;
									}
									//System.out.println(" measurement: "+pack.measurement +" bytes: "+byteswritten);
								}
							}
						}else{
							while(!buffer.isEmpty()){
								Bundle pack = buffer.remove();
								//System.out.print("Correct value bytes: "+byteswritten+" id: "+pack.id);
								writeInt(pack.id, 1);
								writeDouble(pack.measurement, 1);
								byteswritten+=12;
								//System.out.println(" measurement: "+pack.measurement +" bytes: "+byteswritten);
							}
						}
						writeInt(id, 1);
						writeDouble(measurement, 1);
						byteswritten+=12;
						lastvalid = Double.toString(measurement);
						needToExtrapolate = false;
					}
				}else{
					buffer.add(new Bundle(id, measurement));
					/*writeInt(id);
					writeDouble(measurement);
					byteswritten+=12;*/
				}
			} // try

			catch (EndOfStreamException e)
			{
				if(needToExtrapolate){
					while(!buffer.isEmpty()){
						Bundle pack = buffer.remove();
						//System.out.print("bytes: "+byteswritten+" id: "+pack.id);
						if(pack.id == PRESSURE){
							if(lastvalid == null){
								writeInt(pack.id + CORRECTION_OFFSET,1);
								writeDouble(WildLowBound+1,1);
								byteswritten+=12;
								writeInt(pack.id, 2);
								writeDouble(pack.measurement, 2);
								byteswrittentoport2+=12;
								//System.out.println(" measurement: "+pack.measurement +" bytes: "+byteswritten);
							}else{
								writeInt(pack.id + CORRECTION_OFFSET,1);
								writeDouble(Double.parseDouble(lastvalid),1);
								byteswritten+=12;
								writeInt(pack.id, 2);
								writeDouble(pack.measurement, 2);
								byteswrittentoport2+=12;
								//System.out.println(" measurement: "+pack.measurement +" bytes: "+byteswritten);
							}
						}else{
							writeInt(pack.id,1);
							writeDouble(pack.measurement,1);
							byteswritten+=12;
							if(pack.id == TIME){
								writeInt(pack.id, 2);
								writeDouble(pack.measurement, 2);
								byteswrittentoport2+=12;
							}
							//System.out.println(" measurement: "+pack.measurement +" bytes: "+byteswritten);
						}
					}
				}
				while(!buffer.isEmpty()){
					Bundle pack = buffer.remove();
					writeInt(pack.id, 1);
					writeDouble(pack.measurement, 1);
					byteswritten+=12;
				}
				ClosePorts();
				System.out.println( "\n" + this.getName() + "::WildValueExtrapolationFilter Exiting; bytes read: " + bytesread + " bytes written to port 1: " + byteswritten + " bytes written to port 2: " + byteswrittentoport2);
				break;

			} // catch

		} // while

   } // run
	private static class Bundle {
		private int id;
		private double measurement;
		
		public Bundle(int id, double measurement){//Usual constructor
			this.id = id;
			this.measurement = measurement;
		}
	}
} // MiddleFilter
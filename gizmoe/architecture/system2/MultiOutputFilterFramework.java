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
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;


public class MultiOutputFilterFramework extends Connector{
	protected PipedOutputStream OutputWritePort2 = new PipedOutputStream();
	
	private static final int MAX_PORTS = 2;
	void WriteFilterOutputPort(byte datum, int port)
	{
		try
		{
			switch(port){
			case 1:
				OutputWritePort.write((int) datum );
			   	OutputWritePort.flush();
			   	break;
			case MAX_PORTS:
				OutputWritePort2.write((int) datum );
			   	OutputWritePort2.flush();
			   	break;
			}

		} // try

		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch

		return;

	} // WriteFilterPort
	
	
	void ClosePorts()
	{
		try
		{
			InputReadPort.close();
			OutputWritePort.close();
			OutputWritePort2.close();

		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch

	} // ClosePorts
	void writeInt(int data, int port) {
		try{
			byte[] bytes = ByteBuffer.allocate(4).putInt(data).array();
			for(byte bite:bytes){
					WriteFilterOutputPort(bite, port);
			}
		}//try
		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch

		return;
	}

	void writeDouble(double data, int port){
		try{
			byte[] bytes = ByteBuffer.allocate(8).putDouble(data).array();
			for(byte bite:bytes){
				WriteFilterOutputPort(bite, port);
			}
		}//try
		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch
	}
	
	void writeLong(long data, int port) {
		try{
			byte[] bytes = ByteBuffer.allocate(8).putLong(data).array();
			for(byte bite:bytes){
				WriteFilterOutputPort(bite, port);
			}
		}//try
		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch
	}
	
}

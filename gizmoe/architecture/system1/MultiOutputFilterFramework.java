/********************************************************************************************************
* File:MultiOutputFilterFramework.java
* Course: 17655
* Project: Assignment 1
* Versions:
*	1.0 15 February 2013 - Initial code (ukd).
*
* Description:
*
* The class defining multiple output port type filters. Currently, two ports are implemented, but the same
* format can be used for more. Only need to modify the ClosePorts() and WriteFilterOutputPort() methods if
* new ports are added!
*
* Main author/Owner: 
* 	Upsham K Dawra
*
*********************************************************************************************************/
package gizmoe.architecture.system1;
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

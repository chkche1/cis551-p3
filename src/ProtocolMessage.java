import java.io.Serializable;


public class ProtocolMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MessageType type;
	private byte[] message;
	private byte[] signature;
	
	public ProtocolMessage(MessageType type){
		this.type = type;
	}
	
	public ProtocolMessage(MessageType type, byte[] msg){
		this.type = type;
		this.message = msg;
	}
	
	public ProtocolMessage(MessageType type, byte[] msg, byte[] sig){
		this.type = type;
		this.message = msg;
		this.signature = sig;
	}
	
	public MessageType getMessageType(){
		return this.type;
	}
	
	public byte[] getMessage(){
		return this.message;
	}
	
	public byte[] getSignature(){
		return this.signature;
	}
	
}

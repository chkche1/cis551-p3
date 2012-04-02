
public class ProtocolMessage {
	private MessageType type;
	private byte[] message;
	
	public ProtocolMessage(MessageType type){
		this.type = type;
	}
	
	public ProtocolMessage(MessageType type, byte[] msg){
		this.type = type;
		this.message = msg;
	}
	
	public MessageType getMessageType(){
		return this.type;
	}
	
	public byte[] getMessage(){
		return this.message;
	}
	
}

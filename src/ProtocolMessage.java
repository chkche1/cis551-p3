import java.security.*;

public class ProtocolMessage {
	private MessageType type;
	private byte[] message;
	private Integer acctNum;
	private Integer atmNum;
	
	
	public ProtocolMessage(MessageType type, Integer attribute){
		this.type = type;
		if(attribute.equals(MessageType.INIT)) this.acctNum = attribute;
		else this.atmNum = attribute;
	}
	
	public ProtocolMessage(MessageType type, byte[] msg){
		this.type = type;
		this.message = msg;
	}
	
	public Integer getAtmNum(){
		if(this.type!=MessageType.SESS) return null;
		return this.atmNum;
	}
	
	public Integer getAccoutNum(){
		if(this.type!=MessageType.INIT) return null;
		
		return this.acctNum;
	}
	
	public MessageType getMessageType(){
		return this.type;
	}
	
	public byte[] getMessage(){
		return this.message;
	}
	
}

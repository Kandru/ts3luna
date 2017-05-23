package eu.kandru.luna.i18n;

public class MessageBuilder {

	public static String generatePasswordMessage(String password, String username){
		StringBuilder builder = new StringBuilder();
		builder.append("\nHi ");
		builder.append(username);
		builder.append(",\n");
		builder.append("your login password is [b]");
		builder.append(password);
		builder.append("[/b]");
		return builder.toString();
	}
	
}

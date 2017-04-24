package eu.kandru.luna.i18n;

public class MessageBuilder {

	public static String generatePasswordMessage(String password, String username){
		StringBuilder builder = new StringBuilder();
		builder.append("\nHi ");
		builder.append(username);
		builder.append(",\n");
		builder.append("Your password is ");
		builder.append(password);
		return builder.toString();
	}
	
}

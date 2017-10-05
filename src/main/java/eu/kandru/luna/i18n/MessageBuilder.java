package eu.kandru.luna.i18n;

import java.text.MessageFormat;

public class MessageBuilder {

    private static MessageFormat passwordMessage = new MessageFormat("\nHi {0},\n" +
            "someone, hopefully you, tries to use your identity to log into my webinterface. \n" +
            "If it''s actually you, use[b]{1}[/b] as password. " +
            "If not, just ignore this message.");

	public static String generatePasswordMessage(String password, String username){
        return passwordMessage.format(new Object[]{username, password});
    }
	
}

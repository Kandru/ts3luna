package eu.kandru.luna.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OneTimePasswordGeneratorTest {

	@Autowired
	private OneTimePasswordGenerator passwordGenerator;
	
	private static final int PASSWORDS_TO_CHECK = 10;
	
	@Test
	public void testDifferentPasswords(){
		List<String> passwords = new ArrayList<>();
		for (int i = 0; i < PASSWORDS_TO_CHECK; i++){
			String tmpPw = passwordGenerator.generatePassword();
			assertThat(passwords).doesNotContain(tmpPw);
			passwords.add(tmpPw);
		}
	}
	
}

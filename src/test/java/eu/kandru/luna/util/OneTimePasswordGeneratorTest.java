package eu.kandru.luna.util;

import static org.assertj.core.api.Assertions.assertThat;

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
		String[] passwords = new String[PASSWORDS_TO_CHECK];
		for (int i = 0; i < PASSWORDS_TO_CHECK; i++){
			passwords[i] = passwordGenerator.generatePassword();
		}
		for (int i = 0; i < PASSWORDS_TO_CHECK-1; i++){
			for (int x = i+1; x < PASSWORDS_TO_CHECK; x++){
				assertThat(passwords[i]).isNotEqualTo(passwords[x]);
			}
		}
	}
	
}

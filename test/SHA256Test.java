package test;

import static org.junit.Assert.*;

import org.junit.Test;

import util.SHA256;

/**
 * <b>References</b>
 * <ul>
 * <li><a href="https://github.com/junit-team/junit4/wiki/Assertions">JUnit Assertions</a></li>
 * </ul>
 */
public class SHA256Test {

	@Test
	public void testSHA256() {
		SHA256 sha256 = new SHA256();
		assertEquals(sha256.hashString("hello"), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
	}

}
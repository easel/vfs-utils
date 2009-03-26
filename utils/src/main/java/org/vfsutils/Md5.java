package org.vfsutils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * MD5 calculation utilities.
 * @author kleij - at - users.sourceforge.net
 *
 */
public class Md5 {

	/**
	 * Creates a 32 character hex string of the BigInteger representing the md5 code
	 * A zero will be prepended when necessary.
	 * @param md5
	 * @return a 32 character string
	 */
	public String toString(BigInteger md5) {
		String s = md5.toString(16);
		if (s.length() == 31) {
			s = "0" + s;
		}
		return s;
	}
	
	/**
	 * Parses a hex input string to a BigInteger. The input is assumed to represent an
	 * MD5 checksum.
	 * @param input a hexadecimal input
	 * @return the BigInteger represented by the input
	 * @throws NumberFormatException if the input can not be parsed
	 */
	public BigInteger fromString(String input) throws NumberFormatException {
		return new BigInteger(input, 16);
	}
	
	/**
	 * Calculates the md5 code for the input string. Note that the string
	 * does not represent a file; the string itself is used as input.
	 * @param input the string to calculate the md5 for
	 * @return BigInteger representation of the md5 code 
	 * @throws NoSuchAlgorithmException
	 * @see #toString(BigInteger)
	 */
	public BigInteger calculateMd5(String input) throws FileSystemException {
		
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
							
			digest.update(input.getBytes());
					
			byte[] messageDigest  = digest.digest();
			BigInteger bigInt = new BigInteger(1, messageDigest);
				
			return bigInt;
		} catch (Exception e) {
			throw new FileSystemException(e);
		}
	}
	
	/**
	 * Calculates the md5 code for the content of the given file object
	 * @param file
	 * @return BigInteger representation of the md5 code
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @see #toString(BigInteger)
	 */
	public BigInteger calculateMd5(FileObject file) throws FileSystemException {
		InputStream is = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			byte[] buffer = new byte[8192*1];
			int read = -1;
			
			is = file.getContent().getInputStream();
			
			while( (read = is.read(buffer)) >= 0) {
				digest.update(buffer, 0, read);
			}		
			
			//close the stream and free it
			is.close();
			is = null;
			
			byte[] messageDigest  = digest.digest();
			BigInteger bigInt = new BigInteger(1, messageDigest);
			
			return bigInt;
		} catch (Exception e) {
			throw new FileSystemException(e);
		}
		finally {
			if (is !=null) {
				try {
					is.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
	}
}

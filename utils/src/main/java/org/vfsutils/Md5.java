package org.vfsutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * MD5 calculation utilities.
 * @author kleij - at - users.sourceforge.net
 *
 */
public class Md5 {
	
	/**
	 * Class to contain information from a .md5 file containing
	 * a checksum and optionally a fileName	 
	 */
	public class Md5FileInfo {
		public String fileName = null;
		public String checksum = null;
	}

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
	
	public Md5FileInfo parseMd5File(FileObject md5File) throws FileSystemException {
		Md5FileInfo result = new Md5FileInfo();
		
		if (!md5File.exists()) {
			throw new FileSystemException(new IllegalArgumentException("The MD5 file does not exist"));
		}
		
		//check that the checksumFile is small
		if (md5File.getContent().getSize()>1024) {
			throw new FileSystemException(new IllegalArgumentException("The MD5 file is exceptionally big, aborting..."));
		}
		
		StringBuffer sBuffer = new StringBuffer(128);
		
		InputStreamReader reader  = null;
		try {
			reader = new InputStreamReader(md5File.getContent().getInputStream());
			char[] buffer = new char[128];
			
			int read = -1;
			while ((read = reader.read(buffer))>-1) {
				sBuffer.append(buffer, 0, read);
			}
		}
		catch (IOException e) {
			throw new FileSystemException(e);
		}
		finally {
			if (reader!=null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					//ignore
				}
			}
		}
		
		String fileContent = sBuffer.toString();
		
		Pattern pattern = Pattern.compile("([a-fA-F0-9]+)\\s*(\\S*)\\s*");
		
		Matcher matcher = pattern.matcher(fileContent);
		
		if (matcher.matches()) {

			result.checksum = matcher.group(1);
			
			//second group can be empty
			if (matcher.group(2).length()>0) {
				result.fileName = matcher.group(2);
			}
		}
		else {
			throw new FileSystemException(new IllegalArgumentException("The MD5 file does not contain a valid checksum"));
		}
		
		return result;
	}
}

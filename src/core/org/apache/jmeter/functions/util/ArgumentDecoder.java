package org.apache.jmeter.functions.util;

import org.apache.oro.text.perl.Perl5Util;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ArgumentDecoder {

	private static Perl5Util util = new Perl5Util();
	private static String expression = "s#[\\\\](.)#$1#g";
	
	public static String decode( String s ) {
		return util.substitute( expression, s );
	}

}

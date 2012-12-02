import genj.gedcom.*;
import genj.report.*;
import java.io.*;

/**
 * This report prints the information for all Individuals that were living
 * during a year specified by the user.
 *
 * @author Michael Collins <mikec01b@netscape.net>
 *
 */
public class ReportDates extends Report {

	/**
	 * Set this to 'true' to see extra debugging information.
	 */
	private static final boolean DEBUG = true;
	
	/**
	 * The user is allowed to change the perferred HTML viewer with this,
	 * under the Options tab of the report.
	 */
	public String strBrowser = "iexplore.exe";
	
	/**
	 * The user can specify whether the individual was living during the
	 * specified year (false); or whether the individual died in the specified
	 * year or later (true).
	 */
	public boolean isDeathDate = false;
	
    /**
     * The main entry point for the report
     */
    public void start( Object context ) {

	    println( "Temp dir: " + java.lang.System.getProperty( "java.io.tmpdir", "/tmp/" ));
	    String filename = java.lang.System.getProperty( "java.io.tmpdir", "/tmp/" ) +
	    	"tmp" + java.lang.System.currentTimeMillis() + ".html";
	    println( "Output file: " + filename );
	    
		// Assume we're dealing with the whole GedCom file.
		Gedcom gedcom = (Gedcom)context;
		
		// Ask the user for a year to query.
		String strYear = getValueFromUser( "year", "Year:", new String[0] );
		if( strYear == null ) {
			println( "Dates report aborted." );
			return;
		}
		
		// Get a list of all Individuals defined in the Gedcom file.
		Entity[] indis;
		indis = gedcom.getEntities( Gedcom.INDI, "INDI:NAME" );
		
		OutputStream os = null;
		try {
			os = new FileOutputStream( filename );
		} catch( Exception e ) {
			println( "Caught exception: " + e );
		}
		
		// Report Header.
		String[] tmpargs = { strYear };
		String src = "<HTML><HEAD><TITLE>Dates report, version " + getVersion() +
			"</TITLE></HEAD><BODY>\n" +
			( isDeathDate ? i18n( "hdrDeath", tmpargs ) : i18n( "hdrDuring", tmpargs ) ) +
			i18n( "rptHeader" );
		try {
			os.write( src.getBytes() );
		} catch( Exception e ) {
			println( "Caught exception: " + e );
		}

		PropertyDate dateStart, dateEnd;
		dateStart = new PropertyDate();
		dateEnd = new PropertyDate();
		dateStart.setValue( "1 Jan " + strYear );
		dateEnd.setValue( "31 Dec " + strYear );
		
		// Walk through each individual to see if it's dates fall within the range.
		for( int i = 0; i < indis.length; i++ ) {
			Indi indi = (Indi)indis[i];
			if( DEBUG ) {
				println( "Checking date for " + indi.toString() );
			}
			if( null == indi.getBirthDate()) {
				if( DEBUG ) {
					println( "Skipping date for " + indi.toString() );
					println( "-----" );
				}
				continue;
			}
			if( DEBUG ) {
				println( "dateStart = " + dateStart.toString() );
				println( "dateEnd = " + dateEnd.toString() );
				if( null == indi.getDeathDate() ) {
					println( "getDeathDate = not dead yet..." );
				} else {
					println( "getDeathDate = " + indi.getDeathDate().toString() );
					println( "death comparison = " + dateStart.compareTo(indi.getDeathDate()) );
				}
				println( "-----" );
			}
			if(
			  ( !isDeathDate &&
			    dateEnd.compareTo(indi.getBirthDate()) >= 0 &&
			    ( null == indi.getDeathDate() ||
			      dateStart.compareTo(indi.getDeathDate()) <= 1 ))
			  ||
			  ( isDeathDate &&
			    null != indi.getDeathDate() &&
			    dateStart.compareTo(indi.getDeathDate()) <= 1 )
			  ) {
					String strOut;
					String strNames = "";
					
					Property[] props = indi.getProperties( PropertyName.TAG );
					for( int j = 0; j < props.length; j++ ) {
						if( props[j] instanceof PropertyName ) {
							PropertyName pn = (PropertyName)props[j];
		
							String name = pn.getName();
				
							// The name entry is empty, skip this one.
							if( name == null ) {
								continue;
							}
							
							String[] fmtargs = { name };
							// This is a bit of a hack.  The first name (j == 0) gets a
							// different format than the rest.
							if( j == 0 ) {
								strNames += i18n( "fmtName", fmtargs );
							} else {
								strNames += i18n( "fmtAliases", fmtargs );
							}
						}
					}
				
					
					String[] msgargs = { indi.getId(),
											strNames,
											( null == indi.getBirthDate() ?
											  "(unknown)" :
											  indi.getBirthDate().toString() ),
											( null == indi.getDeathDate() ?
											  "(unknown)" :
											  indi.getDeathDate().toString() )
										};
					
					strOut = i18n( "format", msgargs ) + "\n";
					try {
						os.write( strOut.getBytes() );
					} catch( Exception e ) {
						println( "Caught exception: " + e );
					}
			}
		}
		
		// Report Footer.
		src = i18n( "rptFooter" ) + "\n</BODY></HTML>\n";
		try {
			os.write( src.getBytes() );
			os.close();
			Runtime rt = Runtime.getRuntime();
			String[] cmdline = { strBrowser, "file:/" + filename };
			rt.exec( cmdline );
		} catch( Exception e ) {
			println( "Caught exception: " + e );
		}
		
	}
	
    /**
     * Whether this report uses StandardOut or its own windows/dialogs
     */
    public boolean usesStandardOut() {
      return true;
    }

	/**
	 * Returns the version of this report.
	 */
	public String getVersion() {
		return( i18n( "version" ));
	}

	/**
	 * The name of the report
	 */
	public String getName() {
		return( i18n( "reportname" ));
	}

    /**
     * Information about the report
     */
    public String getInfo() {
      return( i18n( "reportinfo" ));
    }

    /**
     * The author of this report
     */
    public String getAuthor() {
      return( i18n( "author" ));
    }

    /**
     * Whether this report will change the Gedcom data it is running on
     */
    public boolean isReadOnly() {
      return true;
    }
    
}
 


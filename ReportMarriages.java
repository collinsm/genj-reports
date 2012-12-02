import genj.gedcom.*;
import genj.report.*;
import java.io.*;

/**
 * This report prints the information for all marriages (families) defined in the
 * Gedcom file.
 *
 * TODO: 1) Find a way to sort based on Husband's NAME, rather than ID.  (Hint:
 * using getEntities(... "FAM:HUSB:NAME") doesn't work as expected).
 *
 */
public class ReportMarriages extends Report {

	/**
	 * Set this to 'true' to see extra debugging information.
	 */
	private static final boolean DEBUG = false;
	
	/**
	 * The user is allowed to change the perferred HTML viewer with this,
	 * under the Options tab of the report.
	 */
	public String strBrowser = "iexplore.exe";
	
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
		
		// Get a list of all the families in the GedCom file.
		Entity[] fams;
		//fams = gedcom.getEntities( Gedcom.FAM, "FAM:HUSB" );
		fams = gedcom.getEntities( Gedcom.FAM, new MarriageComparator() );
		
		OutputStream os = null;
		try {
			os = new FileOutputStream( filename );
		} catch( Exception e ) {
			println( "Caught exception: " + e );
		}
		
		// Report Header.
		String src = "<HTML><HEAD><TITLE>Marriages report, version " + getVersion() +
			"</TITLE></HEAD><BODY>\n";
		try {
			os.write( src.getBytes() );
		} catch( Exception e ) {
			println( "Caught exception: " + e );
		}

		// Walk through each individual to see if it's name matches.
		for( int i = 0; i < fams.length; i++ ) {
			Fam fam = (Fam)fams[i];
			
			// Will probably want this to be a bit more detailed...
			String famdesc = fam.toString();
			
			// I don't want to see unnamed families here.
			if( famdesc.startsWith( "FAM" )) {
				continue;
			}
			
			// Format and print the line to the report.
			String[] msgargs = { fam.getId(), famdesc };
			String result = i18n( "format", msgargs );
			if( result != null ) {
				src = result + "\n";
				try {
					os.write( src.getBytes() );
				} catch( Exception e ) {
					println( "Caught exception: " + e );
				}
			}
		}
		
		// Report Footer.
		src = "</BODY></HTML>\n";
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
    
    /**
     * Internal class to be used to compare two family objects.  The
     * criteria for comparison is the string value of the family
     * (typically Husband's NAME + Wife's NAME).
     */
    class MarriageComparator implements java.util.Comparator {
	    
	    public int compare( Object a, Object b ) {
		    Fam fam1, fam2;
		    String str1, str2;
		    
		    // These objects better be Fams...
		    fam1 = (Fam)a;
		    fam2 = (Fam)b;

		    str1 = fam1.toString();
		    str2 = fam2.toString();
		    return str1.compareTo( str2 );
	    }	// compare()
	    
    }	// MarriageComparator
}
 


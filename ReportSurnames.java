import genj.gedcom.*;
import genj.report.*;
import java.io.*;

/**
 * This report prompts the user for a surname and prints all the individuals
 * in the Gedcom file that share that name.  All the NAME tags for an
 * individual are checked.  If the individual is a female (it is assumed that
 * only her maiden name is entered), her spouse(s) are checked for their
 * surnames.
 */
public class ReportSurnames extends Report {

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
		
		// Ask the user for a last name to use.
		String lastname = getValueFromUser( "lastnames", "Last Name:", new String[0] );
		if( lastname == null ) {
			println( "Test1 report aborted\n" );
			return;
		}

		// Get a list of all the individuals in the GedCom file.
		Entity[] indis;
		indis = gedcom.getEntities( Gedcom.INDI, "INDI:NAME" );
		
		OutputStream os = null;
		try {
			os = new FileOutputStream( filename );
		} catch( Exception e ) {
			println( "Caught exception: " + e );
		}
		
		String src = "<HTML><HEAD><TITLE>Surnames report, version " + getVersion() +
			"</TITLE></HEAD><BODY>\n";
		try {
			os.write( src.getBytes() );
		} catch( Exception e ) {
			println( "Caught exception: " + e );
		}

		// Walk through each individual to see if it's name matches.
		for( int i = 0; i < indis.length; i++ ) {
			Indi indi = (Indi)indis[i];
			String result = checkNames( indi, lastname );
			if( result != null ) {
				src = result + "\n";
				try {
					os.write( src.getBytes() );
				} catch( Exception e ) {
					println( "Caught exception: " + e );
				}
			}
		}
		
		// return true;

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
	 * Check all the NAME entries for this individual, to determine whether
	 * the last name matches.  If no last names match, and the entity is Female,
	 * check the names of her partners (she may be listed with a maiden name).
	 */
	private String checkNames( Indi indi, String lastname ) {
		Property[] props = indi.getProperties( PropertyName.TAG );
		for( int j = 0; j < props.length; j++ ) {
			if( props[j] instanceof PropertyName ) {
				PropertyName pn = (PropertyName)props[j];
		
				String name = pn.getLastName();
				
				// The name entry is empty, skip this one.
				if( name == null ) {
					continue;
				}
				
				if( name.equals( lastname )) {
					// The name matches - use it.
					String[] msgargs = { indi.getId(),
											indi.getName() };
					
					return( i18n( "format", msgargs ));
				}
			}
		}
		if( indi.getSex() == PropertySex.FEMALE ) {
			Indi[] partners = indi.getPartners();
			if( DEBUG ) {
				println( "  Partners for @{" + indi.getId() + "}@ - " + indi.getName() + ": " );
				for( int q = 0; q < partners.length; q++ ) {
					Indi part = (Indi)partners[q];
					println( "    @{" + part.getId() + "}@ - " + part.getName() + "" );
				}
			}
			for( int p = 0; p < partners.length; p++ ) {
				String result = checkNames( (Indi)partners[p], lastname );
				if( result != null ) {
					String[] msgargs = { indi.getId(),
											indi.getName() };
					return( i18n( "format", msgargs ));
				}
			}
		}
		return( null );
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
 


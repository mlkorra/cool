package net.praqma.clearcase;

import net.praqma.clearcase.api.ListVersionTree;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cwolfgang
 */
public class Label extends Type {

    private static final Logger logger = Logger.getLogger( Label.class.getName() );

    public static final Pattern rx = Pattern.compile( "^(.*)\\b\\s*\\((.*?)\\)$" );

    public Label( String name ) {
        super( name );
    }

    public static List<Label> getLabels( File pathname, Branch branch ) throws CleartoolException, UnableToInitializeEntityException {
        logger.fine( "Get labels from " + pathname );

        List<String> lines = new ListVersionTree().addPathName( pathname.toString() ).execute();

        List<Label> labels = new ArrayList<Label>( lines.size() );

        for( String line : lines ) {
            /* Determine if there is a label */
            Matcher m = Label.rx.matcher( line );
            if( m.find() ) {
                Version v = Version.get( m.group( 1 ) );

                if( branch == null || branch.equals( v.getUltimateBranch() ) ) {
                    labels.add( new Label( m.group( 2 ) ) );
                }
            }
        }

        return labels;
    }

    /**
     * From a string, get a {@link List} of {@link Label}s.
     * @param labelString The label string to look for
     * @return A list of {@link Label} 
     */
    public static List<Label> getLabels( String labelString ) {
        int b = labelString.startsWith( "(" ) ? 1 : 0;
        int e = labelString.endsWith( ")" ) ? labelString.length() - 1 : labelString.length();

        String sub = labelString.substring( b, e );
        String[] ls = sub.split( "," );

        List<Label> labels = new ArrayList<Label>( ls.length );

        for( String l : ls ) {
            labels.add( new Label( l.trim() ) );
        }

        return labels;
    }

    @Override
    public String toString() {
        return "Label " + name;
    }
}

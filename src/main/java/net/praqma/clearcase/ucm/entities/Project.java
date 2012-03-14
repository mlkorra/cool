package net.praqma.clearcase.ucm.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToListProjectsException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Project extends UCMEntity {

	private static Logger logger = Logger.getLogger();

	/* Project specific fields */
	private Stream stream = null;

	/**
	 * Enables inter-project deliveries
	 */
	public static final int POLICY_INTERPROJECT_DELIVER = 1;
	public static final int POLICY_CHSTREAM_UNRESTRICTED = 2;
	public static final int POLICY_DELIVER_REQUIRE_REBASE = 4;
	public static final int POLICY_DELIVER_NCO_DEVSTR = 8;

	Project() {
		super( "project" );
	}

	/**
	 * This method is only available to the package, because only UCMEntity
	 * should be allowed to call it.
	 * 
	 * @return A new Project Entity
	 */
	static Project getEntity() {
		return new Project();
	}

	/* For now, the project implements the Plevel functionality */
	public enum PromotionLevel implements Serializable {
		INITIAL, BUILT, TESTED, RELEASED, REJECTED;
	}

	/**
	 * Given a String, return the corresponding Promotion Level.
	 * 
	 * @param str
	 *            , if not a valid Promotion Level INITAL is returned.
	 * @return A Promotion Level
	 */
	public static PromotionLevel getPlevelFromString( String str ) {
		PromotionLevel plevel = PromotionLevel.INITIAL;

		try {
			plevel = PromotionLevel.valueOf( str );
		} catch( Exception e ) {
			/* Do nothing... */
		}

		return plevel;
	}

	public static PromotionLevel promoteFrom( PromotionLevel plevel ) {
		switch ( plevel ) {
		case INITIAL:
			plevel = PromotionLevel.BUILT;
			break;
		case BUILT:
			plevel = PromotionLevel.TESTED;
			break;
		case TESTED:
			plevel = PromotionLevel.RELEASED;
			break;
		case RELEASED:
			plevel = PromotionLevel.RELEASED;
			break;
		}

		return plevel;
	}

	public static String getPolicy( int policy ) {
		String p = "";
		if( ( policy & POLICY_INTERPROJECT_DELIVER ) > 0 ) {
			p += "POLICY_INTERPROJECT_DELIVER,";
		}

		if( ( policy & POLICY_CHSTREAM_UNRESTRICTED ) > 0 ) {
			p += "POLICY_CHSTREAM_UNRESTRICTED,";
		}

		if( ( policy & POLICY_DELIVER_REQUIRE_REBASE ) > 0 ) {
			p += "POLICY_DELIVER_REQUIRE_REBASE,";
		}

		if( ( policy & POLICY_DELIVER_NCO_DEVSTR ) > 0 ) {
			p += "POLICY_DELIVER_NCO_DEVSTR,";
		}

		if( p.length() > 0 ) {
			p = p.substring( 0, ( p.length() - 1 ) );
		}

		return p;
	}

	public static Project create( String name, String root, PVob pvob, int policy, String comment, Component... mcomps ) throws UnableToCreateEntityException {
		//context.createProject( name, root, pvob, policy, comment, mcomps );

		String cmd = "mkproject" + ( comment != null ? " -c \"" + comment + "\"" : "" ) + " -in " + ( root == null ? "RootFolder" : root ) + " -modcomp ";
		for( Component c : mcomps ) {
			cmd += c.getFullyQualifiedName() + " ";
		}
		if( policy > 0 ) {
			cmd += " -policy " + Project.getPolicy( policy );
		}
		cmd += " " + name + "@" + pvob;

		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not create Project " + root + ": " + e.getMessage(), e, UCMType.CREATION_FAILED );
			throw new UnableToCreateEntityException( Project.class, e );
		}

		return get( name, pvob, true );
	}

	public UCMEntity load() throws UnableToLoadEntityException {
		//context.loadProject( this );
		//String result = strategy.loadProject( project.getFullyQualifiedName() );
		String result = "";

		String cmd = "lsproj -fmt %[istream]Xp " + this;

		try {
			result = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( e );
			throw new UnableToLoadEntityException( this, e );
		}

		logger.debug( "Result: " + result );

		setStream( Stream.get( result ) );

		return this;
	}

	public void setStream( Stream stream ) {
		this.stream = stream;
	}

	public Stream getIntegrationStream() throws UnableToLoadEntityException {
		if( !this.loaded ) load();
		return stream;
	}

	public static List<String> getPromotionLevels() {
		List<String> retval = new ArrayList<String>();
		for( Object o : PromotionLevel.values() ) {
			retval.add( o.toString() );
		}
		return retval;
	}

	public static List<Project> getProjects( PVob pvob ) throws UnableToListProjectsException {
		//return context.getProjects( vob );
		logger.debug( "Getting projects for " + pvob );
		String cmd = "lsproject -s -invob " + pvob.toString();

		List<String> projs = null;

		try {
			projs = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( e.getMessage(), e.getMessage() );
			throw new UnableToListProjectsException( pvob, e );
		}

		logger.debug( projs );

		List<Project> projects = new ArrayList<Project>();
		for( String p : projs ) {
			projects.add( Project.get( p + "@" + pvob ) );
		}

		logger.debug( projects );

		return projects;
	}
	
	
	public List<Component> getModifiableComponents() {
		//List<String> cs = strategy.getModifiableComponents( project.getFullyQualifiedName() );
		String[] cs;
		String cmd = "desc -fmt %[mod_comps]p " + this;
		try {
			cs = Cleartool.run( cmd ).stdoutBuffer.toString().split( "\\s+" );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to modifiable components", e );
		}
		
		List<Component> comps = new ArrayList<Component>();

		for( String c : cs ) {
			comps.add( Component.get( c, pvob, true ) );
		}

		return comps;
	}

	
	
	public static Project get( String name ) {
		return get( name, true );
	}

	public static Project get( String name, PVob pvob, boolean trusted ) {
		if( !name.startsWith( "project:" ) ) {
			name = "project:" + name;
		}
		Project entity = (Project) UCMEntity.getEntity( Project.class, name + "@" + pvob, trusted );
		return entity;
	}

	public static Project get( String name, boolean trusted ) {
		if( !name.startsWith( "project:" ) ) {
			name = "project:" + name;
		}
		Project entity = (Project) UCMEntity.getEntity( Project.class, name, trusted );
		return entity;
	}

}

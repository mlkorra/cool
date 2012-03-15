package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

public class ComponentTest {
    private static Appender app;
    
	@BeforeClass
	public static void startup() {
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
        app = new ConsoleAppender();
        app.setMinimumLevel( LogLevel.DEBUG );
        Logger.addAppender( app );
	}
	
    @AfterClass
    public static void end() {
        Logger.removeAppender( app ); 
       }
    

	@Test
	public void testLoad() throws UCMException {
		Component component = UCMEntity.getComponent( "_System@\\Cool_PVOB", false );

		assertNotNull( component );
	}

	@Test
	public void testLoadNotExists() throws UCMException {
		Component component = UCMEntity.getComponent( "_System_no@\\Cool_PVOB", false );

		assertNotNull( component );
	}

	@Test
	public void testComponent() throws UCMException {
		Component component = UCMEntity.getComponent( "_System@\\Cool_PVOB", false );

		assertNotNull( component );
		assertEquals( "component:_System@\\Cool_PVOB", component.getFullyQualifiedName() );
	}

	@Test
	public void testGetRootDir() throws UCMException {
		Component component = UCMEntity.getComponent( "_System@\\Cool_PVOB", false );

		component.getRootDir();
	}

	@Test
	public void testGetBaselinesStreamZeroSize() throws UCMException {
		Component component = UCMEntity.getComponent( "_System@\\Cool_PVOB", true );
		Stream stream = UCMEntity.getStream( "Server_int@\\Cool_PVOB", true );

		List<Baseline> list = component.getBaselines( stream );

		assertTrue( list.size() == 0 );
	}

	@Test
	public void testGetBaselinesStreamPlevelZeroSize() throws UCMException {
		Component component = UCMEntity.getComponent( "_System@\\Cool_PVOB", true );
		Stream stream = UCMEntity.getStream( "Server_int@\\Cool_PVOB", true );

		List<Baseline> list = component.getBaselines( stream, Plevel.INITIAL );

		assertTrue( list.size() == 0 );
	}

}

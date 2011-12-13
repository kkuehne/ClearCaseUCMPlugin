package net.praqma.hudson.remoting;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.hudson.exception.CCUCMException;
import net.praqma.hudson.scm.CCUCMState.State;
import net.praqma.util.debug.LoggerSetting;
import net.praqma.util.debug.appenders.Appender;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.remoting.Future;
import hudson.remoting.Pipe;

public class RemoteUtil {

	private LoggerSetting loggerSetting;
	
	private Appender app;
	
	public RemoteUtil( LoggerSetting loggerSetting, Appender app ) {
		this.loggerSetting = loggerSetting;
		this.app = app;
	}

	public void completeRemoteDeliver( FilePath workspace, BuildListener listener, State state, boolean complete ) throws CCUCMException {

		try {
			if( workspace.isRemote() ) {
				final Pipe pipe = Pipe.createRemoteToLocal();
				Future<Boolean> i = null;
				i = workspace.actAsync( new RemoteDeliverComplete( state.getBaseline(), state.getStream(), state.getSnapView(), state.getChangeset(), complete, listener, pipe, loggerSetting ) );
				app.write( pipe.getIn() );
				i.get();
			} else {
				Future<Boolean> i = null;
				i = workspace.actAsync( new RemoteDeliverComplete( state.getBaseline(), state.getStream(), state.getSnapView(), state.getChangeset(), complete, listener, null, null ) );
				i.get();
			}
			return;

		} catch( Exception e ) {
			throw new CCUCMException( "Failed to " + ( complete ? "complete" : "cancel" ) + " the deliver: " + e.getMessage() );
		}
	}

	public Baseline createRemoteBaseline( FilePath workspace, BuildListener listener, String baseName, Component component, File view, String username ) throws CCUCMException {

		try {
			if( workspace.isRemote() ) {
				final Pipe pipe = Pipe.createRemoteToLocal();
				Future<Baseline> i = null;
				i = workspace.actAsync( new CreateRemoteBaseline( baseName, component, view, username, listener, pipe, loggerSetting ) );
				app.write( pipe.getIn() );
				return i.get();
			} else {
				Future<Baseline> i = null;
				i = workspace.actAsync( new CreateRemoteBaseline( baseName, component, view, username, listener, null, null ) );
				return i.get();
			}

		} catch( Exception e ) {
			throw new CCUCMException( e.getMessage() );
		}
	}

	public List<Baseline> getRemoteBaselinesFromStream( FilePath workspace, Component component, Stream stream, Plevel plevel, boolean slavePolling ) throws CCUCMException {

		GetRemoteBaselineFromStream t = new GetRemoteBaselineFromStream( component, stream, plevel, null, null );

		try {
			if( slavePolling ) {
				if( workspace.isRemote() ) {
					final Pipe pipe = Pipe.createRemoteToLocal();
					Future<List<Baseline>> i = null;
					i = workspace.actAsync( new GetRemoteBaselineFromStream( component, stream, plevel, pipe, loggerSetting ) );
					app.write( pipe.getIn() );
					return i.get();
				} else {
					Future<List<Baseline>> i = null;
					i = workspace.actAsync( new GetRemoteBaselineFromStream( component, stream, plevel, null, null ) );
					return i.get();
				}
			} else {
				return t.invoke( null, null );
			}

		} catch( Exception e ) {
			throw new CCUCMException( e.getMessage() );
		}
	}

	public List<Stream> getRelatedStreams( FilePath workspace, TaskListener listener, Stream stream, boolean pollingChildStreams, boolean slavePolling ) throws CCUCMException {

		PrintStream out = listener.getLogger();

		try {
			if( slavePolling ) {

				if( workspace.isRemote() ) {
					final Pipe pipe = Pipe.createRemoteToLocal();
					Future<List<Stream>> i = null;
					i = workspace.actAsync( new GetRelatedStreams( listener, stream, pollingChildStreams, pipe, loggerSetting ) );
					app.write( pipe.getIn() );
					return i.get();
				} else {
					Future<List<Stream>> i = null;
					i = workspace.actAsync( new GetRelatedStreams( listener, stream, pollingChildStreams, null, null ) );
					return i.get();

				}
			} else {
				GetRelatedStreams t = new GetRelatedStreams( listener, stream, pollingChildStreams, null, null );
				return t.invoke( null, null );
			}

		} catch( Exception e ) {
			e.printStackTrace( out );
			throw new CCUCMException( e.getMessage() );
		}
	}

	public UCMEntity loadEntity( FilePath workspace, UCMEntity entity, boolean slavePolling ) throws CCUCMException {

		try {
			if( slavePolling ) {
				Future<UCMEntity> i = null;

				if( workspace.isRemote() ) {
					final Pipe pipe = Pipe.createRemoteToLocal();

					i = workspace.actAsync( new LoadEntity( entity, pipe, loggerSetting ) );
					app.write( pipe.getIn() );

				} else {
					i = workspace.actAsync( new LoadEntity( entity, null, null ) );
				}

				return i.get();
			} else {
				LoadEntity t = new LoadEntity( entity, null, null );
				return t.invoke( null, null );
			}

		} catch( Exception e ) {
			throw new CCUCMException( e.getMessage() );
		}
	}

	public String getClearCaseVersion( FilePath workspace, Project project ) throws CCUCMException {

		try {
			Future<String> i = null;

			if( workspace.isRemote() ) {
				final Pipe pipe = Pipe.createRemoteToLocal();

				i = workspace.actAsync( new GetClearCaseVersion( project, pipe, loggerSetting ) );
				app.write( pipe.getIn() );

			} else {
				i = workspace.actAsync( new GetClearCaseVersion( project, null, null ) );
			}

			return i.get();

		} catch( Exception e ) {
			throw new CCUCMException( e.getMessage() );
		}
	}

	public void endView( FilePath workspace, String viewtag ) throws CCUCMException {

		try {

			workspace.act( new EndView( viewtag ) );

		} catch( Exception e ) {
			throw new CCUCMException( e.getMessage() );
		}
	}
}

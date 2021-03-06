package aQute.remote.api;

import java.util.*;
import java.util.regex.*;

import org.osgi.framework.dto.*;

/**
 * An agent runs on remote OSGi framework and provides the means to control this
 * framework. This API can also be used to install a framework before an agent
 * is started. Such a pre-agent is called an Envoy. An Envoy implements
 * {@link #createFramework(String, Collection, Map)} and {@link #isEnvoy()} only
 * but switches to the agent API once the framework is installed.
 */
public interface Agent {

	/**
	 * The default port. The port can be overridden with the System/framework
	 * property {$value {@link #AGENT_SERVER_PORT_KEY}.
	 */
	int		DEFAULT_PORT		= 29998;

	/**
	 * The property key to set the agent's port.
	 */
	String	AGENT_SERVER_PORT_KEY	= "aQute.agent.server.port";

	/**
	 * The pattern for a server port specification: {@code [<interface>:]<port>}
	 * .
	 */
	Pattern	PORT_P					= Pattern.compile("(?:([^:]+):)?(\\d+)");
	/**
	 * The port for attaching to a remote Gogo CommandSession
	 */
	int		COMMAND_SESSION		= -1;

	/**
	 * The port for having no redircet of IO
	 */
	int		NONE				= 0;

	/**
	 * The port for System.in, out, err redirecting.
	 */
	int		CONSOLE				= 1;

	/**
	 * An Envoy is an agent that can install a framework (well, -runpath) and
	 * launch it with an Agent. An envoy can only handle this method and
	 * {@link #createFramework(String, Collection, Map)} so other methods should
	 * not be called. This rather awkward model is necessary so that we do not
	 * have to reconnect to the actual agent.
	 * 
	 * @return true if this is a limited envoy, otherwise true for a true Agent.
	 */
	boolean isEnvoy();

	/**
	 * Get the framework DTO
	 */
	FrameworkDTO getFramework() throws Exception;

	/**
	 * Install a new bundle at the given bundle location. The SHA identifies the
	 * file and should be retrievable through {@link Supervisor#getFile(String)}
	 * .
	 * 
	 * @param location
	 *            the bundle location
	 * @param sha
	 *            the sha of the bundle's JAR
	 * @return A Bundle DTO
	 */
	BundleDTO install(String location, String sha) throws Exception;

	/**
	 * Start a number of bundles
	 * 
	 * @param id
	 *            the bundle ids
	 * @return any errors that occurred
	 */
	String start(long... id) throws Exception;

	/**
	 * Stop a number of bundles
	 * 
	 * @param id
	 *            the bundle ids
	 * @return any errors that occurred
	 */
	String stop(long... id) throws Exception;

	/**
	 * Uninstall a number of bundles
	 * 
	 * @param id
	 *            the bundle ids
	 * @return any errors that occurred
	 */

	String uninstall(long... id) throws Exception;

	/**
	 * Update the bundles in the framework. Each agent compares this map against
	 * a map of installed bundles. The map maps a bundle location to SHA. Any
	 * differences are reflected in the installed bundles. That is, a change in
	 * the SHA will update, a new entry will install, and a removed entry will
	 * uninstall. This is the preferred way to keep the remote framework
	 * synchronized since it is idempotent.
	 * 
	 * @param bundles
	 *            the bundles to update
	 */
	String update(Map<String,String> bundles) throws Exception;

	/**
	 * Redirect I/O from port. Port can be {@link #CONSOLE},
	 * {@link #COMMAND_SESSION}, {@link #NONE}, or a TCP Telnet port.
	 * 
	 * @param port
	 *            the port to redirect from
	 * @return if the redirection was changed
	 */
	boolean redirect(int port) throws Exception;

	/**
	 * Send a text to the potentially redirected stdin stream so that remotely
	 * executing code will read it from an InputStream.
	 * 
	 * @param s
	 *            text that should be read as input
	 * @return true if this was redirected
	 */
	boolean stdin(String s) throws Exception;

	/**
	 * Execute a remote command on Gogo (if present) and return the result.
	 * 
	 * @param cmd
	 *            the command to execute
	 * @return the result
	 */
	String shell(String cmd) throws Exception;

	/**
	 * Get the remote's system's System properties
	 * 
	 * @return the remote systems properties
	 */
	Map<String,String> getSystemProperties() throws Exception;

	/**
	 * This method is only implemented in the Envoy (the pre-Agent). It is meant
	 * to install a -runpath before the framework runs. An Envoy can actally
	 * created multiple independent frameworks. If this framework already
	 * existed, and the given parameters are identical, that framework will be
	 * used for the aget that will take over. Otherwise the current framework is
	 * stopped and a new framework is started.
	 * 
	 * @param name
	 *            the name of the framework
	 * @param runpath
	 *            the runpath the install
	 * @param properties
	 *            the framework properties
	 * @return if this created a new framework
	 */
	boolean createFramework(String name, Collection<String> runpath, Map<String,Object> properties) throws Exception;

	/**
	 * Abort the remote agent. The agent should send an event back and die. This
	 * is an async method.
	 */
	void abort() throws Exception;

	/**
	 * Ping the remote agent to see if it is still alive.
	 */
	boolean ping();
}

/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.client.web.rest;

import org.melior.util.number.Clamp;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
public class RestClientConfig
{
		private String url;

		private String username;

		private String password;

		private int maximumConnections = 1000;

		private int connectionTimeout = 0;

		private int connectAttempts = 1;

		private int requestTimeout = 60000;

		private int backoffPeriod = 0;

		private float backoffPeriodIncrement = 0;

		private int backoffPeriodLimit = 0;

		private int inactivityTimeout = 300000;

		private int pruneInterval = 60000;

	/**
	 * Constructor.
	 */
	public RestClientConfig()
	{
				super();
	}

	/**
	 * Get URL.
	 * @return The URL
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Set URL.
	 * @param url The URL
	 */
	public final void setUrl(
		final String url)
	{
		this.url = url;
	}

	/**
	 * Get user name.
	 * @return The user name
	 */
	public final String getUsername()
	{
		return username;
	}

	/**
	 * Set user name.
	 * @param username The user name
	 */
	public final void setUsername(
		final String username)
	{
		this.username = username;
	}

	/**
	 * Get password.
	 * @return The password
	 */
	public final String getPassword()
	{
		return password;
	}

	/**
	 * Set password.
	 * @param password The password
	 */
	public final void setPassword(
		final String password)
	{
		this.password = password;
	}
 
	/**
	 * Get maximum number of connections.
	 * @return The maximum number of connections
	 */
	public int getMaximumConnections()
	{
		return maximumConnections;
	}

	/**
	 * Set maximum number of connections.
	 * @param maximumConnections The maximum number of connections
	 */
	public void setMaximumConnections(
		final int maximumConnections)
	{
		this.maximumConnections = Clamp.clampInt(maximumConnections, 0, Integer.MAX_VALUE);
	}

	/**
	 * Get connection timeout.
	 * @return The connection timeout
	 */
	public int getConnectionTimeout()
	{
		return (connectionTimeout == 0) ? getRequestTimeout() : connectionTimeout;
	}

	/**
	 * Set connection timeout.
	 * @param connectionTimeout The connection timeout, specified in seconds
	 */
	public void setConnectionTimeout(
		final int connectionTimeout)
	{
		this.connectionTimeout = Clamp.clampInt(connectionTimeout * 1000, 0, Integer.MAX_VALUE);
	}

	/**
	 * Get number of connect attempts.
	 * @return The number of connect attempts
	 */
	public final int getConnectAttempts()
	{
		return connectAttempts;
	}

	/**
	 * Set number of connect attempts.
	 * @param connectAttempts The number of connect attempts
	 */
	public final void setConnectAttempts(
		final int connectAttempts)
	{
		this.connectAttempts = Clamp.clampInt(connectAttempts, 1, Integer.MAX_VALUE);
	}

	/**
	 * Get request timeout.
	 * @return The request timeout
	 */
	public final int getRequestTimeout()
	{
		return requestTimeout;
	}

	/**
	 * Set request timeout.
	 * @param requestTimeout The request timeout, specified in seconds
	 */
	public final void setRequestTimeout(
		final int requestTimeout)
	{
		this.requestTimeout = Clamp.clampInt(requestTimeout * 1000, 0, Integer.MAX_VALUE);
	}

	/**
	 * Get backoff period on connection exception.
	 * @return The backoff period on connection exception
	 */
	public final int getBackoffPeriod()
	{
		return backoffPeriod;
	}

	/**
	 * Set backoff period on connection exception.
	 * @param backoffPeriod The backoff period on connection exception, specified in seconds
	 */
	public final void setBackoffPeriod(
		final int backoffPeriod)
	{
		this.backoffPeriod = Clamp.clampInt(backoffPeriod * 1000, 0, Integer.MAX_VALUE);
	}

	/**
	 * Get backoff period increment.
	 * @return The backoff period increment
	 */
	public float getBackoffPeriodIncrement()
	{
		return backoffPeriodIncrement;
	}

	/**
	 * Set backoff period increment.
	 * @param backoffPeriodIncrement The backoff period increment
	 */
	public void setBackoffPeriodIncrement(
		final float backoffPeriodIncrement)
	{
		this.backoffPeriodIncrement = Clamp.clampFloat(backoffPeriodIncrement, 0, Float.MAX_VALUE);
	}

	/**
	 * Get backoff period limit.
	 * @return The backoff period limit
	 */
	public int getBackoffPeriodLimit()
	{
		return backoffPeriodLimit;
	}

	/**
	 * Set backoff period limit.
	 * @param backoffPeriodLimit The backoff period limit, specified in seconds
	 */
	public void setBackoffPeriodLimit(
		final int backoffPeriodLimit)
	{
		this.backoffPeriodLimit = Clamp.clampInt(backoffPeriodLimit * 1000, 0, Integer.MAX_VALUE);
	}

	/**
	 * Get connection inactivity timeout.
	 * @return The connection inactivity timeout
	 */
	public final int getInactivityTimeout()
	{
		return inactivityTimeout;
	}

	/**
	 * Set connection inactivity timeout.
	 * @param inactivityTimeout The connection inactivity timeout, specified in seconds
	 */
	public final void setInactivityTimeout(
		final int inactivityTimeout)
	{
		this.inactivityTimeout = Clamp.clampInt(inactivityTimeout * 1000, 0, Integer.MAX_VALUE);
	}

	/**
	 * Get connection prune interval.
	 * @return The connection prune interval
	 */
	public int getPruneInterval()
	{
		return pruneInterval;
	}

	/**
	 * Set connection prune interval.
	 * @param pruneInterval The connection prune interval, specified in seconds
	 */
	public void setPruneInterval(
		final int pruneInterval)
	{
		this.pruneInterval = Clamp.clampInt(pruneInterval * 1000, 0, Integer.MAX_VALUE);
	}

}

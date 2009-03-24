/**
 * 
 */
package org.rifidi.edge.client.model.sal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.Preferences;
import org.rifidi.edge.client.model.Activator;
import org.rifidi.edge.client.model.sal.notifications.handlers.ReaderFactoryNotificationHandler;
import org.rifidi.edge.client.model.sal.notifications.handlers.ReaderNotificationHandler;
import org.rifidi.edge.client.model.sal.preferences.EdgeServerPreferences;
import org.rifidi.edge.core.api.jms.notifications.ReaderFactoryNotification;
import org.rifidi.edge.core.api.jms.notifications.ReaderNotification;
import org.rifidi.edge.core.api.rmi.dto.ReaderDTO;
import org.rifidi.edge.core.api.rmi.dto.ReaderFactoryDTO;
import org.rifidi.edge.core.rmi.client.edgeserverstub.ESGetStartupTimestamp;
import org.rifidi.edge.core.rmi.client.edgeserverstub.ESSave;
import org.rifidi.edge.core.rmi.client.edgeserverstub.ESServerDescription;
import org.rifidi.edge.core.rmi.client.readerconfigurationstub.RS_GetReaderFactories;
import org.rifidi.edge.core.rmi.client.readerconfigurationstub.RS_GetReaders;
import org.rifidi.edge.core.rmi.client.readerconfigurationstub.RS_ServerDescription;
import org.rifidi.rmi.utils.exceptions.ServerUnavailable;

/**
 * The Edge Server Model Object
 * 
 * @author Kyle Neumeier
 * 
 */
public class RemoteEdgeServer implements MessageListener {

	/** The logger for this class */
	private static final Log logger = LogFactory.getLog(RemoteEdgeServer.class);
	/** The set of reader factories on the server */
	private ObservableMap readerFactoryIDs;
	/** The remote readers on this edge server */
	private ObservableMap remoteReaders;
	/** The current state of the edge server */
	private RemoteEdgeServerState state;

	private ActiveMQConnectionFactory connectionFactory;
	private Connection conn;
	private Session session;
	private Destination dest;
	private MessageConsumer consumer;
	private Long startupTime = 0l;

	/** The property change support for this class */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public static final String STATE_PROPERTY = "org.rifidi.edge.client.model.sal.RemoteEdgeServer.state";

	/**
	 * Constructor
	 * 
	 * @param ip
	 *            The IP of the server
	 * @param port
	 *            The RMI port of the server
	 */
	public RemoteEdgeServer() {
		changeState(RemoteEdgeServerState.DISCONNECTED);

		readerFactoryIDs = new WritableMap();
		remoteReaders = new WritableMap();

		connectionFactory = new ActiveMQConnectionFactory();
	}

	public void connect() {

		if (this.state == RemoteEdgeServerState.CONNECTED) {
			logger.warn("Edge Server is already in the connected state!");
			return;
		}
		try {
			this.startupTime = 0l;
			// set up JMS consumer
			Preferences node = new DefaultScope().getNode(Activator.PLUGIN_ID);
			String ip = node.get(EdgeServerPreferences.EDGE_SERVER_IP,
					EdgeServerPreferences.EDGE_SERVER_IP_DEFAULT);
			String port = node.get(EdgeServerPreferences.EDGE_SERVER_PORT_JMS,
					EdgeServerPreferences.EDGE_SERVER_PORT_JMS_DEFAULT);
			String queuename = node.get(
					EdgeServerPreferences.EDGE_SERVER_JMS_QUEUE,
					EdgeServerPreferences.EDGE_SERVER_JMS_QUEUE_DEFAULT);
			connectionFactory.setBrokerURL("tcp://" + ip + ":" + port);
			conn = connectionFactory.createConnection();
			conn.start();
			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			dest = session.createQueue(queuename);
			consumer = session.createConsumer(dest);
			consumer.setMessageListener(this);

			// Connect to RMI
			logger.info("Remote Edge Server is in the Connected state");
			changeState(RemoteEdgeServerState.CONNECTED);
			update();
		} catch (JMSException e) {
			logger.debug("JMSException!", e);
			disconnect();
		}

	}

	/**
	 * Update the model
	 * 
	 * @throws ServerUnavailable
	 *             If there was a problem when connecting to the server
	 */
	public void update() {
		if (this.state == RemoteEdgeServerState.DISCONNECTED) {
			logger.warn("Cannot update the Remote Edge Server when "
					+ "it is in the Disconnected State!");
			return;
		}
		try {
			// first get the timestamp from the server
			ESGetStartupTimestamp getTimestampCall = new ESGetStartupTimestamp(
					getESServerDescription());
			Long timestamp = getTimestampCall.makeCall();

			// if the server has restarted since the last time we updated, we
			// need to disconnect and reconnect
			if (!(startupTime == 0 || timestamp == startupTime)) {
				disconnect();
				connect();
				return;
			}
			startupTime = timestamp;

			RS_ServerDescription rs_description = getRSServerDescription();
			RS_GetReaderFactories rsGetFactoriesCall = new RS_GetReaderFactories(
					rs_description);
			Set<ReaderFactoryDTO> readerFactoryDTOs = rsGetFactoriesCall
					.makeCall();
			for (ReaderFactoryDTO factory : readerFactoryDTOs) {
				logger.debug("Found ReaderFactory: "
						+ factory.getReaderFactoryID());
				readerFactoryIDs.put(factory.getReaderFactoryID(), factory);
			}

			RS_GetReaders rsGetReaderCall = new RS_GetReaders(rs_description);
			Set<ReaderDTO> readerDTOs = rsGetReaderCall.makeCall();
			for (ReaderDTO reader : readerDTOs) {
				logger.debug("Found reader: " + reader.getReaderID());
				remoteReaders.put(reader.getReaderID(),
						new RemoteReader(reader));
			}
		} catch (ServerUnavailable su) {
			logger.error("ServerUnavailable! ", su);
			disconnect();
		}
	}

	public void disconnect() {
		if (this.state == RemoteEdgeServerState.DISCONNECTED) {
			logger.warn("Remote Edge Server is already "
					+ "in the Disconnected State!");
			return;
		}
		try {
			conn.stop();
			conn.close();
			session.close();
			consumer.close();
		} catch (JMSException e) {
			logger.error("Error when disconnecting: ", e);
		}
		readerFactoryIDs.clear();
		remoteReaders.clear();
		logger.info("Remote Edge Server is in the Disconnected state");
		changeState(RemoteEdgeServerState.DISCONNECTED);
	}

	public void saveConfiguration() {
		if (this.state == RemoteEdgeServerState.DISCONNECTED) {
			logger.warn("Cannot save configuration when Edge Server "
					+ "is in the Disconnected State!");
			return;
		}

		ESSave saveCommand = new ESSave(getESServerDescription());
		try {
			saveCommand.makeCall();
		} catch (ServerUnavailable e) {
			logger.error("Exception while saving Remote Configuration", e);
			disconnect();
		}

	}

	/**
	 * @return the readerFactoryIDs
	 */
	public ObservableMap getReaderFactoryIDs() {
		return readerFactoryIDs;
	}

	/**
	 * @return the remoteReaders
	 */
	public ObservableMap getRemoteReaders() {
		return remoteReaders;
	}

	@Override
	public void onMessage(Message arg0) {
		if (arg0 instanceof BytesMessage) {
			Object message;
			try {
				message = deserialize((BytesMessage) arg0);
				if (message instanceof ReaderNotification) {
					ReaderNotification ra = (ReaderNotification) message;
					ReaderNotificationHandler rnh = new ReaderNotificationHandler(
							ra, remoteReaders, getRSServerDescription());
					rnh.handleNotification();
				} else if (message instanceof ReaderFactoryNotification) {
					ReaderFactoryNotification rfn = (ReaderFactoryNotification) message;
					ReaderFactoryNotificationHandler rfnh = new ReaderFactoryNotificationHandler(
							rfn, readerFactoryIDs, getRSServerDescription());
					rfnh.handleNotification();
				}
			} catch (JMSException e) {
				logger.warn("JMS Exception while recieving message");
				disconnect();
			} catch (IOException e) {
				logger.warn("Cannot Deserialize the Message " + arg0);
			} catch (ClassNotFoundException e) {
				logger.warn("Cannot Deserialize the Message " + arg0);
			}
		}

	}

	private Object deserialize(BytesMessage message) throws JMSException,
			IOException, ClassNotFoundException {
		int length = (int) message.getBodyLength();
		byte[] bytes = new byte[length];
		message.readBytes(bytes);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(
				bytes));
		return in.readObject();
	}

	public RemoteEdgeServerState getState() {
		return this.state;
	}

	private synchronized void changeState(RemoteEdgeServerState newState) {
		logger.info("Remote Edge Server State: " + newState);
		RemoteEdgeServerState oldstate = this.state;
		this.state = newState;
		this.pcs.firePropertyChange(STATE_PROPERTY, oldstate, newState);

	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	private RS_ServerDescription getRSServerDescription() {
		Preferences node = new DefaultScope().getNode(Activator.PLUGIN_ID);
		String ip = node.get(EdgeServerPreferences.EDGE_SERVER_IP,
				EdgeServerPreferences.EDGE_SERVER_IP_DEFAULT);
		int port = Integer.parseInt(node.get(
				EdgeServerPreferences.EDGE_SERVER_PORT_RMI,
				EdgeServerPreferences.EDGE_SERVER_PORT_RMI_DEFAULT));
		return new RS_ServerDescription(ip, port);
	}

	private ESServerDescription getESServerDescription() {
		Preferences node = new DefaultScope().getNode(Activator.PLUGIN_ID);
		String ip = node.get(EdgeServerPreferences.EDGE_SERVER_IP,
				EdgeServerPreferences.EDGE_SERVER_IP_DEFAULT);
		int port = Integer.parseInt(node.get(
				EdgeServerPreferences.EDGE_SERVER_PORT_RMI,
				EdgeServerPreferences.EDGE_SERVER_PORT_RMI_DEFAULT));
		return new ESServerDescription(ip, port);
	}

}
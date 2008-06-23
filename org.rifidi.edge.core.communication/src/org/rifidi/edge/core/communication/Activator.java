package org.rifidi.edge.core.communication;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.rifidi.edge.core.communication.service.CommunicationServiceImpl;
import org.rifidi.edge.core.connection.registry.ReaderConnectionRegistryService;
import org.rifidi.edge.core.connection.registry.ReaderConnectionRegistryServiceImpl;
import org.rifidi.edge.core.readerPluginService.ReaderPluginRegistryService;
import org.rifidi.edge.core.readerPluginService.ReaderPluginRegistryServiceImpl;

public class Activator implements BundleActivator {

	private CommunicationServiceImpl communicationService;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("== Bundle " + this.getClass().getName() + " loaded ==");

		
		System.out.println("Registering Service: CommunicationService");
		
		communicationService = new CommunicationServiceImpl();
		context.registerService(CommunicationService.class.getName(),
				communicationService, null);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		System.out.println("== Bundle " + this.getClass().getName() + " stopped ==");
	}

}

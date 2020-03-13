/*
 * Comcast Copyright Details
 * 
 */

package com.okta.custom.logout.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

/**
 * BootstrapServlet class gets loaded during the server startup.This class contains the functionality of loading any
 * data which will be used by the application.This class retrieves data from AWS DynamoDB tables and store it in static
 * variables.
 */
public class BootstrapServlet extends HttpServlet {

	// System.outprintln()s and printStackTraces are required in this class,
	// because
	// this class
	// initiates the logging system. The functionality in this class cannot be
	// logged before the
	// logging system is initiated.

	/**
	 * a serial version UID
	 */
	private static final long serialVersionUID = 1145921168188517845L;

	/**
	 * Constructor
	 */
	public BootstrapServlet() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		initialStartup(context);
	}

	/**
	 * Method which loads the AWS specific details such as DynamoDB tables data which contains the Okta configuration
	 * and Applications logout URLs details.This method gets invoked during server startup.
	 */
	private void initialStartup(ServletContext context) {
		System.out.println("BootstrapServlet: initialStartup: Loading BootstrapServlet...");
		try {
			this.configureLog4jSystem(context);
			final AWSOperations awsOperations = new AWSOperations();
			awsOperations.getOktaConfigurations();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("BootstrapServlet: initialStartup: exiting");
	}

	/**
	 * Configures the log4j system
	 * 
	 * @param context
	 */
	private void configureLog4jSystem(ServletContext context) {
		InputStream log4jInputStream = null;
		try {
			log4jInputStream = context.getResourceAsStream(LogoutHelper.LOG4J_PROP);
			Properties props = new Properties();
			props.load(log4jInputStream);
			PropertyConfigurator.configure(props);
			System.out.println("*** Successfully loaded log4j configuration from - " + LogoutHelper.LOG4J_PROP);

		} catch (FileNotFoundException e) {
			System.out.println("BootstrapServlet: initialStartup: Exception Occured while log4j configuraion..."
					+ e.getMessage());
			System.err.println("BootstrapServlet: initialStartup: Exception Occured while log4j configuraion..."
					+ e.getMessage());
			e.printStackTrace();
			BasicConfigurator.configure();
			System.out.println("***** Initializing log4j with BasicConfigurator *****");
		} catch (IOException e) {
			System.out.println("*****BootstrapServlet: initialStartup: Exception Occured while log4j configuraion..."
					+ e.getMessage());
			System.err.println("BootstrapServlet: initialStartup: Exception Occured while log4j configuraion..."
					+ e.getMessage());
			e.printStackTrace();
			BasicConfigurator.configure();
			System.out.println("***** Initializing log4j with BasicConfigurator *****");
		} catch (Exception e) {
			System.out.println("*****BootstrapServlet: initialStartup: Exception Occured while log4j configuraion..."
					+ e.getMessage());
			System.err.println("BootstrapServlet: initialStartup: Exception Occured while log4j configuraion..."
					+ e.getMessage());
			e.printStackTrace();
		} finally {
			if (null != log4jInputStream) {
				try {
					log4jInputStream.close();
				} catch (IOException e) {
					System.err
							.println("Exception during log4j initialization. Exception while trying close the FileInputStream for log4j file");
					System.out
							.println("Exception during log4j initialization. Exception while trying close the FileInputStream for log4j file");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Destroy method
	 */
	@Override
	public void destroy() {
		throw new UnsupportedOperationException();
	}
}

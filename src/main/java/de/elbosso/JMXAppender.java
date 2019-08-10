package de.elbosso;

import org.apache.logging.log4j.Level;

import javax.management.*;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.swing.text.BadLocationException;

@org.apache.logging.log4j.core.config.plugins.Plugin(name = "JMXAppender", category = org.apache.logging.log4j.core.Core.CATEGORY_NAME, elementType = org.apache.logging.log4j.core.Appender.ELEMENT_TYPE)
public class JMXAppender extends org.apache.logging.log4j.core.appender.AbstractAppender implements javax.management.NotificationBroadcaster,
		JMXAppenderMBean
{

	private static final java.lang.String[] ITEMNAMES=new java.lang.String[]{"level",
			"timeStamp",
			"fqnOfCategoryClass",
			"loggerName",
			"ndc",
			"threadName",
			"throwableStrRep",
			"message"};
	private static final java.lang.String[] ITEMDESCRIPTIONS=new java.lang.String[]{"level - may be TRACE, DEBUG, INFO, WARN, ERROR, FATAL",
			"timeStamp - the timestamp of the logging event",
			"fqnOfCategoryClass",
			"loggerName - the nema of the logger",
			"ndc",
			"threadName - name of the thread the logging was done from",
			"throwableStrRep - array of strings that represent a stacktrace to the location of the log event",
			"message - the actual layouted message formatted by the given layout"};
	private static javax.management.openmbean.OpenType[] ITEMTYPES;
	private static javax.management.openmbean.CompositeType TYPE;
	static
	{
		try
		{
			ITEMTYPES=new javax.management.openmbean.OpenType[]{SimpleType.STRING,
					SimpleType.LONG,
					SimpleType.STRING,
					SimpleType.STRING,
					SimpleType.STRING,
					SimpleType.STRING,
					new ArrayType(1,SimpleType.STRING),
					SimpleType.STRING};
			TYPE = new javax.management.openmbean.CompositeType("Log event data type",
					"data type to mirror a log4j log event",
					ITEMNAMES,
					ITEMDESCRIPTIONS,
					ITEMTYPES);
		}
		catch(java.lang.Throwable t)
		{
			t.printStackTrace();
		}
	}
	private javax.management.NotificationBroadcasterSupport notificationBroadcasterSupport;
	private long notificationSequence = 0;
	private boolean initialized;


	protected JMXAppender(String name, org.apache.logging.log4j.core.Filter filter) {
		super(name, filter, null);
		notificationBroadcasterSupport=new javax.management.NotificationBroadcasterSupport();
	}

	@org.apache.logging.log4j.core.config.plugins.PluginFactory
	public static JMXAppender createAppender(@org.apache.logging.log4j.core.config.plugins.PluginAttribute("name") String name, @org.apache.logging.log4j.core.config.plugins.PluginElement("Filter") final org.apache.logging.log4j.core.Filter filter) {
		return new JMXAppender(name, filter);
	}

	private void checkInitialization()
	{
		if(initialized==false)
		{
			try
			{
				javax.management.MBeanServer mbs = java.lang.management.ManagementFactory.getPlatformMBeanServer();
				javax.management.ObjectName name = new javax.management.ObjectName("jmxlogger:type=LogEmitter");
				mbs.registerMBean(this, name);
				initialized = true;
			}
			catch(java.lang.Exception t)
			{
				t.printStackTrace();
				error(t.getMessage(),t);
			}
		}
	}

	@Override
	public void append(org.apache.logging.log4j.core.LogEvent le)
	{
		checkInitialization();
		java.lang.String msg=le.getMessage().getFormattedMessage();
		Notification notif=new Notification(
				"Log."+le.getLevel().toString(),     // type
				this,              // source
				++notificationSequence,     // seq. number
				msg
		);
//		notif.setUserData(le);
		Object[] itemValues = new Object[]{le.getLevel().toString(),
				le.getTimeMillis(),
				le.getLoggerFqcn(),
				le.getLoggerName(),
				null,//le.getNDC(),
				le.getThreadName(),
				le.getThrownProxy()!=null?le.getThrownProxy().getExtendedStackTraceAsString():null,
				msg};
		try
		{
			javax.management.openmbean.CompositeDataSupport support = new javax.management.openmbean.CompositeDataSupport(TYPE,ITEMNAMES,itemValues);
			notif.setUserData(support);
		} catch (OpenDataException e)
		{
			error(e.getMessage(),e);
		}
		notificationBroadcasterSupport.sendNotification(notif);
	}

	@Override
	public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
	{
		notificationBroadcasterSupport.addNotificationListener(listener,filter,handback);
	}

	@Override
	public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
	{
		notificationBroadcasterSupport.removeNotificationListener(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo()
	{
		return new MBeanNotificationInfo[]{
				new MBeanNotificationInfo(
						new String[]
								{"Log.TRACE","Log.DEBUG","Log.INFO","Log.WARN","Log.ERROR","Log.FATAL"}, // notif. types
						Notification.class.getName(), // notif. class
						"Log4j Log Appender"     // description
				)
		};
	}

}
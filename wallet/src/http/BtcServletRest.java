package http;

import com.google.common.util.concurrent.Service;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


public class BtcServletRest {

    public static void main(String[] args) throws Exception {
        run(null);
    }

    public static void run(Service app) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);

        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", BtcCmd.class.getCanonicalName());

        try {
            jettyServer.start();
            jettyServer.join();
        } catch (RuntimeException e) {
            System.out.println(e);
        } finally {
            jettyServer.destroy();
        }
    }
}
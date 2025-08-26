package it.polimi.auction;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@WebListener
public class ThymeleafConfig implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
//        // Create a template resolver that loads templates from the classpath
//        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
//        resolver.setTemplateMode(TemplateMode.HTML);  // Use HTML template mode
//        resolver.setPrefix("templates/"); // Template folder under src/main/resources
//        resolver.setSuffix(".html");   // All templates have .html extension
//        resolver.setCharacterEncoding("UTF-8");   // Use UTF-8 encoding
//        resolver.setCacheable(false); // Disable caching during development
//
//        // Create a template engine and set the resolver
//        TemplateEngine engine = new TemplateEngine();
//        engine.setTemplateResolver(resolver);
//
//        // Store the template engine in the ServletContext for access by other components
//        ServletContext context = sce.getServletContext();
//        context.setAttribute("templateEngine", engine);

        ServletContext context = sce.getServletContext();
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(context);
        context.setAttribute("templateEngine", engine);

    }

    //Restituisce un oggetto TemplateEngine, che serve per
    // elaborare e renderizzare i template HTML con Thymeleaf.
    public static TemplateEngine getTemplateEngine(ServletContext context) {
        TemplateEngine engine = new TemplateEngine();
        JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(context);
        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(webApp);
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
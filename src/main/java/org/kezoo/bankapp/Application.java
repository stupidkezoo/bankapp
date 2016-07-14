package org.kezoo.bankapp;

import org.apache.ignite.Ignition;
import org.kezoo.bankapp.listener.PaymentTransferListener;
import org.kezoo.bankapp.service.ConsoleCommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private PaymentTransferListener paymentTransferListener;
    @Autowired
    private ConsoleCommandListener consoleCommandListener;

    public static String bankName;

    public Application(){
        Ignition.start("src/main/resources/ignite-config.xml");
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        AutowireCapableBeanFactory acbFactory = context.getAutowireCapableBeanFactory();
        acbFactory.autowireBean(this);
    }

    public static void main(String[] args) throws Exception {
        if (!checkArgs(args)) {
            log.error("expected bank identifier, but found {}", args);
            System.exit(1);
        }
        log.info("Starting for bank {}", args[0]);
        bankName = args[0];
        Application application = new Application();
        application.consoleCommandListener.start();
        application.paymentTransferListener.startListening();
    }

    private static boolean checkArgs(String[] args) {
        return args != null && args.length > 0 && args[0] != null && !args[0].isEmpty();
    }
}

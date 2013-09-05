/*
 * Copyright (C) 2013 Comune di Riva del Garda
 * (http://www.comune.rivadelgarda.tn.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.tn.rivadelgarda.comune.suite;

import com.axiastudio.pypapi.Application;
import com.axiastudio.pypapi.IStreamProvider;
import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.Resolver;
import com.axiastudio.pypapi.db.Database;
import com.axiastudio.pypapi.db.IDatabase;
import com.axiastudio.suite.plugins.cmis.CmisPlugin;
import com.axiastudio.suite.plugins.cmis.CmisStreamProvider;
import com.axiastudio.suite.plugins.ooops.FileStreamProvider;
import com.axiastudio.suite.plugins.ooops.OoopsPlugin;
import com.axiastudio.suite.plugins.ooops.RuleSet;
import com.axiastudio.suite.plugins.ooops.Template;
import com.axiastudio.suite.Configure;
import com.axiastudio.suite.Mdi;
import com.axiastudio.suite.base.Login;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.pratiche.forms.FormPratica;
import com.axiastudio.suite.procedimenti.GestoreDeleghe;
import com.axiastudio.suite.procedimenti.IGestoreDeleghe;
import com.axiastudio.suite.protocollo.forms.FormProtocollo;
import com.axiastudio.suite.protocollo.forms.FormScrivania;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tiziano Lattisi <tiziano at axiastudio.it>
 * 
 */
public class Start {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // jdbc
        String jdbcUrl = System.getProperty("jdbc.url");
        String jdbcUser = System.getProperty("jdbc.user");
        String jdbcPassword = System.getProperty("jdbc.password");
        String jdbcDriver = System.getProperty("jdbc.driver");
        
        // log
        String logLevel = System.getProperty("suite.loglevel");
        
        // Alfresco
        String cmisUrl = System.getProperty("cmis.url");
        String cmisUser = System.getProperty("cmis.user");
        String cmisPassword = System.getProperty("cmis.password");
        
        // Stampante etichette
        String barcodeDevice = System.getProperty("barcode.device"); // es. Zebra_Technologies_ZTC_GK420t
        String barcodeLanguage = System.getProperty("barcode.language"); // es. ZPL
        
        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url", jdbcUrl);
        if( jdbcUser != null ){
            properties.put("javax.persistence.jdbc.user", jdbcUser);
        }
        if( jdbcPassword != null ){
            properties.put("javax.persistence.jdbc.password", jdbcPassword);
        }
        if( jdbcDriver != null ){
            properties.put("javax.persistence.jdbc.driver", jdbcDriver);
        }
        if( logLevel != null ){
            properties.put("eclipselink.logging.level", logLevel);
            properties.put("eclipselink.logging.parameters", "true");    
        }

        Database db = new Database();
        properties.put("eclipselink.ddl-generation", "");
        db.open("SuitePU", properties);
        Register.registerUtility(db, IDatabase.class);
        
        
        // applicazione Qt        
        Application app = new Application(args);
        app.addQmFile("classpath:com/axiastudio/menjazo/lang/menjazo_{0}.qm"); // menjazo
        app.setLanguage("it");
        
        // config
        app.setConfigItem("barcode.device", barcodeDevice);
        app.setConfigItem("barcode.language", barcodeLanguage);

        // percorsi Alfresco
        app.setConfigItem("alfrescopath.protocollo", "/Siti/protocollo/documentLibrary");
        app.setConfigItem("alfrescopath.pratica", "/Siti/pratiche/documentLibrary");
        app.setConfigItem("alfrescopath.pubblicazione", "/Siti/pubblicazioni/documentLibrary");

        // configurazione originale SuitePA
        Configure.configure(db, System.getProperties());
        
        // configurazione personalizzata Riva GDA        
        Register.registerCallbacks(Resolver.callbacksFromClass(ProtocolloCallbacksRiva.class));
        Register.registerCallbacks(Resolver.callbacksFromClass(PraticaCallbacksRiva.class));
        Register.registerForm(db.getEntityManagerFactory(),
                "classpath:com/axiastudio/suite/deliberedetermine/forms/determina.ui",
                Determina.class,
                FormDeterminaJEnte.class);
        Register.registerUtility(new FinanziariaUtil(), IFinanziaria.class);
        //Register.registerUtility(FinanziariaUtilFake.class, IFinanziaria.class); // test

        /* login */
        Login login = new Login();
        login.setWindowTitle("GDA");
        int res = login.exec();
        if( res == 1 ){

            Mdi mdi = new Mdi();
            mdi.showMaximized();
            mdi.setWindowTitle("GDA");
            //mdi.show();
            
            // Scrivania
            FormScrivania form = new FormScrivania();
            mdi.getWorkspace().addSubWindow(form);
            form.showMaximized();
            
            app.setCustomApplicationName("GDA");
            app.setCustomApplicationCredits("Copyright AXIA Studio - Comune di Riva del Garda (2013)<br/>");
            app.exec();
        }
        
        System.exit(res);
    
    }
    
}

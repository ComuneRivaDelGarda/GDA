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
import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.Resolver;
import com.axiastudio.pypapi.db.Database;
import com.axiastudio.pypapi.db.IDatabase;
import com.axiastudio.suite.Configure;
import com.axiastudio.suite.Mdi;
import com.axiastudio.suite.base.ICheckLogin;
import com.axiastudio.suite.base.Login;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.plugins.cmis.CmisPlugin;
import com.axiastudio.suite.plugins.ooops.OoopsPlugin;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

        String jdbcUrl = null;
        String jdbcUser = null;
        String jdbcPassword = null;
        String jdbcDriver = null;

        String logLevel = null;

        String cmisUrl = null;
        String cmisUser = null;
        String cmisPassword = null;

        String alfrescopathProtocollo = null;
        String alfrescopathPratica = null;
        String alfrescopathPubblicazione = null;

        String barcodeDevice = null;
        String barcodeLanguage = null;

        String oooConnection = "uno:socket,host=ooops,port=2002;urp;StarOffice.ServiceManager";

        // file di Properties
        Properties properties = new Properties();
        try {
            InputStream inputStream = Start.class.getResourceAsStream("gda.properties");
            if( inputStream != null ) {
                properties.load(inputStream);

                jdbcUrl = properties.getProperty("jdbc.url");
                jdbcUser = properties.getProperty("jdbc.user");
                jdbcPassword = properties.getProperty("jdbc.password");
                jdbcDriver = properties.getProperty("jdbc.driver");

                logLevel = properties.getProperty("suite.loglevel");

                cmisUrl = properties.getProperty("cmis.url");
                cmisUser = properties.getProperty("cmis.user");
                cmisPassword = properties.getProperty("cmis.password");

                alfrescopathProtocollo = properties.getProperty("alfrescopath.protocollo");
                alfrescopathPratica = properties.getProperty("alfrescopath.protocollo");
                alfrescopathPubblicazione = properties.getProperty("alfrescopath.protocollo");

                barcodeDevice = properties.getProperty("barcode.device"); // es. Zebra_Technologies_ZTC_GK420t
                barcodeLanguage = properties.getProperty("barcode.language"); // es. ZPL

                oooConnection = properties.getProperty("ooo.connection");
            }
        } catch (IOException e) {

        }

        // jdbc
        if( jdbcUrl == null ) jdbcUrl = System.getProperty("jdbc.url");
        if( jdbcUser == null ) jdbcUser = System.getProperty("jdbc.user");
        if( jdbcPassword == null ) jdbcPassword = System.getProperty("jdbc.password");
        if( jdbcDriver == null ) jdbcDriver = System.getProperty("jdbc.driver");

        // log
        if( logLevel == null ) logLevel = System.getProperty("suite.loglevel");
        
        // Alfresco
        if( cmisUrl == null ) cmisUrl = System.getProperty("cmis.url");
        if( cmisUser == null ) cmisUser = System.getProperty("cmis.user");
        if( cmisPassword == null ) cmisPassword = System.getProperty("cmis.password");

        if( alfrescopathProtocollo == null ) alfrescopathProtocollo = System.getProperty("alfrescopath.protocollo");
        if( alfrescopathPratica == null ) alfrescopathPratica = System.getProperty("alfrescopath.pratica");
        if( alfrescopathPubblicazione == null ) alfrescopathPubblicazione = System.getProperty("alfrescopath.pubblicazione");

        // OpenOffice
        if( oooConnection == null ) oooConnection = System.getProperty("ooo.connection");

        // Stampante etichette
        if( barcodeDevice == null ) barcodeDevice = System.getProperty("barcode.device"); // es. Zebra_Technologies_ZTC_GK420t
        if( barcodeLanguage == null ) barcodeLanguage = System.getProperty("barcode.language"); // es. ZPL
        
        Map mapProperties = new HashMap();
        mapProperties.put("javax.persistence.jdbc.url", jdbcUrl);
        if( jdbcUser != null ){
            mapProperties.put("javax.persistence.jdbc.user", jdbcUser);
        }
        if( jdbcPassword != null ){
            mapProperties.put("javax.persistence.jdbc.password", jdbcPassword);
        }
        if( jdbcDriver != null ){
            mapProperties.put("javax.persistence.jdbc.driver", jdbcDriver);
        }
        if( logLevel != null ){
            mapProperties.put("eclipselink.logging.level", logLevel);
            mapProperties.put("eclipselink.logging.parameters", "true");
        }

        Database db = new Database();
        mapProperties.put("eclipselink.ddl-generation", "");
        db.open("SuitePU", mapProperties);
        Register.registerUtility(db, IDatabase.class);

        CheckPGUser checkPGUser = new CheckPGUser();
        checkPGUser.setJdbcUrl(jdbcUrl);
        Register.registerUtility(checkPGUser, ICheckLogin.class);
        
        // applicazione Qt        
        Application app = new Application(args);
        app.addQmFile("classpath:com/axiastudio/menjazo/lang/menjazo_{0}.qm"); // menjazo
        app.setLanguage("it");
        
        // config
        app.setConfigItem("barcode.device", barcodeDevice);
        app.setConfigItem("barcode.language", barcodeLanguage);

        // percorsi Alfresco
        app.setConfigItem("alfrescopath.protocollo", alfrescopathProtocollo);
        app.setConfigItem("alfrescopath.pratica", alfrescopathPratica);
        app.setConfigItem("alfrescopath.pubblicazione", alfrescopathPubblicazione);

        // scringa di connessione per OpenOffice
        app.setConfigItem("ooops.connection", oooConnection);
        //app.setConfigItem("ooops.connection", "uno:socket,host=192.168.64.56,port=2002;urp;StarOffice.ServiceManager");

        // configurazione originale SuitePA
        Configure.configure(db, System.getProperties());
        
        // configurazione personalizzata Riva GDA
        Register.registerCallbacks(Resolver.callbacksFromClass(DeterminaCallbacksRiva.class));
        Register.registerForm(db.getEntityManagerFactory(),
                "classpath:com/axiastudio/suite/deliberedetermine/forms/determina.ui",
                Determina.class,
                FormDeterminaJEnte.class);
        Register.registerUtility(new FinanziariaUtil(), IFinanziaria.class);
        //Register.registerUtility(FinanziariaUtilFake.class, IFinanziaria.class); // test
        Register.registerForm(db.getEntityManagerFactory(),
                "classpath:com/axiastudio/suite/pubblicazioni/forms/pubblicazione.ui",
                Pubblicazione.class,
                FormPubblicazioneRiva.class);

        // CMIS e Ooops
        CmisPlugin cmisPluginDetermina = (CmisPlugin) Register.queryPlugin(FormDetermina.class, "CMIS");
        Register.registerPlugin(cmisPluginDetermina, FormDeterminaJEnte.class);
        OoopsPlugin ooopsPluginDetermina = (OoopsPlugin) Register.queryPlugin(FormDetermina.class, "Ooops");
        Register.registerPlugin(ooopsPluginDetermina, FormDeterminaJEnte.class);
        CmisPlugin cmisPluginPubblicazioni = (CmisPlugin) Register.queryPlugin(FormPubblicazione.class, "CMIS");
        Register.registerPlugin(cmisPluginPubblicazioni, FormPubblicazioneRiva.class);

        /* login */
        Login login = new Login();
        login.setWindowTitle("GDA");
        int res = login.exec();
        if( res == 1 ){

            Mdi mdi = new Mdi();
            mdi.showMaximized();
            mdi.setWindowTitle("GDA");
            mdi.show();

            // Scrivania
            //FormScrivania form = new FormScrivania();
            //mdi.getWorkspace().addSubWindow(form);
            //form.showMaximized();
            
            app.setCustomApplicationName("GDA");
            app.setCustomApplicationCredits("Copyright AXIA Studio - Comune di Riva del Garda (2013)<br/>");
            app.exec();
        }
        
        System.exit(res);
    
    }
    
}

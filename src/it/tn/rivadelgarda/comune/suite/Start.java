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
import com.axiastudio.suite.Suite;
import com.axiastudio.suite.base.ICheckLogin;
import com.axiastudio.suite.base.Login;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.plugins.cmis.CmisPlugin;
//import com.axiastudio.suite.plugins.docer.DocerPlugin;
import com.axiastudio.suite.plugins.ooops.OoopsPlugin;
import com.axiastudio.suite.protocollo.forms.FormScrivania;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;
import com.axiastudio.suite.richieste.entities.Richiesta;
import com.trolltech.qt.gui.*;

import javax.persistence.EntityManager;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Tiziano Lattisi <tiziano at axiastudio.it>
 * 
 */
public class Start extends Suite {

    private static final String UPDATEURL = "";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        Properties p = new Properties();
//        // p.load(new FileReader(new File("config.properties")));
//        try {
//            p.load(Start.class.getResourceAsStream("config.properties"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // String url =
//        // "http://192.168.64.22:8080/docersystem/services/AuthenticationService";
//        String url = p.getProperty("url");
//        String username = p.getProperty("username");
//        String password = p.getProperty("password");
//
//        DocerHelper helper = new DocerHelper(url, username, password);
//        try {
//            String token = helper.login();
//            System.out.println(token);
//            DocerServicesStub.SearchItem[] res = helper.searchFolders("test1");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//

        Application app = new Application(args);

        /* UPDATE */
        if( UPDATEURL != "" && AppUpdater.checkForUpdates(UPDATEURL) ) {
            QDialog dialog = new QDialog();
            QVBoxLayout layout = new QVBoxLayout(dialog);
            QLabel msg = new QLabel();
            layout.addWidget(msg);
            QPushButton button = new QPushButton("OK");
            layout.addWidget(button);
            button.clicked.connect(dialog, "accept()");
            dialog.setWindowTitle("Aggiornamento");
            if( AppUpdater.update(UPDATEURL) ) {
                msg.setText("L'applicazione è stata aggiornata, è necessario riavviarla.");
                int res = dialog.exec();
                System.exit(res);
            } else {
                msg.setText("Sono presenti degli aggiornamenti. Premere OK per continuare a lavorare con la versione attuale.");
                dialog.exec();
            }
        }

        InputStream propertiesStream = Start.class.getResourceAsStream("gda.properties");

        configure(app, propertiesStream);
        app.setLanguage("it");

        // login su Postgres
        CheckPGUser checkPGUser = new CheckPGUser();
        checkPGUser.setJdbcUrl((String) app.getConfigItem("jdbc.url"));
        Register.registerUtility(checkPGUser, ICheckLogin.class);

        /* login */
        Login login = new Login();
        login.setWindowTitle("GDA");
        int res = login.exec();

        if( res == 1 ){
            Database db = (Database) Register.queryUtility(IDatabase.class);
            Configure.configure(db);

            // configurazione personalizzata Riva GDA
            Register.registerCallbacks(Resolver.callbacksFromClass(DeterminaCallbacksRiva.class));
            Register.registerForm(db.getEntityManagerFactory(),
                    "classpath:com/axiastudio/suite/deliberedetermine/forms/determina.ui",
                    Determina.class,
                    FormDeterminaJEnte.class);
            Register.registerUtility(new FinanziariaUtil(), IFinanziaria.class);
            Register.registerForm(db.getEntityManagerFactory(),
                    "classpath:com/axiastudio/suite/pubblicazioni/forms/pubblicazione.ui",
                    Pubblicazione.class,
                    FormPubblicazioneRiva.class);
            Register.registerForm(db.getEntityManagerFactory(),
                    "classpath:it/tn/rivadelgarda/comune/suite/richiestaRiva.ui",
                    Richiesta.class,
                    FormRichiestaRiva.class);

            // CMIS e Ooops
            CmisPlugin cmisPluginDetermina = (CmisPlugin) Register.queryPlugin(FormDetermina.class, "CMIS");
            Register.registerPlugin(cmisPluginDetermina, FormDeterminaJEnte.class);
            CmisPlugin cmisPluginPubblicazioni = (CmisPlugin) Register.queryPlugin(FormPubblicazione.class, "CMIS");
            Register.registerPlugin(cmisPluginPubblicazioni, FormPubblicazioneRiva.class);
//            DocerPlugin cmisPluginDetermina = (DocerPlugin) Register.queryPlugin(FormDetermina.class, "DocER");
//            Register.registerPlugin(cmisPluginDetermina, FormDeterminaJEnte.class);
//            DocerPlugin cmisPluginPubblicazioni = (DocerPlugin) Register.queryPlugin(FormPubblicazione.class, "DocER");
//            Register.registerPlugin(cmisPluginPubblicazioni, FormPubblicazioneRiva.class);
            OoopsPlugin ooopsPluginDetermina = (OoopsPlugin) Register.queryPlugin(FormDetermina.class, "Ooops");
            Register.registerPlugin(ooopsPluginDetermina, FormDeterminaJEnte.class);

            // parametri ATM
            Properties properties = new Properties();
            try {
                propertiesStream = Start.class.getResourceAsStream("gda.properties");
                if( propertiesStream != null ) {
                    properties.load(propertiesStream);
                    app.setConfigItem("atm.wsakey", properties.getProperty("atm.wsakey"));
                    app.setConfigItem("atm.MAC", properties.getProperty("atm.MAC"));
                    app.setConfigItem("atm.userID", properties.getProperty("atm.userID"));
                    app.setConfigItem("atm.password", properties.getProperty("atm.password"));
                    app.setConfigItem("atm.endpoint", properties.getProperty("atm.endpoint"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Mdi mdi = new Mdi();
            mdi.showMinimized();
            mdi.setWindowTitle("GDA");
            if( Register.queryUtility(ICheckLogin.class) != null ) {
                mdi.getItemPassword().setHidden(true);
            }
            mdi.show();

            // Scrivania
            FormScrivania form = new FormScrivania();
            mdi.getWorkspace().addSubWindow(form);
            form.showMaximized();

            final SplashScreen splash = SplashScreen.getSplashScreen();
            if (splash == null) {
                System.out.println("SplashScreen.getSplashScreen() returned null");
            } else {
                splash.close();
            }

            mdi.showMaximized();

            app.setCustomApplicationName("GDA");
            app.setCustomApplicationCredits("Copyright AXIA Studio - Comune di Riva del Garda (2013)<br/>");
            app.exec();

            Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
            EntityManager em = db.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery("UPDATE generale.finesessionigda SET utente='"+ autenticato.getLogin() +"'").executeUpdate();
            em.getTransaction().commit();
            em.clear();
            em.close();
        }
        
        System.exit(res);
    
    }

}

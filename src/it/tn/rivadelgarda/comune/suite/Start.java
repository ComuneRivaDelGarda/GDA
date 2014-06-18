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
import com.axiastudio.suite.Mdi;
import com.axiastudio.suite.Suite;
import com.axiastudio.suite.base.ICheckLogin;
import com.axiastudio.suite.base.Login;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.plugins.cmis.CmisPlugin;
import com.axiastudio.suite.plugins.ooops.OoopsPlugin;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Tiziano Lattisi <tiziano at axiastudio.it>
 * 
 */
public class Start extends Suite {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Application app = new Application(args);
        InputStream propertiesStream = Start.class.getResourceAsStream("gda.properties");

        configure(app, propertiesStream);
        app.setLanguage("it");

        Database db = (Database) Register.queryUtility(IDatabase.class);

        // login su Postgres
        CheckPGUser checkPGUser = new CheckPGUser();
        checkPGUser.setJdbcUrl((String) app.getConfigItem("jdbc.url"));
        Register.registerUtility(checkPGUser, ICheckLogin.class);

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

        }

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

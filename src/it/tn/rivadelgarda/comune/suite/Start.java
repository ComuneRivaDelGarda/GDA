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
import com.axiastudio.pypapi.plugins.barcode.Barcode;
import com.axiastudio.pypapi.plugins.cmis.CmisPlugin;
import com.axiastudio.pypapi.plugins.cmis.CmisStreamProvider;
import com.axiastudio.pypapi.plugins.ooops.FileStreamProvider;
import com.axiastudio.pypapi.plugins.ooops.OoopsPlugin;
import com.axiastudio.pypapi.plugins.ooops.RuleSet;
import com.axiastudio.pypapi.plugins.ooops.Template;
import com.axiastudio.pypapi.ui.Dialog;
import com.axiastudio.pypapi.ui.IQuickInsertDialog;
import com.axiastudio.pypapi.ui.Window;
import com.axiastudio.suite.Configure;
import com.axiastudio.suite.Mdi;
import com.axiastudio.suite.anagrafiche.AnagraficheAdapters;
import com.axiastudio.suite.anagrafiche.entities.AlboProfessionale;
import com.axiastudio.suite.anagrafiche.entities.Gruppo;
import com.axiastudio.suite.anagrafiche.entities.Indirizzo;
import com.axiastudio.suite.anagrafiche.entities.Relazione;
import com.axiastudio.suite.anagrafiche.entities.RelazioneSoggetto;
import com.axiastudio.suite.anagrafiche.entities.Riferimento;
import com.axiastudio.suite.anagrafiche.entities.Soggetto;
import com.axiastudio.suite.anagrafiche.entities.Stato;
import com.axiastudio.suite.anagrafiche.entities.TitoloSoggetto;
import com.axiastudio.suite.anagrafiche.forms.FormIndirizzo;
import com.axiastudio.suite.anagrafiche.forms.FormQuickInsertSoggetto;
import com.axiastudio.suite.anagrafiche.forms.FormRelazioneSoggetto;
import com.axiastudio.suite.anagrafiche.forms.FormSoggetto;
import com.axiastudio.suite.base.Login;
import com.axiastudio.suite.base.entities.Ufficio;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.entities.MovimentoDetermina;
import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
import com.axiastudio.suite.finanziaria.entities.Capitolo;
import com.axiastudio.suite.finanziaria.entities.Servizio;
import com.axiastudio.suite.generale.entities.Costante;
import com.axiastudio.suite.pratiche.PraticaAdapters;
import com.axiastudio.suite.pratiche.PraticaPrivate;
import com.axiastudio.suite.pratiche.entities.Dipendenza;
import com.axiastudio.suite.pratiche.entities.DipendenzaPratica;
import com.axiastudio.suite.pratiche.entities.Pratica;
import com.axiastudio.suite.pratiche.entities.TipoPratica;
import com.axiastudio.suite.pratiche.forms.FormDipendenzaPratica;
import com.axiastudio.suite.pratiche.forms.FormPratica;
import com.axiastudio.suite.procedimenti.GestoreDeleghe;
import com.axiastudio.suite.procedimenti.IGestoreDeleghe;
import com.axiastudio.suite.procedimenti.entities.Delega;
import com.axiastudio.suite.procedimenti.entities.Procedimento;
import com.axiastudio.suite.protocollo.ProtocolloAdapters;
import com.axiastudio.suite.protocollo.ProtocolloPrivate;
import com.axiastudio.suite.protocollo.entities.Fascicolo;
import com.axiastudio.suite.protocollo.entities.PraticaProtocollo;
import com.axiastudio.suite.protocollo.entities.Protocollo;
import com.axiastudio.suite.protocollo.entities.SoggettoProtocollo;
import com.axiastudio.suite.protocollo.forms.FormSoggettoProtocollo;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;
import com.axiastudio.suite.procedimenti.entities.Carica;
import com.axiastudio.suite.procedimenti.entities.Norma;
import com.axiastudio.suite.protocollo.entities.Oggetto;
import com.axiastudio.suite.protocollo.entities.SoggettoRiservatoProtocollo;
import com.axiastudio.suite.protocollo.entities.Titolo;
import com.axiastudio.suite.protocollo.forms.FormProtocollo;
import com.axiastudio.suite.protocollo.forms.FormScrivania;
import com.axiastudio.suite.sedute.entities.CaricaCommissione;
import com.axiastudio.suite.sedute.entities.Commissione;
import com.axiastudio.suite.sedute.entities.Seduta;
import com.axiastudio.suite.sedute.entities.TipoSeduta;
import com.axiastudio.suite.sedute.forms.FormTipoSeduta;
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
        
        String jdbcUrl = System.getProperty("jdbc.url");
        String jdbcUser = System.getProperty("jdbc.user");
        String jdbcPassword = System.getProperty("jdbc.password");
        String jdbcDriver = System.getProperty("jdbc.driver");
        String logLevel = System.getProperty("suite.loglevel");
        String cmisUrl = System.getProperty("cmis.url");
        String cmisUser = System.getProperty("cmis.user");
        String cmisPassword = System.getProperty("cmis.password");

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
        app.setLanguage("it");

        // configurazione originale SuitePA
        Configure.configure(db);
        
        // configurazione personalizzata Riva GDA        
        Register.registerCallbacks(Resolver.callbacksFromClass(ProtocolloCallbacksRiva.class));
        Register.registerCallbacks(Resolver.callbacksFromClass(PraticaCallbacksRiva.class));
        Register.registerForm(db.getEntityManagerFactory(),
                              "classpath:com/axiastudio/suite/protocollo/forms/protocollo.ui",
                              Protocollo.class,
                              FormProtocolloRiva.class);

                
        

        // Plugin CmisPlugin per accedere ad Alfresco
        String templateCmisProtocollo = "/Siti/protocolli/documentLibrary/${dataprotocollo,date,yyyy}/${dataprotocollo,date,MM}/${dataprotocollo,date,dd}/${iddocumento}/";
        CmisPlugin cmisPlugin = new CmisPlugin();
        cmisPlugin.setup(cmisUrl, cmisUser, cmisPassword, 
                templateCmisProtocollo,
                Boolean.FALSE);
        Register.registerPlugin(cmisPlugin, FormProtocolloRiva.class);
        CmisPlugin cmisPlugin2 = new CmisPlugin();
        cmisPlugin2.setup(cmisUrl, cmisUser, cmisPassword, 
                templateCmisProtocollo,
                Boolean.FALSE);
        Register.registerPlugin(cmisPlugin, FormScrivania.class);
        
        /*
        CmisPlugin cmisPluginPubblicazioni = new CmisPlugin();
        cmisPluginPubblicazioni.setup("http://localhost:8080/alfresco/service/cmis", "admin", "admin", 
                "/Pubblicazioni/${inizioconsultazione,date,YYYY}/${inizioconsultazione,date,MM}/${inizioconsultazione,date,dd}/${id}/");
        Register.registerPlugin(cmisPluginPubblicazioni, FormPubblicazione.class);
        */
        
        // Plugin Barcode per la stampa del DataMatrix
        Barcode barcodePlugin = new Barcode();
        barcodePlugin.setup("lp -d Zebra_Technologies_ZTC_GK420t", ".\nS1\nb245,34,D,h6,\"0123456789\"\nP1\n.\n");
        Register.registerPlugin(barcodePlugin, FormProtocolloRiva.class);

        // Plugin OoopsPlugin per interazione con OpenOffice
        OoopsPlugin ooopsPlugin = new OoopsPlugin();
        ooopsPlugin.setup("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");
        
        // template da file system
        HashMap<String,String> rules = new HashMap();
        rules.put("oggetto", "return obj.getDescrizione()");
        RuleSet ruleSet = new RuleSet(rules);
        IStreamProvider streamProvider1 = new FileStreamProvider("/Users/tiziano/NetBeansProjects/PyPaPi/plugins/PyPaPiOoops/template/test.ott");
        Template template = new Template(streamProvider1, "Prova", "Template di prova", ruleSet);
        ooopsPlugin.addTemplate(template);
        
        // template da Cmis
        
        HashMap<String,String> rules2 = new HashMap();        
        rules2.put("oggetto", "return obj.getDescrizione()+\", da Alfresco!!\"");
        RuleSet ruleSet2 = new RuleSet(rules2);
        IStreamProvider streamProvider2 = new CmisStreamProvider("http://localhost:8080/alfresco/service/cmis", "admin", "admin", 
                                                                 "workspace://SpacesStore/7b3a2895-51e7-4f2c-9e3d-cf67f7043257");
        Template template2 = new Template(streamProvider2, "Prova 2", "(template proveniente da Alfresco)", ruleSet2);
        ooopsPlugin.addTemplate(template2);
        
                
        Register.registerPlugin(ooopsPlugin, FormPratica.class);

        // gestore deleghe
        GestoreDeleghe gestoreDeleghe = new GestoreDeleghe();
        Register.registerUtility(gestoreDeleghe, IGestoreDeleghe.class);
        
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

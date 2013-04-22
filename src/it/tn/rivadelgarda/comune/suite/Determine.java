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
import com.axiastudio.pypapi.plugins.cmis.CmisPlugin;
import com.axiastudio.pypapi.plugins.cmis.CmisStreamProvider;
import com.axiastudio.pypapi.plugins.ooops.OoopsPlugin;
import com.axiastudio.pypapi.plugins.ooops.RuleSet;
import com.axiastudio.pypapi.plugins.ooops.Template;
import com.axiastudio.pypapi.ui.Util;
import com.axiastudio.pypapi.ui.Window;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.base.entities.Utente_;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.entities.Determina_;
import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.finanziaria.entities.Servizio;
import com.axiastudio.suite.procedimenti.GestoreDeleghe;
import com.axiastudio.suite.procedimenti.IGestoreDeleghe;
import com.axiastudio.suite.procedimenti.entities.Carica;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 */
public class Determine {
    private static final String ALFRESCO_PASS = "*";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String jdbcUrl = System.getProperty("jdbc.url");
        String jdbcUser = System.getProperty("jdbc.user");
        String jdbcPassword = System.getProperty("jdbc.password");
        String jdbcDriver = System.getProperty("jdbc.driver");
        String suiteUser = System.getProperty("suite.user");
        String suiteIdPratica = System.getProperty("suite.idpratica");
        Map properties = new HashMap();
        if( jdbcUrl != null ){
            properties.put("javax.persistence.jdbc.url", jdbcUrl);
        }
        if( jdbcUser != null ){
            properties.put("javax.persistence.jdbc.user", jdbcUser);
        }
        if( jdbcPassword != null ){
            properties.put("javax.persistence.jdbc.password", jdbcPassword);
        }
        if( jdbcDriver != null ){
            properties.put("javax.persistence.jdbc.driver", jdbcDriver);
        }

        Database db = new Database();
        if( properties.isEmpty() ){
            db.open("SuitePU");
        } else {
            properties.put("eclipselink.ddl-generation", "");
            db.open("SuitePU", properties);
        }
        Register.registerUtility(db, IDatabase.class);
        EntityManagerFactory emf = db.getEntityManagerFactory();
        
        Application app = new Application(args);
        app.setLanguage("it");

        Register.registerForm(db.getEntityManagerFactory(),
                              "classpath:com/axiastudio/suite/base/forms/utente.ui",
                              Utente.class,
                              Window.class,
                              "Utenti");

        Register.registerForm(db.getEntityManagerFactory(),
                              null,
                              Carica.class,
                              Window.class,
                              "Carica");

        Register.registerForm(db.getEntityManagerFactory(),
                              "classpath:com/axiastudio/suite/finanziaria/forms/servizio.ui",
                              Servizio.class,
                              Window.class,
                              "Servizi");

        Register.registerForm(db.getEntityManagerFactory(),
                              "classpath:com/axiastudio/suite/deliberedetermine/forms/determina.ui",
                              Determina.class,
                              FormDeterminaJEnte.class,
                              "Determine");
        
        // gestore deleghe
        GestoreDeleghe gestoreDeleghe = new GestoreDeleghe();
        Register.registerUtility(gestoreDeleghe, IGestoreDeleghe.class);
        
        // callback per svuotare la determina dagli impegni presi da jEnte prima
        // di committare
        Register.registerCallbacks(Resolver.callbacksFromClass(DeterminaCallbacks.class));

        // Plugin CmisPlugin per accedere ad Alfresco
        CmisPlugin cmisPlugin = new CmisPlugin();
        //cmisPlugin.setup("http://localhost:8080/alfresco/service/cmis", "admin", "admin", 
        //        "/Pratiche/${idPratica}/");
        cmisPlugin.setup("http://192.168.64.41:8080/alfresco/service/cmis", "pypapi", ALFRESCO_PASS, 
                "/Siti/pratiche/documentLibrary/${dataPratica,date,yyyy}/${dataPratica,date,MM}/${idPratica}/");
        //Register.registerPlugin(cmisPlugin, FormDeterminaJEnte.class);
        
        // Plugin OoopsPlugin per interazione con OpenOffice
        OoopsPlugin ooopsPlugin = new OoopsPlugin();
        //ooopsPlugin.setup("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");
        ooopsPlugin.setup("uno:socket,host=192.168.64.59,port=2002;urp;StarOffice.ServiceManager");
        //Register.registerPlugin(ooopsPlugin, FormDeterminaJEnte.class);

        // configurazione template determina.ott
        HashMap<String,String> rulesMapDetermina = new HashMap();
        rulesMapDetermina.put("OGGETTO", "return obj.getOggetto()");
        rulesMapDetermina.put("CODICE", "return obj.getCodiceInterno()");
        rulesMapDetermina.put("PRATICA", "return obj.getCodiceInterno()"); // + \"\n\" + ((obj.getUbicazione!=null && \"PRESSO \"))");
        RuleSet rulesSetDetermina = new RuleSet(rulesMapDetermina);
        //IStreamProvider streamProviderDetermina = new CmisStreamProvider("http://localhost:8080/alfresco/service/cmis", "admin", "admin", 
        //                                                         "workspace://SpacesStore/4a561d2b-30cd-42c4-bb80-432426b17c88");
        IStreamProvider streamProviderDetermina = new CmisStreamProvider("http://192.168.64.41:8080/alfresco/service/cmis", "pypapi", ALFRESCO_PASS, 
                                                                 "workspace://SpacesStore/bcfe7d69-bb16-4da7-a6ac-b7e72e176229");
        Template templateDetermina = new Template(streamProviderDetermina, "Determina", "Determinazione del responsabile di servizio", rulesSetDetermina);
        ooopsPlugin.addTemplate(templateDetermina);

        // configurazione template impegni
        HashMap<String,String> rulesMapImpegni = new HashMap();
        rulesMapImpegni.put("idpratica", "return obj.getIdPratica()");
        RuleSet rulesSetImpegni = new RuleSet(rulesMapImpegni);
        //IStreamProvider streamProviderImpegni = new CmisStreamProvider("http://localhost:8080/alfresco/service/cmis", "admin", "admin", 
        //                                                         "workspace://SpacesStore/62906e73-e3c2-4c28-9688-8be590a0c489");
        IStreamProvider streamProviderImpegni = new CmisStreamProvider("http://192.168.64.41:8080/alfresco/service/cmis", "pypapi", ALFRESCO_PASS, 
                                                                 "workspace://SpacesStore/e5044e25-ac4b-4fa3-bd9f-2dac78af9862");
        Template templateImpegni = new Template(streamProviderImpegni, "Impegni", "Impegni della determinazione del responsabile di servizio", rulesSetImpegni);
        ooopsPlugin.addTemplate(templateImpegni);
        
       // login
        EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Utente> cq = cb.createQuery(Utente.class);
        Root from = cq.from(Utente.class);
        cq.select(from);
        Predicate predicate = cb.equal(from.get(Utente_.login), suiteUser);
        cq = cq.where(predicate);
        Query q = em.createQuery(cq);
        List<Utente> entities = q.getResultList();
        if( entities.size() == 1 ){
            Utente utente = entities.get(0);
            Register.registerUtility(utente, IUtente.class);
        } else {
            System.out.println("Utente non presente");
            System.exit(1);
        }
 
        // utilità per la gestione degli impegni
        FinanziariaUtil finanziariaUtil = new FinanziariaUtil(suiteUser);
        //FinanziariaUtilFake finanziariaUtil = new FinanziariaUtilFake(suiteUser);
        Register.registerUtility(finanziariaUtil, IFinanziaria.class);
        
 
        // selezione della pratica
        CriteriaBuilder cb2 = em.getCriteriaBuilder();
        CriteriaQuery<Determina> cq2 = cb2.createQuery(Determina.class);
        Root from2 = cq2.from(Determina.class);
        cq2.select(from2);
        Predicate predicate2 = cb2.equal(from2.get(Determina_.idpratica), suiteIdPratica);
        cq2 = cq2.where(predicate2);
        Query q2 = em.createQuery(cq2);
        List<Determina> determine = q2.getResultList();
        Determina determina=null;
        if( determine.isEmpty() ){
            determina = new Determina();
            determina.setIdpratica(suiteIdPratica);
            
            // la data dovrà essere pescata dalla pratica!
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date d;
            try {
                d = sdf.parse("2009/01/01");
            } catch (ParseException ex) {
                d = new Date();
            }
            determina.setDataPratica(d);
            determina.setVistoBilancio(Boolean.FALSE);
            determina.setVistoResponsabile(Boolean.FALSE);
            determina.setVistoNegato(Boolean.FALSE);
            determina.setDiSpesa(Boolean.FALSE);
            em.getTransaction().begin();
            em.persist(determina);
            em.getTransaction().commit();
            System.out.println("Creata determina "+suiteIdPratica);
        } else if( determine.size() == 1 ){
            determina = determine.get(0);
            System.out.println("Selezionata determina "+suiteIdPratica);
        } else {
            System.out.println("Più determine con lo stesso IdPratica.");
            System.exit(1);
        }
        //Window form = Util.formFromName(Determina.class.getName());
        FormDetermina form = (FormDetermina) Util.formFromEntity(determina);
        form.show();
        
        app.exec();
        System.exit(1);
    }
}

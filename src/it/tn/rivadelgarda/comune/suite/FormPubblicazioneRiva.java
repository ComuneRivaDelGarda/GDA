/*
 * Copyright (C) 2012 AXIA Studio (http://www.axiastudio.com)
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

import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.pypapi.Application;
import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.ui.Util;
import com.axiastudio.suite.plugins.atm.FileATM;
import com.axiastudio.suite.plugins.atm.PubblicazioneATM;
import com.axiastudio.suite.plugins.atm.helper.PutAttoHelper;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QWidget;
import it.tn.rivadelgarda.comune.gda.WebAppBridge;
import it.tn.rivadelgarda.comune.gda.WebAppBridgeBuilder;
import it.tn.rivadelgarda.comune.gda.docer.DocerHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 */
public class FormPubblicazioneRiva extends FormPubblicazione {

    public FormPubblicazioneRiva(String uiFile, Class entityClass, String title){
        super(uiFile, entityClass, title);
    }

    public void pubblicaOra(){


        QSpinBox n_giorni = (QSpinBox) findChild(QSpinBox.class, "spinBox_n_giorni");
        int n = n_giorni.value();
        if( n == 0 ){
            Util.warningBox(this, "Dati di pubblicazione errati", "Deve essere indicato un numero di giorni di consultazione");
            return;
        }

        Pubblicazione pubblicazione = (Pubblicazione) this.getContext().getCurrentEntity();
        String externalId = "pubblicazione_" + pubblicazione.getId();

        PubblicazioneATM pubblicazioneATM = new PubblicazioneATM();

        String descrizione = pubblicazione.getDescrizione().replaceAll("'", "`");
        descrizione = descrizione.replaceAll("\"", "`");
        descrizione = descrizione.replaceAll("\n", " ");
        pubblicazioneATM.setDescrizione(descrizione);

        if( pubblicazione.getDataatto() != null ) {
            pubblicazioneATM.setDataatto(pubblicazione.getDataatto());
        } else {
            Calendar calendar = Calendar.getInstance(Locale.ITALIAN);
            pubblicazioneATM.setDataatto(calendar.getTime());
        }

        if( pubblicazione.getNumeroatto() != null  ) {
            pubblicazioneATM.setNumeroatto(pubblicazione.getNumeroatto());
        }
        pubblicazioneATM.setDurataconsultazione(pubblicazione.getDurataconsultazione());
        pubblicazioneATM.setRichiedente(pubblicazione.getRichiedente());
        pubblicazioneATM.setOrgano(pubblicazione.getOrgano());
        pubblicazioneATM.setTipoatto(pubblicazione.getTipoattopubblicazione().getChiave());

        PutAttoHelper helper = new PutAttoHelper();

        Application app = Application.getApplicationInstance();
        helper.setup((String) app.getConfigItem("atm.userID"),
                (String) app.getConfigItem("atm.password"),
                (String) app.getConfigItem("atm.MAC"),
                (String) app.getConfigItem("atm.wsakey"),
                (String) app.getConfigItem("atm.endpoint"));

        // documento e allegati
        DocerHelper docerHelper = new DocerHelper((String)app.getConfigItem("docer.url"), (String) app.getConfigItem("docer.username"),
                (String) app.getConfigItem("docer.password"));

        List<FileATM> allegati = new ArrayList<FileATM>();
        Integer i=0;
        try {
            List<Map<String, String>> folderDocuments = docerHelper.searchDocumentsByExternalIdFirstAndRelated(externalId);
            for( Map<String, String> doc: folderDocuments){
                String documentId = doc.get("DOCNUM");
                String tipo_componente = doc.get("TIPO_COMPONENTE");
                String name = doc.get("DOCNAME");
                String title = doc.get("ABSTRACT");
                InputStream inputStream = docerHelper.getDocumentStream(documentId, "1");
                FileATM fileATM = new FileATM();
                fileATM.setTitoloallegato(title);
                fileATM.setFileallegato(inputStream);
                fileATM.setFileallegatoname(name);
                if( "PRINCIPALE".equals(tipo_componente) ) {
                    pubblicazioneATM.setFileAtto(fileATM);
                } else if( "ALLEGATO".equals(tipo_componente) ){
                    allegati.add(fileATM);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        pubblicazioneATM.setAllegati(allegati);
        boolean res = helper.putAtto(pubblicazioneATM);
        if( !res ){
            Util.warningBox(this, "Errore in pubblicazione", "L'atto non è stato pubblicato all'albo.");
        } else {
            Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
            pubblicazione.setPubblicato(Boolean.TRUE);
            pubblicazione.setEsecutorepubblicazione(autenticato.getLogin());
            this.getContext().commitChanges();
            Util.warningBox(this, "Pubblicazione avvenuta", "L'atto è stato pubblicato all'albo, verificare su http://www.albotelematico.tn.it/bacheca/riva-del-garda.");
        }
    }

    private Properties loadConfig() {
        Properties p = new Properties();

        try {
            p.load(getClass().getResourceAsStream("putatto.properties"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return p;

    }

    @Override
    protected void indexChanged(int row) {
        super.indexChanged(row);
        QAction action = (QAction) this.findChild(QAction.class, "showForm");
        action.triggered.disconnect();
        action.triggered.connect(this, "apriDocumento()");

        Pubblicazione pubblicazione = (Pubblicazione) this.getContext().getCurrentEntity();
        // Pubblicazione inviata all'albo: disabilitazione di tutto tranne data fine pubblicazione (per ri-pubblicazione)
        String[] roWidgets = {"dateEdit_dataatto", "spinBox_numeroatto", "comboBox_TipoAtto", "lineEdit_Richiedente",
                "lineEdit_Organo", "textEdit_Descrizione", ""};
        for (String widgetName : roWidgets) {
            Util.setWidgetReadOnly((QWidget) this.findChild(QWidget.class, widgetName), pubblicazione.getPubblicato());
        }
    }

    private void apriDocumento(){

        Pubblicazione pubblicazione = (Pubblicazione) this.getContext().getCurrentEntity();
        String externalId = "pubblicazione_" + pubblicazione.getId();
        if (pubblicazione == null || pubblicazione.getId() == null) {
            return;
        }
        Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
        Application app = Application.getApplicationInstance();
        DocerHelper helper = new DocerHelper((String)app.getConfigItem("docer.url"), (String) app.getConfigItem("docer.username"),
                (String) app.getConfigItem("docer.password"));

        String url = "http://192.168.64.200:8080/gdadocer/index.html#?externalId=" + externalId;
        url += "&utente=" + autenticato.getNome().toUpperCase();
        Boolean view = false;
        Boolean download = false;
        Boolean parent = false;
        Boolean delete;
        Boolean upload;
        Boolean version;
        if ( pubblicazione.getPubblicato() ){
            delete = Boolean.FALSE;
            upload = Boolean.FALSE;
            version = Boolean.FALSE;
        } else {
            delete = Boolean.TRUE;
            upload = Boolean.TRUE;
            version = Boolean.TRUE;
        }
        String flags="";
        for( Boolean flag: Arrays.asList(view, delete, download, parent, upload, version) ){
            flags += flag ? "1" :  "0";
        }
        url += "&flags=" + flags;

        WebAppBridge bridge = WebAppBridgeBuilder.create()
                .url(url)
//                .developerExtrasEnabled(Boolean.TRUE)                       // abilità "inspect" dal menu contestuale
                .javaScriptEnabled(Boolean.TRUE)                            // abilità l'esecuzione di JavaScript
//                .javaScriptCanOpenWindows(Boolean.TRUE)                     // JS può aprire finestre in popup
//                .javaScriptCanCloseWindows(Boolean.TRUE)                    // JS può chiudere finestre
//                .javaScriptCanAccessClipboard(Boolean.TRUE)                 // JS legge e scrive da clipboard
                .cookieJarEnabled(Boolean.TRUE)                             // la pagina può impostare dei cookie
                .downloadEnabled(Boolean.TRUE)                              // download abilitati
//                .downloadContentTypes(new String[]{"application/pdf"})      // content type permessi al download
                .downloadPath("/home/pivamichela")                   // cartella proposta per il download
//                .loadFinishedCallback(callbackClass, "callback()")          // callback da eseguire a pagina caricata
                .build();

        bridge.show();


    }


}

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

import com.axiastudio.pypapi.plugins.IPlugin;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.menjazo.AlfrescoHelper;
import com.axiastudio.pypapi.Application;
import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.ui.Util;
import com.axiastudio.suite.plugins.atm.FileATM;
import com.axiastudio.suite.plugins.atm.PubblicazioneATM;
import com.axiastudio.suite.plugins.atm.helper.PutAttoHelper;
import com.axiastudio.suite.plugins.cmis.CmisPlugin;
import com.axiastudio.suite.plugins.cmis.CmisStreamProvider;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QWidget;

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
        CmisPlugin cmisPlugin = (CmisPlugin) Register.queryPlugin(pubblicazione.getClass(), "CMIS");
        AlfrescoHelper cmisHelper = cmisPlugin.createAlfrescoHelper(pubblicazione);
        Boolean isAtto=Boolean.TRUE;
        List<FileATM> allegati = new ArrayList<FileATM>();
        Integer i=0;
        for( Map child: cmisHelper.children() ){
            String name = (String) child.get("name");
            String title = (String) child.get("title");
            if( title == null ){
                if( i==0 ){
                    title = "atto";
                } else {
                    title = "allegato " + i;
                }
            }
            CmisStreamProvider streamProvider = cmisPlugin.createCmisStreamProvider((String) child.get("objectId"));
            InputStream inputStream = streamProvider.getInputStream();
            FileATM fileATM = new FileATM();
            fileATM.setTitoloallegato(title);
            fileATM.setFileallegato(inputStream);
            fileATM.setFileallegatoname(name);
            if( isAtto ) {
                pubblicazioneATM.setFileAtto(fileATM);
                isAtto = Boolean.FALSE;
            } else {
                allegati.add(fileATM);
            }
            i++;
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

    public void apriDocumento() {
        Pubblicazione pubblicazione = (Pubblicazione) this.getContext().getCurrentEntity();
        if (pubblicazione == null || pubblicazione.getId() == null) {
            return;
        }
        List<IPlugin> plugins = (List) Register.queryPlugins(this.getClass());
        for (IPlugin plugin : plugins) {
            if ("CMIS".equals(plugin.getName())) {
                if ( pubblicazione.getPubblicato() ){
                    ((CmisPlugin) plugin).showForm(pubblicazione, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE,
                            Boolean.FALSE, Boolean.FALSE, new HashMap());
                } else {
                    ((CmisPlugin) plugin).showForm(pubblicazione, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE,
                            Boolean.TRUE, Boolean.TRUE, new HashMap());
                }
            }
        }
    }

}

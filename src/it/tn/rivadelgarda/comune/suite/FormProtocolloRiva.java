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

import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.ui.Util;
import com.axiastudio.pypapi.ui.widgets.PyPaPiTableView;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.protocollo.ProfiloUtenteProtocollo;
import com.axiastudio.suite.protocollo.entities.Protocollo;
import com.axiastudio.suite.protocollo.forms.FormProtocollo;
import com.trolltech.qt.gui.QToolButton;
import com.trolltech.qt.gui.QWidget;


/**
 *
 * @author Tiziano Lattisi <tiziano at axiastudio.it>
 */
public class FormProtocolloRiva extends FormProtocollo {
    //private  ProtocolloMenuBar protocolloMenuBar=null;
    //private QTabWidget tabWidget;
    
    
    public FormProtocolloRiva(FormProtocolloRiva form){
        super(form.uiFile, form.entityClass, form.title);
    }
    
    public FormProtocolloRiva(String uiFile, Class entityClass, String title){
        super(uiFile, entityClass, title);
    }
    
    @Override
    protected void indexChanged(int row) {
        
        Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
        Protocollo protocollo = (Protocollo) this.getContext().getCurrentEntity();
        ProfiloUtenteProtocollo profilo = new ProfiloUtenteProtocollo(protocollo, autenticato);
        Boolean nuovoInserimento = protocollo.getId() == null;
        
        super.indexChanged(row);
        /*if( nuovoInserimento ){
            return;
        }*/
        // modifica protocollo convalidato
        String[] roWidgets = {"textEdit_oggetto", "tableView_soggettiprotocollo",
            "tableView_soggettiriservatiprotocollo", "tableView_ufficiprotocollo",
            "comboBoxTitolario", "comboBox_tiporiferimentomittente", "lineEdit_nrriferimentomittente",
            "dateEdit_datariferimentomittente", "richiederisposta", "spedito", "riservato",
            "corrispostoostornato"};
        for( String widgetName: roWidgets ){
            Util.setWidgetReadOnly((QWidget) this.findChild(QWidget.class, widgetName), protocollo.getConvalidaprotocollo());
        }
        ((QToolButton) this.findChild(QToolButton.class, "toolButtonTitolario")).setEnabled(!protocollo.getConvalidaprotocollo());
        
        // Se non è un nuovo inserimento, metto tutti i campi in readonly

        // Visibilità dei soggetti riservati
        PyPaPiTableView tvSoggettiRiservati =  (PyPaPiTableView) this.findChild(PyPaPiTableView.class, "tableView_soggettiriservatiprotocollo");
        //tvSoggettiRiservati.setReadOnly(!(nuovoInserimento || profilo.inSportelloOAttribuzioneR()));
        if( !(nuovoInserimento || profilo.inSportelloOAttribuzioneR()) ){
            tvSoggettiRiservati.hide();
        } else {
            tvSoggettiRiservati.show();
        }
        
        // Se attributore protocollo modifica anche a protocollo con attribuzioni convalidate,
        // altrimenti solo se in attribuzione principale o sportello.
        PyPaPiTableView tableViewAttribuzioni = (PyPaPiTableView) this.findChild(PyPaPiTableView.class, "tableView_attribuzioni");
        Boolean modificaAttribuzioni = nuovoInserimento || autenticato.getAttributoreprotocollo() || (!protocollo.getConvalidaattribuzioni() && profilo.inSportelloOAttribuzionePrincipale());
        //Util.setWidgetReadOnly(tableViewAttribuzioni, !modificaAttribuzioni);
        tableViewAttribuzioni.setEnabled(modificaAttribuzioni); // XXX: altrimenti si sposta l'attribuzione principale...

        /* protocollo annullato? */
        if( protocollo.getAnnullato() ){
            // TODO: provare qualche alternativa...
            this.setStyleSheet("color: red;");
        } else {
            this.setStyleSheet("");
        }
    }

}

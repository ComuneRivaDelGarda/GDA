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
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Ufficio;
import com.axiastudio.suite.base.entities.UfficioUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.protocollo.entities.Attribuzione;
import com.axiastudio.suite.protocollo.entities.Protocollo;
import com.axiastudio.suite.protocollo.forms.FormProtocollo;
import java.util.ArrayList;
import java.util.List;


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
        
        /* Comportamento form da permessi tabellati */
        Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
        Protocollo protocollo = (Protocollo) this.getContext().getCurrentEntity();
        Ufficio sportello = protocollo.getSportello();
        List<Ufficio> attribuzioni = new ArrayList();
        Ufficio attribuzionePrincipale = null;
        for( Attribuzione attribuzione: protocollo.getAttribuzioneCollection() ){
            attribuzioni.add(attribuzione.getUfficio());
            if( attribuzione.getPrincipale() ){
                attribuzionePrincipale = attribuzione.getUfficio();
            }
        }
        List<Ufficio> ufficiUtente = new ArrayList();
        List<Ufficio> ufficiRicercaUtente = new ArrayList();
        List<Ufficio> ufficiRiservatoUtente = new ArrayList();
        for(UfficioUtente uu: autenticato.getUfficioUtenteCollection()){
            ufficiUtente.add(uu.getUfficio());
            if( uu.getRicerca() ){
                ufficiRicercaUtente.add(uu.getUfficio());
            }
            if( uu.getPrivato() ){
                ufficiRiservatoUtente.add(uu.getUfficio());
            }
        }
        
        // Profilo dell'utente
        List intersezione = new ArrayList(attribuzioni);
        intersezione.retainAll(ufficiUtente);
        Boolean inSportelloOAttribuzione = ufficiUtente.contains(sportello) || intersezione.size()>0;
        Boolean inSportelloOAttribuzionePrincipale = ufficiUtente.contains(sportello) || ufficiUtente.contains(attribuzionePrincipale);
        intersezione = new ArrayList(attribuzioni);
        intersezione.retainAll(ufficiUtente);
        Boolean inSportelloOAttribuzioneR = ufficiRiservatoUtente.contains(sportello) || intersezione.size()>0;
        Boolean inSportelloOAttribuzionePrincipaleR = ufficiRiservatoUtente.contains(sportello) || ufficiRiservatoUtente.contains(attribuzionePrincipale);
        Boolean neSportelloNeAttribuzione = !(inSportelloOAttribuzione || inSportelloOAttribuzioneR);
        
        
        /*
        Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
        Protocollo protocollo = (Protocollo) this.getContext().getCurrentEntity();
        Ufficio sportello = protocollo.getSportello();
        Ufficio attribuzionePrincipale = null;
        for( Attribuzione attribuzione: protocollo.getAttribuzioneCollection() ){
            if( attribuzione.getPrincipale() ){
                attribuzionePrincipale = attribuzione.getUfficio();
                break;
            }
        }
        Boolean convAttribuzioni = protocollo.getConvalidaAttribuzioni() == true;
        Boolean convProtocollo = protocollo.getConvalidaProtocollo() == true;
        Boolean nuovoInserimento = protocollo.getId() == null;
        
        /* uffici dell'utente autenticato *
        List<Ufficio> uffici = new ArrayList();
        List<Ufficio> ufficiRicerca = new ArrayList();
        for(UfficioUtente uu: autenticato.getUfficioUtenteCollection()){
            uffici.add(uu.getUfficio());
            if( uu.getRicerca() ){
                ufficiRicerca.add(uu.getUfficio());
            }
        }
        Boolean modificaProtocollo=false;
        if( nuovoInserimento ){
            modificaProtocollo=true;
        } else if( uffici.contains(sportello) ){
            modificaProtocollo=true;
        } else if( ufficiRicerca.contains(attribuzionePrincipale) ){
            modificaProtocollo=true;
        }
        
        /* Abilitazione dei pulsanti di convalida *
        this.protocolloMenuBar.actionByName("convalidaAttribuzioni").setEnabled(!convAttribuzioni);
        this.protocolloMenuBar.actionByName("convalidaProtocollo").setEnabled(!convProtocollo);

        /* Prima verifico i campi privati *
        super.indexChanged(row);
        
        /* 
         * Le condizione per il read only (in or):
         * 
         * - non è un nuovo inserimento
         * - il protocollo è convalidato
         * - non hai il permesso di modifica
         * 
         *
        Boolean readOnly = false;
        if( convProtocollo || !modificaProtocollo){
            readOnly = true;
        }
        
        /* Metto eventualmente in read only i campi *
        for( QObject object: this.getWidgets().values() ){
            Util.setWidgetReadOnly((QWidget) object, readOnly);
        }
        
        /* Il campo note è modificabile anche a protocollo convalidato *
        Util.setWidgetReadOnly((QWidget) this.findChild(QTextEdit.class, "textEdit_note"), !modificaProtocollo);
        
        /* Se  sei attributore puoi sempre, altrimenti solo se non è read only e non sono convalidate le attribuzioni *
        if( autenticato.getAttributoreprotocollo() ){
            Util.setWidgetReadOnly((QWidget) this.findChild(PyPaPiTableView.class, "tableView_attribuzioni"), false );
        } else {
            Util.setWidgetReadOnly((QWidget) this.findChild(PyPaPiTableView.class, "tableView_attribuzioni"), readOnly || convAttribuzioni );
        }
        
        /* id documento, data, e gestione annullamento sempre read only *
        //Util.setWidgetReadOnly((QWidget) this.findChild(QLineEdit.class, "lineEdit_iddocumento"), true);
        //Util.setWidgetReadOnly((QWidget) this.findChild(QDateEdit.class, "dateEdit_data"), true);
        //Util.setWidgetReadOnly((QWidget) this.findChild(QCheckBox.class, "annullato"), true);
        //Util.setWidgetReadOnly((QWidget) this.findChild(QCheckBox.class, "annullamentorichiesto"), true);
        
        /* Inserimento pratiche permesso solo agli utenti appartenenti allo sportello o a una attribuzione *
        Boolean inserimentoPratica=false;
        if( uffici.contains(sportello) ){
            inserimentoPratica=true;
        } else {
            for( Attribuzione attribuzione: protocollo.getAttribuzioneCollection() ){
                if( uffici.contains(attribuzione.getUfficio()) ){
                    inserimentoPratica=true;
                    break;
                }
            }
        }
        Util.setWidgetReadOnly((QWidget) this.findChild(PyPaPiTableView.class, "tableView_pratiche"), (!inserimentoPratica || !modificaProtocollo));
        
        super.indexChanged(row);
        */
    }

}

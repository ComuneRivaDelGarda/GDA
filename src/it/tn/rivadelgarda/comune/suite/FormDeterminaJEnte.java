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
import com.axiastudio.pypapi.ui.Context;
import com.axiastudio.pypapi.ui.Window;
import com.axiastudio.pypapi.ui.widgets.PyPaPiToolBar;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Ufficio;
import com.axiastudio.suite.base.entities.UfficioUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.entities.MovimentoDetermina;
import com.axiastudio.suite.deliberedetermine.forms.DeterminaToolbar;
import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.procedimenti.GestoreDeleghe;
import com.axiastudio.suite.procedimenti.IGestoreDeleghe;
import com.axiastudio.suite.procedimenti.TitoloDelega;
import com.axiastudio.suite.procedimenti.entities.CodiceCarica;
import com.trolltech.qt.gui.QCheckBox;

import java.util.List;

class GestoreMovimentiMenuBar extends PyPaPiToolBar {
    public GestoreMovimentiMenuBar(String title, Window parent){
        super(title, parent);
        this.insertButton("apriGestoreMovimenti", "Gestore movimenti",
                "classpath:com/axiastudio/pypapi/plugins/jente/resources/jente.png",
                "Apri la maschera di gestione dei movimenti", parent);
        this.actionByName("apriGestoreMovimenti").setEnabled(false);
    }
}

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 * 
 * Estensione della form di gestione delle determine di Suite PA, per
 * l'integrazione della business logic dipendente da jEnte
 * 
 * Alla firma del responsabile viene richiesta la trasformazione della bozza
 * in atto.
 * 
 * 
 * 
 */
public class FormDeterminaJEnte extends FormDetermina {

    private Determina precedente=null;
    private GestoreMovimentiMenuBar gestoreMovimenti;
    private DeterminaToolbar determinaToolbar;

    public FormDeterminaJEnte(String uiFile, Class entityClass, String title){
        super(uiFile, entityClass, title);
        gestoreMovimenti = new GestoreMovimentiMenuBar("Gestore movimenti", this);
        this.addToolBar(gestoreMovimenti);
        determinaToolbar = new DeterminaToolbar("Dettaglio", this);
        addToolBar(determinaToolbar);
    }


    @Override
    protected void indexChanged(int row) {
        if( precedente != null ){
            this.getContext().getController().refresh(precedente);
        }
        Determina determina = (Determina) this.getContext().getCurrentEntity();
        precedente = determina;
        if( determina.getId() != null ){
            IFinanziaria finanziariaUtil = (IFinanziaria) Register.queryUtility(IFinanziaria.class);
            List<MovimentoDetermina> movimenti = finanziariaUtil.getMovimentiDetermina(determina);
            determina.setMovimentoDeterminaCollection(movimenti);
            this.gestoreMovimenti.actionByName("apriGestoreMovimenti").setEnabled(
                    ((QCheckBox) this.findChild(QCheckBox.class, "checkBox_spesa")).isChecked() ||
                    ((QCheckBox) this.findChild(QCheckBox.class, "checkBox_entrata")).isChecked() || movimenti.size()>0);

            Boolean vediDocumenti=Boolean.FALSE;
            Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
            // Segretario abilitato
            GestoreDeleghe gestoreDeleghe = (GestoreDeleghe) Register.queryUtility(IGestoreDeleghe.class);
//            TitoloDelega vediDocumenticheck = gestoreDeleghe.checkTitoloODelega(CodiceCarica.SEGRETARIO);
            if( gestoreDeleghe.checkTitoloODelega(CodiceCarica.SEGRETARIO) != null ){
                vediDocumenti = Boolean.TRUE;
            }
            // Responsabile di servizio abilitato
            else if( gestoreDeleghe.checkTitoloODelega(CodiceCarica.RESPONSABILE_DI_SERVIZIO, determina.getServizio()) != null ){
                vediDocumenti = Boolean.TRUE;
            }
            // Utenti "gestori Determine"
            else if( gestoreDeleghe.checkTitoloODelega(CodiceCarica.GESTORE_DETERMINA) != null ){
                vediDocumenti = Boolean.TRUE;
            }
            // Utenti appartenenti all'ufficio gestore della pratica
            else if( utenteInUfficio(autenticato, determina.getPratica().getGestione()) != null &&
                        utenteInUfficio(autenticato, determina.getPratica().getGestione()).getModificapratica()){
                    vediDocumenti = Boolean.TRUE;
            }
            // Istruttore e responsabile della pratica
            // TODO
            this.determinaToolbar.actionByName("apriDocumenti").setEnabled(vediDocumenti);

            // faccio credere al contesto che il contesto padre è cambiato, quindi lo spingo ad aggiornarsi
            Context context = (Context) Register.queryRelation(this, ".movimentoDeterminaCollection");
            context.refreshContext();
            super.indexChanged(row);
        }
    }
    
    private void apriGestoreMovimenti() {
        Determina determina = (Determina) this.getContext().getCurrentEntity();
        IFinanziaria finanziariaUtil = (IFinanziaria) Register.queryUtility(IFinanziaria.class);
        finanziariaUtil.apriGestoreMovimenti(determina);
    }

    private UfficioUtente utenteInUfficio(Utente utente, Ufficio ufficio) {
        for (UfficioUtente ufficioUt: utente.getUfficioUtenteCollection()) {
            if ( ufficioUt.getUfficio().equals(ufficio) ) {
                return ufficioUt;
            }
        }
        return null;
    }

}

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
import com.axiastudio.pypapi.plugins.jente.FormMovimenti;
import com.axiastudio.pypapi.plugins.jente.JEnteHelper;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.entities.MovimentoDetermina;
import com.axiastudio.suite.deliberedetermine.entities.ServizioDetermina;
import com.axiastudio.suite.finanziaria.entities.Capitolo;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.pypapi.plugins.jente.webservices.Movimento;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 */
public class FinanziariaUtil implements IFinanziaria {

    public FinanziariaUtil() {
    }

    @Override
    public List<MovimentoDetermina> getMovimentiDetermina(Determina determina) {
        String organoSettore = "DT";
        String anno;
        String attoOBozza;
        String numero;
        if( determina.getNumero() == null ){
            attoOBozza = "B";
            numero = ((Integer) Integer.parseInt(determina.getPratica().getIdpratica().substring(4))).toString();
            anno = determina.getPratica().getIdpratica().substring(0, 4);
        } else {
            attoOBozza = "A";
            numero = determina.getNumero().toString();
            anno = determina.getAnno().toString();
        }
        Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
        JEnteHelper jEnteHelper = new JEnteHelper(autenticato.getLogin());
        List<Movimento> movimenti = jEnteHelper.chiamataRichiestaElencoMovimenti(attoOBozza, organoSettore, anno, numero);
        List<MovimentoDetermina> impegni = new ArrayList();
        if( movimenti != null ){
            for( Movimento movimentoJEnte: movimenti ){
                MovimentoDetermina movimento = new MovimentoDetermina();
                Capitolo capitolo = new Capitolo();
                capitolo.setDescrizione(movimentoJEnte.getMovImpAcce().getCapitolo()+"-"+movimentoJEnte.getMovImpAcce().getDescCapitolo());
                movimento.setCapitolo(capitolo);
                movimento.setArticolo(movimentoJEnte.getMovImpAcce().getArticolo());
                movimento.setImpegno(movimentoJEnte.getMovImpAcce().getNumeroImpacc());
                movimento.setCodiceMeccanografico(movimentoJEnte.getMovImpAcce().getCodMeccanografico());
                movimento.setSottoImpegno(movimentoJEnte.getMovImpAcce().getSubImpacc());
                BigDecimal importo = new BigDecimal(movimentoJEnte.getMovImpAcce().getImporto().replace(".", "").replace(",", "."));
                movimento.setImporto(importo);
                
                impegni.add(movimento);
            }
        }
        return impegni;
    }

    /*
     * Gestione esterna dei movimenti
     */
    @Override
    public void apriGestoreMovimenti(Determina determina) {
        String utente = ((Utente) Register.queryUtility(IUtente.class)).getLogin();
        Boolean vistoResponsabile = determina.getVistoResponsabile();
        String rProc=null;
        String organoSettore = "DT";
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String annoBozza = determina.getPratica().getIdpratica().substring(0, 4);
        String numeroBozza = ((Integer) Integer.parseInt(determina.getPratica().getIdpratica().substring(4))).toString();
        String annoAtto=null;
        String numeroAtto = null;
        if( determina.getNumero() != null ){
            annoAtto = determina.getAnno().toString();
            numeroAtto = determina.getNumero().toString();
        }
        String data = dateFormat.format(determina.getPratica().getDatapratica());
        for( ServizioDetermina servizioDetermina: determina.getServizioDeterminaCollection() ){
            rProc = String.format("%04d", servizioDetermina.getServizio().getId());
            break;
        }        

        String dataVistoResponsabile = null;
        if( determina.getDataVistoResponsabile() != null ){
            dataVistoResponsabile = dateFormat.format(determina.getDataVistoResponsabile());
        }
        FormMovimenti form = new FormMovimenti(annoBozza, organoSettore, numeroBozza, annoAtto, organoSettore, numeroAtto, utente, rProc, vistoResponsabile, data, dataVistoResponsabile);
        form.show();
    }
    
}

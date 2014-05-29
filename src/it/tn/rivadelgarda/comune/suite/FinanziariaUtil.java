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
import com.axiastudio.pypapi.plugins.jente.webservices.Movimento;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.entities.MovimentoDetermina;
import com.axiastudio.suite.deliberedetermine.entities.ServizioDetermina;
import com.axiastudio.suite.finanziaria.entities.Capitolo;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.pratiche.entities.Visto;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        if( determina.getNumero() == null || determina.getNumero() == 0 ){
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
                if ( movimentoJEnte.getMovImpAcce()!=null ) {
                    MovimentoDetermina movimento = new MovimentoDetermina();
                    Capitolo capitolo = new Capitolo();
                    if (movimentoJEnte.getMovImpAcce().getEsercizio()!=null && !movimentoJEnte.getMovImpAcce().getEsercizio().equals("")) {
                        movimento.setAnnoEsercizio(Long.parseLong(movimentoJEnte.getMovImpAcce().getEsercizio()));
                    }
                    capitolo.setNumero(movimentoJEnte.getMovImpAcce().getCapitolo());
                    capitolo.setDescrizione(movimentoJEnte.getMovImpAcce().getCapitolo()+"-"+movimentoJEnte.getMovImpAcce().getDescCapitolo());
                    movimento.setCapitolo(capitolo);
                    movimento.setArticolo(movimentoJEnte.getMovImpAcce().getArticolo());
                    movimento.setArchivio(movimentoJEnte.getMovImpAcce().getArchivio());
                    movimento.setEu(movimentoJEnte.getMovImpAcce().getEu());
                    movimento.setTipoMovimento(movimentoJEnte.getMovImpAcce().getTipoMovimento());
                    movimento.setImpegno(movimentoJEnte.getMovImpAcce().getNumeroImpacc());
                    if (movimentoJEnte.getMovImpAcce().getAnnoImpacc()!=null && !movimentoJEnte.getMovImpAcce().getAnnoImpacc().equals("")) {
                        movimento.setAnnoImpegno(Long.parseLong(movimentoJEnte.getMovImpAcce().getAnnoImpacc()));
                    }
                    movimento.setCodiceMeccanografico(movimentoJEnte.getMovImpAcce().getCodMeccanografico());
                    movimento.setSottoImpegno(movimentoJEnte.getMovImpAcce().getSubImpacc());
                    BigDecimal importo = new BigDecimal(movimentoJEnte.getMovImpAcce().getImporto().replace(".", "").replace(",", "."));
                    movimento.setImporto(importo);
                    if (movimentoJEnte.getMovImpAcce().getImportoImpacc()!=null && !movimentoJEnte.getMovImpAcce().getImportoImpacc().equals("")) {
                        BigDecimal importoTot = new BigDecimal(movimentoJEnte.getMovImpAcce().getImporto().replace(".", "").replace(",", "."));
                        movimento.setImportoImpegnoAccertamento(importoTot);
                    }
                    if (movimentoJEnte.getMovImpAcce().getCodDebBen()!=null && !movimentoJEnte.getMovImpAcce().getCodDebBen().equals("")) {
                        movimento.setCodiceBeneficiario(Long.parseLong(movimentoJEnte.getMovImpAcce().getCodDebBen()));
                    }
                    movimento.setDescrizioneBeneficiario(movimentoJEnte.getMovImpAcce().getDescCodDebBen());
                    movimento.setCodiceCup(movimentoJEnte.getMovImpAcce().getCodiceCup());
                    movimento.setCodiceCig(movimentoJEnte.getMovImpAcce().getCodiceCig());
                    movimento.setCespite(movimentoJEnte.getMovImpAcce().getCespite());
                    movimento.setDescrizioneCespite(movimentoJEnte.getMovImpAcce().getDescCespite());

                    impegni.add(movimento);
                }
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
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Visto visto = determina.getVistoResponsabile();
        Boolean vistoResponsabile=false;
        String dataVistoResponsabile=null;
        if( visto != null ){
            vistoResponsabile = !visto.getNegato();
            dataVistoResponsabile = dateFormat.format(visto.getData());
        }
        String rProc=null;
        String organoSettore = "DT";
        String annoBozza = determina.getPratica().getIdpratica().substring(0, 4);
        String numeroBozza = ((Integer) Integer.parseInt(determina.getPratica().getIdpratica().substring(4))).toString();
        String annoAtto=null;
        String numeroAtto = null;
        if( determina.getNumero() != null ){
            annoAtto = determina.getAnno().toString();
            numeroAtto = determina.getNumero().toString();
        }
        String data;
        if (determina.getPratica().getDatapratica()!=null) {
            data=dateFormat.format(determina.getPratica().getDatapratica());
        } else {
            data="";
        }
        for( ServizioDetermina servizioDetermina: determina.getServizioDeterminaCollection() ){
            rProc = String.format("%04d", servizioDetermina.getServizio().getId());
            break;
        }
        String validoImpegni;
        if (determina.getDispesa()) {
            validoImpegni = "S";
        } else {
            validoImpegni = "N";
        }
        String validoAccertamenti;
        if (determina.getDientrata()) {
            validoAccertamenti = "S";
        } else {
            validoAccertamenti = "N";
        }

        FormMovimenti form = new FormMovimenti(annoBozza, organoSettore, numeroBozza, annoAtto, organoSettore,
                numeroAtto, utente, rProc, vistoResponsabile, determina.getOggetto(), data, dataVistoResponsabile,
                "S", "S", "S", "S", "S");
        form.show();
    }

    public Boolean assegnaEsecutivitaAtto(Determina determina, Date data) {
        String utente = ((Utente) Register.queryUtility(IUtente.class)).getLogin();
        JEnteHelper jEnteHelper = new JEnteHelper(utente);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        return jEnteHelper.chiamataModificaEsecutivitaAtto("A", "DT", determina.getAnno().toString(),
                determina.getNumero().toString(), sdf.format(data), "S");
    }

    public Boolean trasformaBozzaInAtto(Determina determina, Date data) {

        String utente = ((Utente) Register.queryUtility(IUtente.class)).getLogin();
        JEnteHelper jEnteHelper = new JEnteHelper(utente);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String annoBozza = determina.getPratica().getIdpratica().substring(0, 4);
        String numeroBozza = ((Integer) Integer.parseInt(determina.getPratica().getIdpratica().substring(4))).toString();

        if( determina.getNumero() != null && determina.getNumero() != 0 ) {
            if ( ! jEnteHelper.chiamataRichiestaEsisteBozzaOAtto("A", "DT", determina.getAnno().toString(), determina.getNumero().toString()) ){
                return jEnteHelper.chiamataRichiestaTrasformazioneBozzaInAtto("B", "DT", annoBozza, numeroBozza, "DT", determina.getAnno().toString(),
                        determina.getNumero().toString(), sdf.format(data));
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

}

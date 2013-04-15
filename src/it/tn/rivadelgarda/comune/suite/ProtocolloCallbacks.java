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
import com.axiastudio.pypapi.annotations.Callback;
import com.axiastudio.pypapi.annotations.CallbackType;
import com.axiastudio.pypapi.db.Database;
import com.axiastudio.pypapi.db.IDatabase;
import com.axiastudio.pypapi.db.Validation;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Ufficio;
import com.axiastudio.suite.base.entities.UfficioUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.pratiche.entities.Pratica;
import com.axiastudio.suite.protocollo.entities.Attribuzione;
import com.axiastudio.suite.protocollo.entities.PraticaProtocollo;
import com.axiastudio.suite.protocollo.entities.Protocollo;
import com.axiastudio.suite.protocollo.entities.Protocollo_;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Tiziano Lattisi <tiziano at axiastudio.it>
 */
public class ProtocolloCallbacks {
    
    /*
     * Valida il protocollo e richiede il nuovo iddocumento
     */
    @Callback(type=CallbackType.BEFORECOMMIT)
    public static Validation beforeCommit(Protocollo protocollo){
        Boolean res = true;
        Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
        Boolean eNuovo = protocollo.getId() == null;
        List<Ufficio> uffici = new ArrayList();
        List<Ufficio> ufficiRicerca = new ArrayList();
        List<Ufficio> ufficiPrivato = new ArrayList();
        for(UfficioUtente uu: autenticato.getUfficioUtenteCollection()){
            uffici.add(uu.getUfficio());
            if( uu.getRicerca() ){
                ufficiRicerca.add(uu.getUfficio());
            }
            if( uu.getPrivato() ){
                ufficiPrivato.add(uu.getUfficio());
            }
        }
        Ufficio attribuzionePrincipale = null;
        for( Attribuzione attribuzione: protocollo.getAttribuzioneCollection() ){
            if( attribuzione.getPrincipale() ){
                attribuzionePrincipale = attribuzione.getUfficio();
                break;
            }
        }
        Ufficio sportello = protocollo.getSportello();

        String msg = "";
        
        if( !eNuovo ){
            /*
             * Modifica permessa solo allo sportello e all'attribuzione principale
             * con flag ricerca
             */
            if( !(uffici.contains(sportello) || ufficiRicerca.contains(attribuzionePrincipale)) ){
                msg += "Devi appartenere allo sportello o all'attribuzione principale\n";
                msg += "con diritti di ricerca, per poter modificare il protocollo.\n";
                res = false;
            }
            
        } else {
            /*
             * Nuovo inserimento
             */
        

            /* sportello obbligatorio */
            if( protocollo.getSportello() == null ){
                msg += "Deve essere dichiarato uno sportello ricevente.\n";
                res = false;
            }

            /* sportello tra quelli dell'utente */
            if( !uffici.contains(protocollo.getSportello()) ){
                msg += "Lo sportello deve essere scelto tra gli uffici dell'utente.\n";
                res = false;
            }

            /* almeno un soggetto */
            if( protocollo.getSoggettoProtocolloCollection().isEmpty() ){
                msg += "Deve essere dichiarato almeno un soggetto esterno (mittente o destinatario).\n";
                res = false;
            }

            if( eNuovo ){
                Calendar calendar = Calendar.getInstance();
                Integer year = calendar.get(Calendar.YEAR);
                Date date = calendar.getTime();
                Database db = (Database) Register.queryUtility(IDatabase.class);
                EntityManager em = db.getEntityManagerFactory().createEntityManager();
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Protocollo> cq = cb.createQuery(Protocollo.class);
                Root<Protocollo> root = cq.from(Protocollo.class);
                cq.select(root);
                cq.where(cb.equal(root.get(Protocollo_.anno), year));
                cq.orderBy(cb.desc(root.get("iddocumento")));
                TypedQuery<Protocollo> tq = em.createQuery(cq).setMaxResults(1);
                Protocollo max;
                protocollo.setDataprotocollo(date);
                protocollo.setAnno(year);
                try {
                    max = tq.getSingleResult();
                } catch (NoResultException ex) {
                    max=null;
                }
                String newIddocumento;
                if( max != null ){
                    Integer i = Integer.parseInt(max.getIddocumento().substring(4));
                    i++;
                    newIddocumento = year+String.format("%08d", i);
                } else {
                    newIddocumento = year+"00000001";
                }
                protocollo.setIddocumento(newIddocumento);
            }
        }
        
        /*
         * Verifica inserimento pratiche: permesso solo se ufficio gestore, eventualmente
         * con flag riservato.
         */
        for( PraticaProtocollo praticaProtocollo: protocollo.getPraticaProtocolloCollection() ){
            if( praticaProtocollo.getProtocollo() == null ){
                /* Nuovo inserimento */
                Pratica pratica = praticaProtocollo.getPratica();
                Ufficio ufficioGestore = pratica.getGestione();
                if( true ){
                    /* TODO: riservato */
                    if( !ufficiPrivato.contains(ufficioGestore) && !autenticato.getSupervisorepratiche() ){
                        msg += "Per poter inserire pratiche riservate è necessario appartenere al loro ufficio gestore\n";
                        msg += "con flag riservato, o essere un amministratore delle pratiche.\n";
                        res = false;
                    }
                } else {
                    if( !uffici.contains(ufficioGestore) && !autenticato.getSupervisorepratiche() ){
                        msg += "Per poter inserire pratiche è necessario appartenere al loro ufficio gestore,\n";
                        msg += "o essere un amministratore delle pratiche.\n";
                        res = false;
                    }
                }
            }
        }
        
        /*
         * Restituzione della validazione
         */
        if( res == false ){
            return new Validation(false, msg);
        } else {
            return new Validation(true);
        }
    }
        
    /*
     * CallbackType.AFTERCOMMIT
     */
    @Callback(type=CallbackType.AFTERCOMMIT)
    public static Validation afterCommit(Protocollo protocollo){
        // placeholder
        return new Validation(true);
    }

}
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

import com.axiastudio.mapformat.MessageMapFormat;
import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.annotations.Callback;
import com.axiastudio.pypapi.annotations.CallbackType;
import com.axiastudio.pypapi.db.Database;
import com.axiastudio.pypapi.db.IDatabase;
import com.axiastudio.pypapi.db.Validation;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.UfficioUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.pratiche.entities.Pratica;
import com.axiastudio.suite.pratiche.entities.Pratica_;
import com.axiastudio.suite.protocollo.ProfiloUtenteProtocollo;
import com.axiastudio.suite.protocollo.entities.PraticaProtocollo;
import com.axiastudio.suite.protocollo.entities.Protocollo;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Tiziano Lattisi <tiziano at axiastudio.it>
 */
public class PraticaCallbacksRiva {
    
    @Callback(type=CallbackType.BEFORECOMMIT)
    public static Validation validaPratica(Pratica pratica){
        String msg = "";
        Boolean res = true;
        Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
        Boolean inUfficioGestore = false;
        for( UfficioUtente uu: autenticato.getUfficioUtenteCollection() ){
            if( uu.getUfficio().equals(pratica.getGestione()) && uu.getModificapratica() ){
                // se la pratica è riservata, mi serve anche il flag
                if( !pratica.getRiservata() || uu.getRiservato() ){
                    inUfficioGestore = true;
                    break;
                }
            }
        }
        
        // se l'utente non è istruttore non può inserire o modificare pratiche,
        if( !autenticato.getIstruttorepratiche() ){
            msg = "Devi avere come ruolo \"istruttore\" per poter inserire\n";
            msg += "o modificare una pratica.";
            return new Validation(false, msg);
        }
                        
        // devono essese definite attribuzione e tipologia
        if( pratica.getAttribuzione() == null ){
            msg = "Devi selezionare un'attribuzione.";
            return new Validation(false, msg);
        } else if( pratica.getTipo() == null ){
            msg = "Devi selezionare un tipo di pratica.";
            return new Validation(false, msg);
        }

        
        if( pratica.getId() == null ){
            Calendar calendar = Calendar.getInstance();
            Integer year = calendar.get(Calendar.YEAR);
            Date date = calendar.getTime();
            Database db = (Database) Register.queryUtility(IDatabase.class);
            EntityManager em = db.getEntityManagerFactory().createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Pratica> cq = cb.createQuery(Pratica.class);
            Root<Pratica> root = cq.from(Pratica.class);
            cq.select(root);
            cq.where(cb.equal(root.get(Pratica_.anno), year));
            cq.orderBy(cb.desc(root.get(Pratica_.idpratica)));
            TypedQuery<Pratica> tq = em.createQuery(cq).setMaxResults(1);
            Pratica max;
            pratica.setDatapratica(date);
            pratica.setAnno(year);
            try {
                max = tq.getSingleResult();
            } catch (NoResultException ex) {
                max=null;
            }
            String newIdpratica;
            if( max != null ){
                Integer i = Integer.parseInt(max.getIdpratica().substring(4));
                i++;
                newIdpratica = year+String.format("%05d", i);
            } else {
                newIdpratica = year+"00001";
            }
            pratica.setIdpratica(newIdpratica);
            
            // codifica
            String formulacodifica = pratica.getTipo().getFormulacodifica();
            Map<String, Object> map = new HashMap();
            map.put("anno", year.toString());
            map.put("giunta", "2010");
            String s1="";
            String s2="";
            String s3="";
            Integer n1=0;
            Integer n2=0;
            Integer n3=0;
            if( pratica.getTipo().getCodice().length()>=3 ){
                s1 = pratica.getTipo().getCodice().substring(0, 3);
                
            }
            if( pratica.getTipo().getCodice().length()>=5 ){
                s2 = pratica.getTipo().getCodice().substring(3, 5);
            }
            if( pratica.getTipo().getCodice().length()==7 ){
                s3 = pratica.getTipo().getCodice().substring(5, 7);
            }
            map.put("s1", s1);
            map.put("s2", s2);
            map.put("s3", s3);
            
            // max n1
            Query q1 = em.createQuery("select max(p.codiceinterno) from Pratica p where p.anno = " + year.toString() + " and p.codiceinterno like '"+s1+"%'");
            String maxN1 = (String) q1.getSingleResult();
            Integer da = pratica.getTipo().getPorzionenumeroda();
            Integer a = pratica.getTipo().getPorzionenumeroa();
            if( maxN1.length()>=a && da!=null && a!=null ){
                String porzionenumero = maxN1.substring(da, a);
                n1 = Integer.parseInt(porzionenumero)+1;
            }
            // max n2
            Query q2 = em.createQuery("select max(p.codiceinterno) from Pratica p where p.anno = " + year.toString() + " and p.codiceinterno like '"+s1+"%'");
            String maxN2 = (String) q2.getSingleResult();
            if( maxN2.length()>=a && da!=null && a!=null ){
                String porzionenumero = maxN2.substring(da, a);
                n2 = Integer.parseInt(porzionenumero)+1;
            }
            // max n3
            Query q3 = em.createQuery("select max(p.codiceinterno) from Pratica p where p.anno = " + year.toString() + " and p.codiceinterno like '"+s1+"%'");
            String maxN3 = (String) q3.getSingleResult();
            if( maxN3.length()>=a && da!=null && a!=null ){
                String porzionenumero = maxN3.substring(da, a);
                n3 = Integer.parseInt(porzionenumero)+1;
            }
            
            map.put("n1", n1);
            map.put("n2", n2);
            map.put("n3", n3);
            MessageMapFormat mmp = new MessageMapFormat(formulacodifica);
            String codifica = mmp.format(map);
            pratica.setCodiceinterno(codifica);
            
            // se mancano gestione e ubicazione, li fisso come l'attribuzione
            if( pratica.getGestione() == null ){
                pratica.setGestione(pratica.getAttribuzione());
            }
            if( pratica.getUbicazione() == null ){
                pratica.setUbicazione(pratica.getAttribuzione());
            }
        } else {
            // l'amministratore pratiche modifica anche se non appartenente all'ufficio gestore e
            // anche se la pratica è archiviata.
            if( !autenticato.getSupervisorepratiche() ){
                // se l'utente non è inserito nell'ufficio gestore con flag modificapratiche non può modificare
                if( !inUfficioGestore ){
                    msg = "Per modificare la pratica devi appartenere all'ufficio gestore con i permessi di modifica, ed eventuali privilegi sulle pratiche riservate.";
                    return new Validation(false, msg);
                }
                // impossibile togliere gli uffici
                if( pratica.getGestione() == null || pratica.getAttribuzione() == null || pratica.getUbicazione() == null){
                    msg = "Non è permesso rimuovere attribuzione, gestione o ubicazione.";
                    return new Validation(false, msg);
                }
                // Se la pratica è archiviata, non posso modificarla, ma ciò viene implementato con il cambio di ufficio gestore
            }
        }
        
        /*
         * Verifica inserimento protocollo in pratica: permesso solo all'ufficio gestore (già sopra),
         * e solo se l'utente ha piena visibilità del protocollo (sportello o attribuzione)
         */
        for( PraticaProtocollo praticaProtocollo: pratica.getPraticaProtocolloCollection() ){
            // nuovo inserimento
            if( praticaProtocollo.getPratica() == null ){
                // il supervisore inserisce pratiche non riservate
                if( !(!pratica.getRiservata() && autenticato.getSupervisorepratiche()) ){
                    Protocollo protocollo = praticaProtocollo.getProtocollo();
                    ProfiloUtenteProtocollo profilo = new ProfiloUtenteProtocollo(protocollo, autenticato);
                    if( !pratica.getRiservata() && !profilo.inSportelloOAttribuzione() ){
                        msg = "Devi avere completa visibilità del protocollo per poterlo inserire nella pratica.";
                        return new Validation(false, msg);
                    } else if( pratica.getRiservata() && !profilo.inSportelloOAttribuzioneR() ){
                        msg = "Devi avere completa visibilità del protocollo e permesso sui dati riservati per poterlo inserire nella pratica riservata.";
                        return new Validation(false, msg);
                    }
                }
            }
        }
        
        return new Validation(true);
    }
}

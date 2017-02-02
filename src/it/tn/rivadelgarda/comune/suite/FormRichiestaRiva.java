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


import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.db.Database;
import com.axiastudio.pypapi.db.IDatabase;
import com.axiastudio.pypapi.ui.widgets.PyPaPiTableView;
import com.axiastudio.suite.base.entities.IUtente;
import com.axiastudio.suite.base.entities.Utente;
import com.axiastudio.suite.base.entities.Utente_;
import com.axiastudio.suite.richieste.entities.DestinatarioUtente;
import com.axiastudio.suite.richieste.entities.Richiesta;
import com.axiastudio.suite.richieste.forms.FormRichiesta;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QMessageBox;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 */
public class FormRichiestaRiva extends FormRichiesta {

    public FormRichiestaRiva(String uiFile, Class entityClass, String title){
        super(uiFile, entityClass, title);
        ((QComboBox) findChild(QComboBox.class, "comboBoxGruppiDestinatari")).currentStringChanged.connect(this, "insertGruppiDestinatari()");
    }

    @Override
    protected void indexChanged(int row) {
        super.indexChanged(row);
        Richiesta richiesta=(Richiesta) this.getContext().getCurrentEntity();
        Boolean nuovaRichiesta = richiesta.getId() == null;
        ((QComboBox) findChild(QComboBox.class, "comboBoxGruppiDestinatari")).setEnabled(nuovaRichiesta);
    }

    private void insertGruppiDestinatari() {
        String gruppo=((QComboBox) findChild(QComboBox.class, "comboBoxGruppiDestinatari")).currentText();
        QMessageBox.information(this, " ",gruppo);
        if ( !gruppo.equals(" ") ) {
            Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
            Database db = (Database) Register.queryUtility(IDatabase.class);
            EntityManager em = db.getEntityManagerFactory().createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Dipendenti> cq = cb.createQuery(Dipendenti.class);
            Root<Dipendenti> root = cq.from(Dipendenti.class);
            Join<Dipendenti, Utente> itemUtente = root.join(Dipendenti_.cod_protocollo);
            cq = cq.select(root);
            List<Predicate> predicates = new ArrayList();
            predicates.add(cb.notEqual(root.get(Dipendenti_.cod_protocollo), autenticato));

            if (gruppo.equals("Tutti gli utenti del Comune")) {
                predicates.add(cb.isTrue(itemUtente.get(Utente_.richieste)));
                predicates.add(cb.not(root.get(Dipendenti_.settore).in(Arrays.asList(6, 9, 30))));
            } else if (gruppo.equals("Tutti i dipendenti")) {
                predicates.add(cb.isTrue(itemUtente.get(Utente_.richieste)));
                predicates.add(cb.not(root.get(Dipendenti_.settore).in(Arrays.asList(6, 9, 30, 100))));
                predicates.add(cb.notEqual(root.get(Dipendenti_.tipo_contratto), 10));
            } else if (gruppo.equals("Tutti i responsabili strutture organizzative")) {
                predicates.add(cb.isTrue(root.get(Dipendenti_.incarico)));
                predicates.add(cb.isNull(root.get(Dipendenti_.unita_op)));
            }
            cq = cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            Order ord = cb.asc(itemUtente.get(Utente_.nome));
            cq = cq.orderBy(ord);
            TypedQuery<Dipendenti> query = em.createQuery(cq);
            List<Dipendenti> destinatari = query.getResultList();

            Richiesta richiesta = (Richiesta) this.getContext().getCurrentEntity();
            List<Utente> destUtenti = new ArrayList<Utente>();
            for ( DestinatarioUtente ut: richiesta.getDestinatarioUtenteCollection() ) {
                destUtenti.add(ut.getDestinatario());
            }
            List<Utente> destinatariRichiesta = new ArrayList<Utente>();
            for ( Dipendenti dest: destinatari ) {
                if ( !destUtenti.contains(dest.getCod_protocollo()) ) {
//                    DestinatarioUtente destRichiesta = new DestinatarioUtente();
//                    destRichiesta.setDestinatario(dest.getCod_protocollo());
//                    destRichiesta.setRichiestacancellabile(richiesta.getCancellabile());
//                    destinatariRichiesta.add(destRichiesta);
//                    richiesta.getDestinatarioUtenteCollection().add(destRichiesta);
                    destinatariRichiesta.add(dest.getCod_protocollo());
                }
            }
            ((PyPaPiTableView) this.findChild(PyPaPiTableView.class, "tableViewPersone")).actionAdd(destinatariRichiesta);
        }
    }
}

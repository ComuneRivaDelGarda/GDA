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
import com.axiastudio.pypapi.db.Validation;
import com.axiastudio.suite.deliberedetermine.DeterminaCallbacks;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 */
public class DeterminaCallbacksRiva {

    /*
     * Prima di salvare svuoto la lista dei movimenti
     */
    @Callback(type = CallbackType.BEFORECOMMIT)
    public static Validation beforeCommit(Determina determina) {
        Validation validation = DeterminaCallbacks.validaDetermina(determina);
        if (!validation.getResponse()) {
            return validation;
        }
        // svuoto la lista dei movimenti, visto che sono collegato a jEnte
        if (determina.getMovimentoDeterminaCollection() != null) {
            determina.getMovimentoDeterminaCollection().clear();
        }
        return new Validation(true);
    }

    @Callback(type = CallbackType.AFTERCOMMIT)
    public static Validation afterCommit(Determina determina) {
        Validation validation=new Validation(true);
        if (determina.hasChanged()) {
            IFinanziaria finanziariaUtil = (IFinanziaria) Register.queryUtility(IFinanziaria.class);
            Boolean ret=finanziariaUtil.modificaOggettoBozza(determina, determina.getOggetto());
            validation.setResponse(ret);
            if ( ret ) {
                validation.setMessage("Oggetto dell'atto modificato in Infor. Verificare l'oggetto per eventuali impegni gia' inseriti.");
            }
            determina.reset();
        }
        return validation;
    }
}

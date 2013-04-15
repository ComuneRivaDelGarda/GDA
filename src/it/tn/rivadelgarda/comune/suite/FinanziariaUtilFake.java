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

import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.entities.MovimentoDetermina;
import com.axiastudio.suite.finanziaria.entities.Capitolo;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 */
public class FinanziariaUtilFake implements IFinanziaria {
    private String utente;

    public FinanziariaUtilFake(String utente) {
        this.utente = utente;
    }

    @Override
    public List<MovimentoDetermina> getMovimentiDetermina(Determina determina) {
        List<MovimentoDetermina> impegni = new ArrayList();
        for( Integer i=1; i<5; i++ ){
            MovimentoDetermina movimento = new MovimentoDetermina();
            BigDecimal importo = new BigDecimal(i+"0.0");
            //movimento.setCodiceCapitolo(1888L);
            Capitolo capitolo1888 = new Capitolo();
            capitolo1888.setDescrizione("capitolo "+i);
            movimento.setCapitolo(capitolo1888);
            movimento.setImporto(importo);
            movimento.setImpegno("bla bla");
            movimento.setAnnoEsercizio(2013L);
            movimento.setArticolo("stringa articolo "+i);
            impegni.add(movimento);
        }
        return impegni;
    }

    @Override
    public void apriGestoreMovimenti(Determina determina) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

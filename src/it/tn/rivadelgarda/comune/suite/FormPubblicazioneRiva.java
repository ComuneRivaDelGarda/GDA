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

import com.axiastudio.pypapi.ui.Util;
import com.axiastudio.suite.plugins.atm.PubblicazioneATM;
import com.axiastudio.suite.plugins.atm.helper.PutAttoHelper;
import com.axiastudio.suite.plugins.atm.ws.ATMClient;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;
import com.trolltech.qt.gui.QSpinBox;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

/**
 *
 * @author AXIA Studio (http://www.axiastudio.com)
 */
public class FormPubblicazioneRiva extends FormPubblicazione {

    public FormPubblicazioneRiva(String uiFile, Class entityClass, String title){
        super(uiFile, entityClass, title);
    }

    public void pubblicaOra(){


        QSpinBox n_giorni = (QSpinBox) findChild(QSpinBox.class, "spinBox_n_giorni");
        int n = n_giorni.value();
        if( n == 0 ){
            Util.warningBox(this, "Dati di pubblicazione errati", "Deve essere indicato un numero di giorni di consultazione");
            return;
        }

        Pubblicazione pubblicazione = (Pubblicazione) this.getContext().getCurrentEntity();

        PubblicazioneATM pubblicazioneATM = new PubblicazioneATM();

        String titolo = pubblicazione.getTitolo().replaceAll("'", "`");
        pubblicazioneATM.setTitolo(titolo);
        String descrizione = pubblicazione.getDescrizione().replaceAll("'", "`");
        pubblicazioneATM.setDescrizione(descrizione);

        if( pubblicazione.getDataatto() != null ) {
            pubblicazioneATM.setDataatto(pubblicazione.getDataatto());
        } else {
            Calendar calendar = Calendar.getInstance(Locale.ITALIAN);
            pubblicazioneATM.setDataatto(calendar.getTime());
        }

        if( pubblicazione.getNumeroatto() != null  ) {
            pubblicazioneATM.setNumeroatto(pubblicazione.getNumeroatto());
        }
        pubblicazioneATM.setDurataconsultazione(pubblicazione.getDurataconsultazione());
        pubblicazioneATM.setRichiedente(pubblicazione.getRichiedente());
        pubblicazioneATM.setTipoatto(pubblicazione.getTipoattopubblicazione().getDescrizione());

        PutAttoHelper helper = new PutAttoHelper();

        Properties ctx = loadConfig();

        helper.setup(ctx.getProperty(ATMClient.USER_ID),
                ctx.getProperty(ATMClient.PASSWORD),
                ctx.getProperty(ATMClient.MAC_NAME),
                ctx.getProperty(ATMClient.WSAKEY),
                ctx.getProperty(ATMClient.ENDPOINT));

        boolean res = helper.putAtto(pubblicazioneATM);
        if( !res ){
            Util.warningBox(this, "Arrore in pubblicazione", "L'atto non è stato pubblicato all'albo.");
        }
    }

    private Properties loadConfig() {
        Properties p = new Properties();

        try {
            p.load(getClass().getResourceAsStream("putatto.properties"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return p;

    }
}

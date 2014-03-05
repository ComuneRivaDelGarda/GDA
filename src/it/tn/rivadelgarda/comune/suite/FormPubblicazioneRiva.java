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

import com.axiastudio.pypapi.ui.Window;
import com.axiastudio.suite.plugins.atm.PubblicazioneATM;
import com.axiastudio.suite.plugins.atm.helper.PutAttoHelper;
import com.axiastudio.suite.plugins.atm.ws.ATMClient;
import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;
import com.trolltech.qt.gui.QPushButton;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
        System.out.println("buh!");

        Pubblicazione pubblicazione = (Pubblicazione) this.getContext().getCurrentEntity();

        Calendar calendar = Calendar.getInstance(Locale.ITALIAN);
        Date oggi = calendar.getTime();

        PubblicazioneATM pubblicazioneATM = new PubblicazioneATM();

        pubblicazioneATM.setTitolo(pubblicazione.getTitolo());
        pubblicazioneATM.setDescrizione(pubblicazione.getDescrizione());
        pubblicazioneATM.setInizioconsultazione(pubblicazione.getInizioconsultazione());
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

        helper.putAtto(pubblicazioneATM);
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
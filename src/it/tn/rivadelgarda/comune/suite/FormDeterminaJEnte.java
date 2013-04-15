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

import com.axiastudio.menjazo.AlfrescoHelper;
import com.axiastudio.pypapi.IStreamProvider;
import com.axiastudio.pypapi.Register;
import com.axiastudio.pypapi.plugins.cmis.CmisPlugin;
import com.axiastudio.pypapi.plugins.cmis.CmisStreamProvider;
import com.axiastudio.pypapi.plugins.ooops.IDocumentFolder;
import com.axiastudio.pypapi.plugins.ooops.Template;
import com.axiastudio.pypapi.ui.Context;
import com.axiastudio.pypapi.ui.Window;
import com.axiastudio.pypapi.ui.widgets.PyPaPiToolBar;
import com.axiastudio.suite.deliberedetermine.entities.Determina;
import com.axiastudio.suite.deliberedetermine.entities.MovimentoDetermina;
import com.axiastudio.suite.deliberedetermine.entities.ServizioDetermina;
import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
import com.axiastudio.suite.finanziaria.entities.IFinanziaria;
import com.axiastudio.suite.procedimenti.IGestoreDeleghe;
import com.axiastudio.suite.procedimenti.entities.CodiceCarica;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class GestoreMovimentiMenuBar extends PyPaPiToolBar {
    public GestoreMovimentiMenuBar(String title, Window parent){
        super(title, parent);
        this.insertButton("apriGestoreMovimenti", "Gestore movimenti",
                "classpath:com/axiastudio/pypapi/plugins/jente/resources/jente.png",
                "Apri la maschera di gestione dei movimenti", parent);
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
public class FormDeterminaJEnte extends FormDetermina implements IDocumentFolder {
    
    public FormDeterminaJEnte(String uiFile, Class entityClass, String title){
        super(uiFile, entityClass, title);
        GestoreMovimentiMenuBar gestoreMovimenti = new GestoreMovimentiMenuBar("Gestore movimenti", this);
        this.addToolBar(gestoreMovimenti);
    }
    
    @Override
    protected Boolean vistoResponsabile() {
        if( super.vistoResponsabile() ){
            this.apriGestoreMovimenti();
            /* TODO:
             * - trasformazione bozza in atto (chiamata jEnte)
             * - generazione protocollo (modello entità pyprotocollo)
             * - convalida protocollo (modello entità pyprotocollo)
             * - generazione numero di determinazione
             * - valorizzazione vistoresponsabile, datavistoresponsabile, utentevistoresponsabile (implementato in super)
             * - valorizzazione riferimento protocollo
             * - elaborazione determina_*.odt con i dati nuovi -> determina_*_protocollo_*.odt
             * - richiesta conferma dell'utente
             */
            
            return true;
        }
        return false;
    }

    /*
     * Verifica delle condizioni di abilitazione alla firma del responsabile
     * del servizio.
     */
    @Override
    protected Boolean checkResponsabile() {
        Determina determina = (Determina) this.getContext().getCurrentEntity();
        //Pratica pratica = SuiteUtil.findPratica(determina.getIdPratica());
        IGestoreDeleghe gestore = (IGestoreDeleghe) Register.queryUtility(IGestoreDeleghe.class);
        IFinanziaria finanziariaUtil = (IFinanziaria) Register.queryUtility(IFinanziaria.class);
        
        /* non è di spesa o la lista degli impegni è vuota */
        List<MovimentoDetermina> impegni = finanziariaUtil.getMovimentiDetermina(determina);
        if( !determina.getDiSpesa() || impegni.isEmpty() ){
            return false;
        }

        CmisPlugin cmisPlugin = (CmisPlugin) Register.queryPlugin(FormDeterminaJEnte.class, "CMIS");
        AlfrescoHelper helper = cmisPlugin.createAlfrescoHelper(determina);
        List<HashMap> children = helper.children();

        /* non è presente un unico file Determina_*.odt */
        Boolean fileDeterminaUnico = false;
        for( HashMap map: children ){
            String fileName = (String) map.get("name");
            if( fileName.startsWith("Determina_") && fileName.endsWith(".odt") ){
                if( fileDeterminaUnico == false ){
                    fileDeterminaUnico = true;
                } else {
                    fileDeterminaUnico = false;
                    break;
                }
            }
        }
        if( !fileDeterminaUnico ){
            return false;
        }
        
        /* la determina è di spesa e non c'è un unico file Impegni_*.odt */
        if( determina.getDiSpesa() ){
            Boolean fileImpegniUnico = false;
            for( HashMap map: children ){
                String fileName = (String) map.get("name");
                if( fileName.startsWith("Impegni_") && fileName.endsWith(".odt") ){
                    if( fileImpegniUnico == false ){
                        fileImpegniUnico = true;
                    } else {
                        fileImpegniUnico = false;
                        break;
                    }
                }
            }
            if( !fileImpegniUnico ){
                return false;
            }
        }
        
        /* sono presenti file con estensione diversa da odt o pdf */
        for( HashMap map: children ){
            String fileName = (String) map.get("name");
           if( !(fileName.endsWith(".pdf") || fileName.endsWith(".odt")) ){
               return false;
           }
        }
        /* l'anno della determina non è l'anno corrente */
        String annoCorrente = (new SimpleDateFormat("yyyy")).format(new Date());
        String annoPratica = (new SimpleDateFormat("yyyy")).format(determina.getDataPratica());
        if( !annoCorrente.equals(annoPratica) ){
            //return false; // XXX: come testare?
        }
        
        /* non è specificato un referente politico */
        if( determina.getReferentePolitico() == null || determina.getReferentePolitico().equals("")){
            return false;
        }
        
        /* nessun servizio è selezionato */
        if( determina.getServizioDeterminaCollection().isEmpty() ){
            return false;
        }
        
        /* l'utente non è il responsabile del servizio principale e neppure il segretario generale */
        ServizioDetermina servizioDetermina = (ServizioDetermina) determina.getServizioDeterminaCollection().toArray()[0];
        if( !(gestore.checkTitoloODelega(CodiceCarica.RESPONSABILE_DI_SERVIZIO, servizioDetermina.getServizio()) || gestore.checkTitoloODelega(CodiceCarica.SEGRETARIO)) ){
            return false;
        }
        
        return true;

    }

    @Override
    protected void indexChanged(int row) {
        Determina determina = (Determina) this.getContext().getCurrentEntity();
        IFinanziaria finanziariaUtil = (IFinanziaria) Register.queryUtility(IFinanziaria.class);
        List<MovimentoDetermina> movimenti = finanziariaUtil.getMovimentiDetermina(determina);
        determina.setMovimentoDeterminaCollection(movimenti);
        // faccio credere al contesto che il contesto padre è cambiato, quindi lo spingo ad aggiornarsi
        Context context = (Context) Register.queryRelation(this, ".movimentoDeterminaCollection");
        context.refreshContext();
        super.indexChanged(row);
    }
    
    private void apriGestoreMovimenti() {
        Determina determina = (Determina) this.getContext().getCurrentEntity();
        IFinanziaria finanziariaUtil = (IFinanziaria) Register.queryUtility(IFinanziaria.class);
        finanziariaUtil.apriGestoreMovimenti(determina);
    }

    /*
     * Per la determina i template aggiuntivi sono quelli contenuti nei documenti
     * odt presenti nella cartella della pratica.
     */
    @Override
    public List<Template> getTemplates() {
        List<Template> templates = new ArrayList();
        Determina determina = (Determina) this.getContext().getCurrentEntity();
        //Pratica pratica = SuiteUtil.findPratica(determina.getIdPratica());
        CmisPlugin cmisPlugin = (CmisPlugin) Register.queryPlugin(FormDeterminaJEnte.class, "CMIS");
        AlfrescoHelper helper = cmisPlugin.createAlfrescoHelper(determina);
        List<HashMap> children = helper.children();
        for( HashMap map: children ){
            IStreamProvider streamProvider = new CmisStreamProvider("http://192.168.64.41:8080/alfresco/service/cmis", "pypapi", "0i2kiwi", 
                                                                    (String) map.get("objectId"));
            //RuleSet rulesSet = new RuleSet(new HashMap()); // XXX: da pescare
            Template template = new Template(streamProvider, (String) map.get("name"), "Documento generato", null);
            templates.add(template);
        }
        return templates;
    }

    /*
     * I documenti creati nel contesto della determina vengono posizionati nella cartella della
     * pratica, pronti eventualmente ad essere riutilizzati come template.
     */
    @Override
    public void createDocument(String subpath, String name, byte[] content, String mimeType) {
        Determina determina = (Determina) this.getContext().getCurrentEntity();
        //Pratica pratica = SuiteUtil.findPratica(determina.getIdPratica());
        CmisPlugin cmisPlugin = (CmisPlugin) Register.queryPlugin(FormDeterminaJEnte.class, "CMIS");
        AlfrescoHelper helper = cmisPlugin.createAlfrescoHelper(determina);
        String documentName = name + "_" + determina.getIdPratica() + ".odt";
        //String documentName = "out.pdf";
        helper.createDocument(subpath, documentName, content, mimeType);
        cmisPlugin.showForm(determina);
        this.verificaAbilitazionePulsanti();
    }
    
}

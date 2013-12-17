import com.axiastudio.suite.pratiche.PraticaUtil

/*
 *  Determina del responsabile
 */

// Visto del responsabile: condizione
{ determina ->

    // se la determina è di spesa, allora ci vogliono impegni
    if( determina.dispesa && determina.movimentoDeterminaCollection.size()==0 ){
        return "Le determine di spese devono avere impegni associati."
    }

    // ci vuole un solo file del tipo Determina_*.odt
    ok = false
    for( String fileName: documenti ){
        if( fileName.startsWith("Determina_") && fileName.endsWith(".odt") ){
            if( !ok ){
                ok = true
            } else {
                ok = false
                break
            }
        }
    }
    if( !ok ){
        return "Deve essere presente uno ed un solo documento di tipo 'Determina'."
    }

    // se la determina è di spesa e ci vuole un solo file del tipo Impegni_*.odt
    ok = false
    if( determina.dispesa ){
        for( String fileName: documenti ){
            if( fileName.startsWith("Impegni_") && fileName.endsWith(".odt") ){
                if( !ok ){
                    ok = true
                } else {
                    ok = false
                    break
                }
            }
        }
        if( !ok ){
            return "Deve essere presente uno ed un solo documento di tipo 'Impegni'."
        }
    }

    // non devono essere presenti file con estensione diversa da odt o pdf
    for( String fileName: documenti ){
        if( !(fileName.endsWith(".odt") || fileName.endsWith(".pdf")) ){
            return "Non sono ammessi documenti con estensione diversa da odf o pdf."
        }
    }

    // l'anno della determina deve essere l'anno corrente
    def oggi = new Date()
    def annoCorrente = oggi[1]
    def annoDetermina = determina.getPratica().getDatapratica()[1]
    if( annoDetermina != annoCorrente ){
        return "L'anno della determina deve essere l'anno corrente."
    }

    // deve essere specificato un referente politico
    if( determina.getReferentePolitico() == null || determina.getReferentePolitico().equals("")){
        return "Specificare il referente politico."
    }

    // deve essere indicato almeno un servizio
    if( determina.getServizioDeterminaCollection().isEmpty() ){
        return "Deve essere indicato almeno un servizio."
    }

    // l'utente deve essere il responsabile del servizio principale oppure il segretario generale
    def servizioDetermina = determina.getServizioDeterminaCollection().toArray()[0]
    if( !(gestoreDeleghe.checkTitoloODelega(CodiceCarica.RESPONSABILE_DI_SERVIZIO, servizioDetermina.getServizio()) || gestoreDeleghe.checkTitoloODelega(CodiceCarica.SEGRETARIO)) ){
        return "L'utente deve essere responsabile del servizio principale o segretario generale."
    }


    return true
}

// Visto del responsabile: azione
{ determina ->

    // creazione del protocollo
    def sportello = determina.getServizioDeterminaCollection().toArray()[0].getServizio().getUfficio()
    def attribuzioni = [] // XXX
    def res = PraticaUtil.protocollaPratica(determina.getPratica(), sportello, determina.getOggetto(), attribuzioni)
    return res
}
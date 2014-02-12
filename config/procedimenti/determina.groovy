import com.axiastudio.suite.pratiche.PraticaUtil

/*
 *  Determina del responsabile
 */

// Istruttoria: condizione
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

    return true
}

// Istruita da approvare: condizione
{ determina ->

    // l'utente deve essere il responsabile del servizio principale oppure il segretario generale
    def servizioDetermina = determina.getServizioDeterminaCollection().toArray()[0]
    if( !(gestoreDeleghe.checkTitoloODelega(CodiceCarica.RESPONSABILE_DI_SERVIZIO, servizioDetermina.getServizio()) || gestoreDeleghe.checkTitoloODelega(CodiceCarica.SEGRETARIO)) ){
        return "L'utente deve essere responsabile del servizio principale o segretario generale."
    }
    // deve essere presente almeno un ufficio per l'attribuzione del protocollo

    return true
}

// Istruita da approvare: azione
{ determina ->

    // apertura maschera impegni (per trasformazione bozza in atto)

    // creazione del protocollo
    def sportello = determina.getServizioDeterminaCollection().toArray()[0].getServizio().getUfficio()
    def ud = determina.getUfficioDeterminaCollection().toArray()
    def n = ud.size()-1
    def attribuzioni = (0..n).collect { ud[it].ufficio }
    def uffici = [ sportello ]
    def validation = PraticaUtil.protocollaPratica(determina.getPratica(), sportello, determina.getOggetto(), attribuzioni, null, null, uffici)
    if( validation.response == false ){
        return validation.message
    }
    determina.setProtocollo(validation.entity)
    return true
}

// Predisposta per la sottoscrizione: condizione
{ determina ->

    // i documenti sono pronti per l'inserimento

}

// Predisposta per la sottoscrizione: azione
{ determina ->

    // path protocollo
    def path = "/Siti/protocollo/documentLibrary/"
    def protocollo = determina.protocollo
    path += protocollo.dataprotocollo.format("yyyy/MM/dd/")
    path += protocollo.iddocumento

    // inserimento documenti in protocollo
    for( Map child: alfrescoHelper.children("protocollo") ){
        document = alfrescoHelper.copyDocument(child["objectId"], path)
    }
    return true

}

// Sottoscritta dal responsabile: condizione
{ determina ->

    // se la determina è di spesa e ci vuole un solo file del tipo Visto di bilancio_*.odt
    ok = false
    for( String fileName: documenti ){
        if( fileName.startsWith("Visto di bilancio_") && fileName.endsWith(".pdf") ){
            if( !ok ){
                ok = true
            } else {
                ok = false
                break
            }
        }
    }
    if( !ok ){
        return "Deve essere presente uno ed un solo documento di tipo 'Visto di bilancio' (pdf)."
    }
    return true

}

// Sottoscritta dal responsabile: azione
{ determina ->

    // path protocollo
    def path = "/Protocollo/"
    def protocollo = determina.protocollo
    path += protocollo.dataprotocollo.format("yyyy/MM/dd/")
    path += protocollo.iddocumento

    // inserimento visto in protocollo
    for( Map child: alfrescoHelper.children() ){
        if( child["name"].startsWith("Visto di bilancio_") && child["name"].endsWith(".pdf") ){
            document = alfrescoHelper.copyDocument(child["objectId"], path)
        }
    }
    return true
}

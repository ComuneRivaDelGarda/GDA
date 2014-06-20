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

    // devono essere specificati degli uffici
    if( determina.getUfficioDeterminaCollection().size() < 1 ){
        return "Devono essere specificate delle attribuzioni per il protocollo."
    }

    return true
}

// Istruita da approvare: azione
        { determina ->

            nImpegniInfor = determina.movimentoDeterminaCollection.size()

            // acquisizione numero determina (se non già esistente)
            if (determina.getNumero() == null || determina.getNumero() == 0){
                if ( ! DeterminaUtil.numeroDiDetermina(determina) ) {
                    return "Errore nell'acquisizione del numero di derermina"
                }
            }

            // creazione del protocollo (se non già esistente)
            if( determina.getProtocollo() == null ){
                def validation = PraticaUtil.protocollaDetermina(determina)
                if( validation.response == false ){
                    return validation.message
                }
            }

            // trasformazione bozza in atto se spesa o entrata
            if ((determina.dispesa || determina.diEntrata) && nImpegniInfor>0 ) {
                if ( ! finanziariaUtil.trasformaBozzaInAtto(determina, determina.getData()) ) {
                    return "Errore nella trasformazione da bozza in atto"
                }
            }

            // copia documenti pdf nella cartella x la protocollazione
            def path = "/Siti/pratiche/documentLibrary/"
            path += determina.datapratica.format("yyyy/MM/") + determina.pratica.idpratica + "/protocollo"

            // inserimento documenti in protocollo
            for( Map child: alfrescoHelper.children() ){
                if( child["name"].endsWith(".pdf") ) {
                    document = alfrescoHelper.copyDocument(child["objectId"], path)
                }
            }

            return true
        }

// Predisposta per la sottoscrizione: condizione
{ determina ->

    // i documenti sono pronti per l'inserimento
    def path = "/Siti/protocollo/documentLibrary/"
    def protocollo = determina.protocollo
    path += protocollo.dataprotocollo.format("yyyy/MM/dd/")
    path += protocollo.iddocumento

    if( alfrescoHelper.children("protocollo").size() < 1 ){
        return "Devono essere preparati i documenti per l'associazione al protocollo"
    }
    return true

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

            // consolida protocollo
            protocollo.setConsolidadocumenti(true)

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
            def path = "/Siti/protocollo/documentLibrary/"
            def protocollo = determina.protocollo
            path += protocollo.dataprotocollo.format("yyyy/MM/dd/")
            path += protocollo.iddocumento

            // inserimento visto in protocollo
            for( Map child: alfrescoHelper.children() ){
                if( child["name"].startsWith("Visto di bilancio_") && child["name"].endsWith(".pdf") ){
                    document = alfrescoHelper.copyDocument(child["objectId"], path)
                }
            }

            // inserimento attribuzioni dalla determina
            def validation = PraticaUtil.inserisciAttribuzioniProtocolloDetermina(determina)
            if( validation.response == false ){
                return validation.message
            }

            // inserimento data di esecutività dell'atto
            if ( determina.movimentoDeterminaCollection.size()>0 && ! finanziariaUtil.assegnaEsecutivitaAtto(determina, Calendar.getInstance().getTime()) ) {
                return "Errore nell'inserimento della data di esecutività"
            }

            return true
        }
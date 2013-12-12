/*
 *  Determina del responsabile
 */

// Visto del responsabile
{ determina ->

    // se la determina è di spesa, allora ci vogliono impegni
    if( determina.dispesa && determina.movimentoDeterminaCollection.size()==0 ){
        return false
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
        return false
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
            return false
        }
    }

    // non devono essere presenti file con estensione diversa da odt o pdf
    for( String fileName: documenti ){
        if( !(fileName.endsWith(".odt") || fileName.endsWith(".pdf")) ){
            return false
        }
    }

    // l'anno della determina deve essere l'anno corrente
    def oggi = new Date()
    def annoCorrente = oggi[1]
    def annoDetermina = determina.getDatapratica()[1]
    if( annoDetermina != annoCorrente ){
        return false
    }

    return true
}
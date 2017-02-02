/*
 * Copyright (C) 2013 AXIA Studio (http://www.axiastudio.com)
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
 * You should have received a copy of the GNU Afffero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.tn.rivadelgarda.comune.suite;

import com.axiastudio.suite.base.entities.Utente;

import javax.persistence.*;

/**
 *
 * @author Comune di Riva del Garda
 */
@Entity
@Table(schema="GENERAL")
//@SequenceGenerator(name="gengruppo", sequenceName="anagrafiche.gruppo_id_seq", initialValue=1, allocationSize=1)
public class Dipendenti {
//    private static final long serialVersionUID = 1L;
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="gengruppo")
//    private Long id;
    @Id
    @Column(name="id_anag")
    private Integer id_anag;
    @JoinColumn(name = "cod_protocollo", referencedColumnName = "id")
    private Utente cod_protocollo;
    @Column(name="settore")
    private Integer settore;
    @Column(name="area_funzionale")
    private Integer area_funzionale;
    @Column(name="unita_op")
    private Integer unita_op;
    @Column(name="tipo_contratto")
    private Integer tipo_contratto;
    @Column(name="incarico")
    private Boolean incarico;

    public Integer getId_anag() {
        return id_anag;
    }

    public void setId_anag(Integer id_anag) {
        this.id_anag = id_anag;
    }

    public Utente getCod_protocollo() {
        return cod_protocollo;
    }

    public void setCod_protocollo(Utente cod_protocollo) {
        this.cod_protocollo = cod_protocollo;
    }

    public Integer getSettore() {
        return settore;
    }

    public void setSettore(Integer settore) {
        this.settore = settore;
    }

    public Integer getArea_funzionale() {
        return area_funzionale;
    }

    public void setArea_funzionale(Integer area_funzionale) {
        this.area_funzionale = area_funzionale;
    }

    public Integer getUnita_op() {
        return unita_op;
    }

    public void setUnita_op(Integer unita_op) {
        this.unita_op = unita_op;
    }

    public Integer getTipo_contratto() {
        return tipo_contratto;
    }

    public void setTipo_contratto(Integer tipo_contratto) {
        this.tipo_contratto = tipo_contratto;
    }

    public Boolean getIncarico() {
        return incarico;
    }

    public void setIncarico(Boolean incarico) {
        this.incarico = incarico;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id_anag != null ? id_anag.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dipendenti)) {
            return false;
        }
        Dipendenti other = (Dipendenti) object;
        return !((this.id_anag == null && other.id_anag != null) || (this.id_anag != null && !this.id_anag.equals(other.id_anag)));
    }

    @Override
    public String toString() {
        return this.id_anag.toString();
    }
    
}

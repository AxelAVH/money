package de.amo.money;

import de.amo.view.fachwerte.Fachwert;
import de.amo.view.table.ATableModel;

import java.util.List;

/**
 * Created by private on 18.01.2016.
 */
public class BuchungszeilenTableModel extends ATableModel {


    public BuchungszeilenTableModel(List<Fachwert> fachwerte) {
        super(fachwerte);
    }

    @Override
    public Object getValueAt(Object record, String attributName) {

        Buchungszeile buchungszeile = (Buchungszeile) record;

        if (Fachwerte.BETRAG.equals(attributName))              return buchungszeile.getBetragAsDouble();
        if (Fachwerte.BUCHUNGSTEXT.equals(attributName))        return buchungszeile.buchungstext;
        if (Fachwerte.DATUM.equals(attributName))               return buchungszeile.datum;
        if (Fachwerte.HAUPTBUCHUNGSNR.equals(attributName))     return buchungszeile.hauptbuchungsNr;
        if (Fachwerte.KATEGORIE.equals(attributName))           return buchungszeile.kategorie;
        if (Fachwerte.KOMMENTAR.equals(attributName))           return buchungszeile.kommentar;
        if (Fachwerte.PBETRAG.equals(attributName))             return buchungszeile.getPBetragAsDouble();
        if (Fachwerte.PSALDO.equals(attributName))              return buchungszeile.getPSaldoAsDouble();
        if (Fachwerte.SALDOGEGLAETTET.equals(attributName))     return buchungszeile.getSaldoGeglaettetAsDouble();
        if (Fachwerte.QUELLEZIEL.equals(attributName))          return buchungszeile.quelleZiel;
        if (Fachwerte.SALDO.equals(attributName))               return buchungszeile.getSaldoAsDouble();
        if (Fachwerte.UMBUCHUNGSNR.equals(attributName))        return buchungszeile.umbuchungNr;
        if (Fachwerte.VERWENDUNGSZWECK.equals(attributName))    return buchungszeile.verwendungszweck;
        if (Fachwerte.WAEHRUNG.equals(attributName))            return buchungszeile.waehrung;

        return "Nomapping for "+ attributName;
    }

    @Override
    public void setValueAt(Object value, Object record, String attributName) {

        Buchungszeile buchungszeile = (Buchungszeile) record;

        if (Fachwerte.BETRAG.equals(attributName)) buchungszeile.setBetrag((Double) value);
        if (Fachwerte.BUCHUNGSTEXT.equals(attributName)) buchungszeile.buchungstext = (String) value;
        if (Fachwerte.DATUM.equals(attributName)) buchungszeile.datum = (String) value;
        if (Fachwerte.HAUPTBUCHUNGSNR.equals(attributName)) buchungszeile.hauptbuchungsNr = (int) value;
        if (Fachwerte.KATEGORIE.equals(attributName)) buchungszeile.kategorie = (String) value;
        if (Fachwerte.KOMMENTAR.equals(attributName)) buchungszeile.kommentar = (String) value;
        if (Fachwerte.PBETRAG.equals(attributName)) buchungszeile.setPBetrag((Double) value);
        if (Fachwerte.PSALDO.equals(attributName)) buchungszeile.setPSaldo((Double) value);
        if (Fachwerte.SALDOGEGLAETTET.equals(attributName)) buchungszeile.setSaldoGeglaettet((Double) value);
        if (Fachwerte.QUELLEZIEL.equals(attributName)) buchungszeile.quelleZiel = (String) value;
        if (Fachwerte.SALDO.equals(attributName)) buchungszeile.setSaldo((Double) value);
        if (Fachwerte.UMBUCHUNGSNR.equals(attributName)) buchungszeile.umbuchungNr = (int) value;
        if (Fachwerte.VERWENDUNGSZWECK.equals(attributName)) buchungszeile.verwendungszweck = (String) value;
        if (Fachwerte.WAEHRUNG.equals(attributName)) buchungszeile.waehrung = (String) value;
    }

    @Override
    public boolean isRecordEmpty(Object record) {
        return false;
    }

    @Override
    public Object createEmptyRecord() {
        return new Buchungszeile();
    }
}

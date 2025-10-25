package de.amo.money;

import java.io.File;

public interface UmsatzReaderIfc {

    public String ermittleKontonummer(File file) throws Exception;

    /**
     * Liest alle Buchungssatz-Zeilen der Datei in Moneytransient.buchungszeilen ein
     *
     * @return die unter den eingelesenen Buchungszeilen im Buchungsablauf erste Buchungszeile
     */
    public Buchungszeile readUmsatzFile(File file, de.amo.money.MoneyTransient moneyTransient) throws Exception;

}

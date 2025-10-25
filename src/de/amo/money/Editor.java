package de.amo.money;

import de.amo.tools.IOTools;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 23.10.14
 * Time: 20:52
 * To change this template use File | Settings | File Templates.
 */
public class Editor {

    public boolean editiereBuchungsliste(List<Buchungszeile> buchungszeilen) {
        int max = buchungszeilen.size();

        for (int i = 0; i < buchungszeilen.size(); i++) {
            Buchungszeile buchungszeile = buchungszeilen.get(i);
            String nr = "    " + (i + 1);
            nr = nr.substring((nr.length()-3));
            System.out.println(nr + ") " + buchungszeile.toShow());
        }

        int zeilNr = IOTools.inputInt("Zu kommentierende Zeile", 0, max + 1);

        if (zeilNr == 0) {
            return false;
        }
        Buchungszeile zeile = buchungszeilen.get(zeilNr - 1);

        System.out.println(zeile.toShow());
        zeile.pbetrag = IOTools.inputInt("Neuer p-Betrag", -10000000, 10000000, zeile.pbetrag);
        zeile.kommentar = IOTools.input("Neuer Kommentar:");

        return true;
    }
}

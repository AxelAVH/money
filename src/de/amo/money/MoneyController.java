package de.amo.money;

import java.util.List;

/** Der MoneyController vermittelt das transiente Datenmodell (MoneyTransient) zwischen der View (MoneyView) und der Datenbank (MoneyDatabase)
 *
 * Created by amo on 24.08.2015.
 */
public class MoneyController {

    MoneyTransient moneyTr;
    MoneyView moneyView;
    MoneyDatabase moneyDatabase;

    public String lastMessage = "no message";

    public MoneyTransient getMoneyTr() {
        return moneyTr;
    }

    public MoneyController(MoneyTransient moneyTransient, MoneyDatabase moneyDatabase) {
        this.moneyTr = moneyTransient;
        this.moneyDatabase = moneyDatabase;
    }

    public MoneyDatabase getMoneyDatabase() {
        return moneyDatabase;
    }

    public String getMessage() {
        return lastMessage;
    }

    public boolean isSaved() {
        return moneyTr.isSaved();
    }

    public MoneyView getMoneyView() {
        return moneyView;
    }

    public void saveDatabase() {
        lastMessage = moneyDatabase.saveDatabase(moneyTr);
        moneyView.updateGui();
    }

    public void refreshView() {
        moneyView.updateGui();
    }


    public void createSplittbuchungen(Buchungszeile parent, int betrag, String kategorie, String kommentar) {
        List<Buchungszeile> splittbuchungen = parent.createSplittbuchungen(betrag, kategorie, kommentar);
        getMoneyTr().addUmbuchungszeilen(splittbuchungen.get(0), splittbuchungen.get(1));
    }
}
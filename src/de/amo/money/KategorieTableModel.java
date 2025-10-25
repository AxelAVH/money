package de.amo.money;

import de.amo.view.fachwerte.Fachwert;
import de.amo.view.table.ATableModel;

import java.util.List;

/**
 * Created by private on 19.01.2016.
 */
public class KategorieTableModel extends ATableModel {


    public KategorieTableModel(List<Fachwert> fachwerte) {
        super(fachwerte);
    }

    @Override
    public Object getValueAt(Object record, String attributname) {

        Kategorie kategorie = (Kategorie) record;

        if (Kategorie.CODE.equals(attributname)) return kategorie.getCode();
        if (Kategorie.BESCHREIBUNG.equals(attributname)) return kategorie.getBeschreibung();

        return "unbekanntes Attribut : " + attributname;
    }

    @Override
    public void setValueAt(Object value, Object record, String attributName) {

        Kategorie kategorie = (Kategorie) record;

        if (Kategorie.CODE.equals(attributName)) kategorie.setCode((String) value);
        if (Kategorie.BESCHREIBUNG.equals(attributName)) kategorie.setBeschreibung((String) value);
    }

    @Override
    public boolean isRecordEmpty(Object record) {
        Kategorie kategorie = (Kategorie) record;
        if (kategorie.getCode() != null && !"".equals(kategorie.getCode())) return false;
        if (kategorie.getBeschreibung() != null && !"".equals(kategorie.getBeschreibung())) return false;
        return true;
    }

    @Override
    public Object createEmptyRecord() {
        return new Kategorie();
    }
}

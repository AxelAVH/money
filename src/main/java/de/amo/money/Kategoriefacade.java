package de.amo.money;

import de.amo.view.ErrorMessageDialog;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by private on 12.01.2016.
 */
public class Kategoriefacade {

    private static Kategoriefacade instance;

    private File propertyFile;

    private Kategoriefacade() {
    }

    public static Kategoriefacade get() {
        return instance;
    }

    private List<Kategorie> kategorien;

    public List <Kategorie> getKategorien() {
        return kategorien;
    }

    public static void init(String kontoDir) throws IOException {

        instance = new Kategoriefacade();
        instance.propertyFile = new File(kontoDir,"Kategorie.property");
        instance.kategorien = new ArrayList<>();

        instance.loadKategorien();
    }

    private void loadAuslieferungsumfang() {
        kategorien.add(new Kategorie("AK", "Auto - Kraftstoff"));
        kategorien.add(new Kategorie("AR", "Auto - alles ausser Kraftstoff/Steuer/Versicherung"));
        kategorien.add(new Kategorie("AV", "Auto - Versicherung"));
        kategorien.add(new Kategorie("AS", "Auto - Steuern"));
        kategorien.add(new Kategorie("BE", "Bar - Entnahme"));
        kategorien.add(new Kategorie("ES", "Einkommen - Steuer"));
        kategorien.add(new Kategorie("EK", "Einkommen - alles rumd um dbh, auch Reisekosten und -Rückerstattungen"));
        kategorien.add(new Kategorie("FC", "Freizeit - Chor"));
        kategorien.add(new Kategorie("GK", "Gesundheit - Kosten"));
        kategorien.add(new Kategorie("GR", "Gesundheit - Rückerstattungen"));
        kategorien.add(new Kategorie("FU", "Freizeit - Urlaub/Kultur"));
        kategorien.add(new Kategorie("LT", "Leben - täglicher Bedarf"));
        kategorien.add(new Kategorie("LG", "Leben - Geschenke u. sonstige Anschaffungen"));
        kategorien.add(new Kategorie("LK", "Leben - Kleidung"));
        kategorien.add(new Kategorie("WM", "Wohnen - Miete"));
        kategorien.add(new Kategorie("WW", "Wohnen - Warm"));
        kategorien.add(new Kategorie("WT", "Wohnen - Telekommunikation"));
        kategorien.add(new Kategorie("VB", "Versicherung - Berufsunfähigkeit"));
        kategorien.add(new Kategorie("VR", "Versicherung - Rente"));
        kategorien.add(new Kategorie("UA", "Unterhalt - Aktivitäten/Geschenke Kinder"));
        kategorien.add(new Kategorie("UF", "Unterhalt - Frau"));
        kategorien.add(new Kategorie("UK", "Unterhalt - Kinder"));
        kategorien.add(new Kategorie("US", "Unterhalt - Sonstiges"));
        kategorien.add(new Kategorie("ZK", "Zuschüsse - Kleinsorge"));
        kategorien.add(new Kategorie("ZM", "Zuschüsse - Möller W."));
    }

    public List<String> getComboboxList() {
        SortedSet<String> set = new TreeSet<String>();

        for (Kategorie kategorie : kategorien) {
            set.add(kategorie.getCode() + " - " + kategorie.getBeschreibung());
        }

        ArrayList ret = new ArrayList();
        ret.addAll(set);
        return ret;
    }

    public String getKategorieFromComboboxString(String selectedItem) {
        if (selectedItem == null || "".equals(selectedItem)) {
            return null;
        }
        if (selectedItem.length() > 2) {
            selectedItem = selectedItem.substring(0, 2);
        }
        selectedItem = selectedItem.trim();
        return selectedItem;
    }

    public void loadKategorien() throws IOException {

        if (!propertyFile.exists()) {
            loadAuslieferungsumfang();
            return;
        }

        FileReader fr = new FileReader(propertyFile);

        Properties properties = new Properties();
        properties.load(fr);

        SortedMap<String, Kategorie> sm = new TreeMap();

        for (Map.Entry<Object, Object> propEntry : properties.entrySet()) {
            Kategorie kategorie = new Kategorie("" + propEntry.getKey(), "" + propEntry.getValue());
            sm.put("" + propEntry.getKey(), kategorie);
        }
        kategorien.clear();
        kategorien.addAll(sm.values());
    }

    public void saveKategorien() throws IOException {

        for (Kategorie kategorie : kategorien) {
            if (kategorie.getCode() != null && !kategorie.getCode().trim().equals("")) {
                if (kategorie.getCode().length() > 2) {
                    new ErrorMessageDialog("Speichern nicht ausgeführt","Kategorie-Code muss ein oder zwei Zeichen lang sein.",null);
                    return;
                }
            }
        }


        if (propertyFile.exists()) {
            propertyFile.delete();
        }

        FileWriter fw = new FileWriter(propertyFile);
        Properties properties = new Properties();
        for (Kategorie kategorie : kategorien) {
            if (kategorie.getCode() != null && !kategorie.getCode().trim().equals("")) {
                properties.put(kategorie.getCode(), kategorie.getBeschreibung());
            }
        }

        properties.store(fw, "");

        fw.close();
    }
}

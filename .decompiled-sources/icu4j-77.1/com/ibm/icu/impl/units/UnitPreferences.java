/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.units;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.UResource;
import com.ibm.icu.impl.units.ConversionRates;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.impl.units.SingleUnitImpl;
import com.ibm.icu.impl.units.UnitsData;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnitPreferences {
    private static final Map<String, String> measurementSystem;
    private HashMap<String, HashMap<String, UnitPreference[]>> mapToUnitPreferences = new HashMap();

    public UnitPreferences() {
        ICUResourceBundle resource = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "units");
        UnitPreferencesSink sink = new UnitPreferencesSink();
        resource.getAllItemsWithFallback("unitPreferenceData", sink);
        this.mapToUnitPreferences = sink.getMapToUnitPreferences();
    }

    public static String formMapKey(String category, String usage) {
        return category + "++" + usage;
    }

    private static String[] getAllUsages(String usage) {
        ArrayList<String> result = new ArrayList<String>();
        result.add(usage);
        for (int i = usage.length() - 1; i >= 0; --i) {
            if (usage.charAt(i) != '-') continue;
            result.add(usage.substring(0, i));
        }
        if (!usage.equals("default")) {
            result.add("default");
        }
        return result.toArray(new String[0]);
    }

    public UnitPreference[] getPreferencesFor(String category, String usage, ULocale locale, UnitsData data) {
        if ("temperature".equals(category)) {
            String localeUnitCategory;
            String localeUnit = locale.getKeywordValue("mu");
            if ("fahrenhe".equals(localeUnit)) {
                localeUnit = "fahrenheit";
            }
            try {
                localeUnitCategory = localeUnit == null ? null : data.getCategory(MeasureUnitImpl.forIdentifier(localeUnit));
            }
            catch (Exception e) {
                localeUnitCategory = null;
            }
            if (localeUnitCategory != null && category.equals(localeUnitCategory)) {
                UnitPreference[] preferences = new UnitPreference[]{new UnitPreference(localeUnit, null, null)};
                return preferences;
            }
        }
        String region = ULocale.getRegionForSupplementalData(locale, true);
        String localeSystem = locale.getKeywordValue("measure");
        boolean isLocaleSystem = measurementSystem.containsKey(localeSystem);
        String[] subUsages = UnitPreferences.getAllUsages(usage);
        UnitPreference[] result = null;
        for (String subUsage : subUsages) {
            result = this.getUnitPreferences(category, subUsage, region);
            if (result != null && isLocaleSystem) {
                ConversionRates rates = new ConversionRates();
                boolean unitsMatchSystem = true;
                for (UnitPreference unitPref : result) {
                    MeasureUnitImpl measureUnit = MeasureUnitImpl.forIdentifier(unitPref.getUnit());
                    ArrayList<SingleUnitImpl> singleUnits = new ArrayList<SingleUnitImpl>(measureUnit.getSingleUnits());
                    for (SingleUnitImpl singleUnit : singleUnits) {
                        String systems = rates.extractSystems(singleUnit);
                        if (systems.contains("metric_adjacent") || systems.contains(localeSystem)) continue;
                        unitsMatchSystem = false;
                    }
                }
                if (!unitsMatchSystem) {
                    String newRegion = measurementSystem.get(localeSystem);
                    result = this.getUnitPreferences(category, subUsage, newRegion);
                }
            }
            if (result != null) break;
        }
        assert (result != null) : "At least the category must be exist";
        return result;
    }

    private UnitPreference[] getUnitPreferences(String category, String usage, String region) {
        String key = UnitPreferences.formMapKey(category, usage);
        if (this.mapToUnitPreferences.containsKey(key)) {
            UnitPreference[] result;
            HashMap<String, UnitPreference[]> unitPreferencesMap = this.mapToUnitPreferences.get(key);
            UnitPreference[] unitPreferenceArray = result = unitPreferencesMap.containsKey(region) ? unitPreferencesMap.get(region) : unitPreferencesMap.get("001");
            assert (result != null);
            return result;
        }
        return null;
    }

    static {
        HashMap<String, String> tempMS = new HashMap<String, String>();
        tempMS.put("metric", "001");
        tempMS.put("ussystem", "US");
        tempMS.put("uksystem", "GB");
        measurementSystem = Collections.unmodifiableMap(tempMS);
    }

    public static class UnitPreferencesSink
    extends UResource.Sink {
        private HashMap<String, HashMap<String, UnitPreference[]>> mapToUnitPreferences = new HashMap();

        public HashMap<String, HashMap<String, UnitPreference[]>> getMapToUnitPreferences() {
            return this.mapToUnitPreferences;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            assert ("unitPreferenceData".equals(key.toString()));
            UResource.Table categoryTable = value.getTable();
            int i = 0;
            while (categoryTable.getKeyAndValue(i, key, value)) {
                assert (value.getType() == 2);
                String category = key.toString();
                UResource.Table usageTable = value.getTable();
                int j = 0;
                while (usageTable.getKeyAndValue(j, key, value)) {
                    assert (value.getType() == 2);
                    String usage = key.toString();
                    UResource.Table regionTable = value.getTable();
                    int k = 0;
                    while (regionTable.getKeyAndValue(k, key, value)) {
                        assert (value.getType() == 8);
                        String region = key.toString();
                        UResource.Array preferencesTable = value.getArray();
                        ArrayList<UnitPreference> unitPreferences = new ArrayList<UnitPreference>();
                        int l = 0;
                        while (preferencesTable.getValue(l, value)) {
                            assert (value.getType() == 2);
                            UResource.Table singlePrefTable = value.getTable();
                            String unit = null;
                            String geq = "1";
                            String skeleton = "";
                            int m = 0;
                            while (singlePrefTable.getKeyAndValue(m, key, value)) {
                                assert (value.getType() == 0);
                                String keyString = key.toString();
                                if ("unit".equals(keyString)) {
                                    unit = value.getString();
                                } else if ("geq".equals(keyString)) {
                                    geq = value.getString();
                                } else if ("skeleton".equals(keyString)) {
                                    skeleton = value.getString();
                                } else assert (false) : "key must be unit, geq or skeleton";
                                ++m;
                            }
                            assert (unit != null);
                            unitPreferences.add(new UnitPreference(unit, geq, skeleton));
                            ++l;
                        }
                        assert (!unitPreferences.isEmpty());
                        this.insertUnitPreferences(category, usage, region, unitPreferences.toArray(new UnitPreference[0]));
                        ++k;
                    }
                    ++j;
                }
                ++i;
            }
        }

        private void insertUnitPreferences(String category, String usage, String region, UnitPreference[] unitPreferences) {
            HashMap<Object, Object> shouldInsert;
            String key = UnitPreferences.formMapKey(category, usage);
            if (this.mapToUnitPreferences.containsKey(key)) {
                shouldInsert = this.mapToUnitPreferences.get(key);
            } else {
                shouldInsert = new HashMap();
                this.mapToUnitPreferences.put(key, shouldInsert);
            }
            shouldInsert.put(region, unitPreferences);
        }
    }

    public static class UnitPreference {
        private final String unit;
        private final BigDecimal geq;
        private final String skeleton;

        public UnitPreference(String unit, String geq, String skeleton) {
            this.unit = unit;
            this.geq = geq == null ? BigDecimal.valueOf(Double.MIN_VALUE) : new BigDecimal(geq);
            this.skeleton = skeleton == null ? "" : skeleton;
        }

        public String getUnit() {
            return this.unit;
        }

        public BigDecimal getGeq() {
            return this.geq;
        }

        public String getSkeleton() {
            return this.skeleton;
        }
    }
}


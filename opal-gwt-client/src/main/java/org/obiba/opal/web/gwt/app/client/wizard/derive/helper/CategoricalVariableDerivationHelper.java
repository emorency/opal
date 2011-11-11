/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.regexp.shared.RegExp;

/**
 *
 */
public class CategoricalVariableDerivationHelper extends DerivationHelper {

  private static final String MISSING_REGEXP = "^DNK$|^DK-NA$|^PNA$|^REFUSED$";

  private static final String NONE_REGEXP = "^NONE$|^NEVER$";

  private static final String YES_REGEXP = "^Y$|^YES$|^TRUE$";

  private static final String NO_REGEXP = "^N$|^NO$|^FALSE$";

  private static final String MALE_REGEXP = "^MALE$";

  private static final String FEMALE_REGEXP = "^FEMALE$";

  public CategoricalVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
    initializeValueMapEntries();
  }

  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();

    // recode non-missing values and identify missing values
    initializeCategoryValueMapEntries();

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  /**
   * For each category, make value map entry and process separately missing and non-missing ones.
   */
  private void initializeCategoryValueMapEntries() {
    List<ValueMapEntry> missingValueMapEntries = new ArrayList<ValueMapEntry>();
    int index = 1;

    for(CategoryDto cat : JsArrays.toIterable(originalVariable.getCategoriesArray())) {
      ValueMapEntry.Builder builder = ValueMapEntry.fromCategory(cat);

      if(estimateIsMissing(cat)) {
        builder.missing();
        missingValueMapEntries.add(builder.build());
      } else {
        index = initializeNonMissingCategoryValueMapEntry(index, cat, builder);
      }
    }

    // recode missing values
    initializeMissingCategoryValueMapEntries(missingValueMapEntries, index);
  }

  /**
   * Process non-missing categories.
   * @param missingValueMapEntries
   * @param index
   * @param cat
   * @param builder
   * @return current index value
   */
  @SuppressWarnings({ "unchecked", "PMD.NcssMethodCount" })
  private int initializeNonMissingCategoryValueMapEntry(int index, CategoryDto cat, ValueMapEntry.Builder builder) {
    int newIndex = index;
    if(RegExp.compile("^\\d+$").test(cat.getName())) {
      builder.newValue(cat.getName());
    } else if(RegExp.compile(NO_REGEXP + "|" + NONE_REGEXP, "i").test(cat.getName())) {
      builder.newValue("0");
    } else if(RegExp.compile(YES_REGEXP + "|" + MALE_REGEXP, "i").test(cat.getName())) {
      builder.newValue("1");
      if(index < 2) {
        newIndex = 2;
      }
    } else if(RegExp.compile(FEMALE_REGEXP, "i").test(cat.getName())) {
      builder.newValue("2");
      if(index < 3) {
        newIndex = 3;
      }
    } else {
      builder.newValue(Integer.toString(newIndex++));
    }

    valueMapEntries.add(builder.build());

    return newIndex;
  }

  /**
   * Recode each missing values with indexes like 8s and 9s.
   * @param missingValueMapEntries
   * @param indexMax
   */
  private void initializeMissingCategoryValueMapEntries(List<ValueMapEntry> missingValueMapEntries, int indexMax) {
    if(missingValueMapEntries.size() == 0) return;

    int missIndex = 10 - missingValueMapEntries.size();
    int factor = 1;
    while(missIndex * factor < indexMax + 1) {
      factor = factor * 10 + 1;
    }
    for(ValueMapEntry entry : missingValueMapEntries) {
      entry.setNewValue(Integer.toString(missIndex * factor));
      missIndex++;
    }

    valueMapEntries.addAll(missingValueMapEntries);
  }

  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable);

    Map<String, CategoryDto> newCategoriesMap = new LinkedHashMap<String, CategoryDto>();

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "').map({");

    appendCategoryValueMapEntries(scriptBuilder, newCategoriesMap);
    scriptBuilder.append("  }");
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getOtherValuesMapEntry());
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getEmptyValuesMapEntry());
    scriptBuilder.append(");");

    // new categories
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto cat : newCategoriesMap.values()) {
      cats.push(cat);
    }
    derived.setCategoriesArray(cats);

    // set script in derived variable
    setScript(derived, scriptBuilder.toString());

    return derived;
  }

  private void appendCategoryValueMapEntries(StringBuilder scriptBuilder, Map<String, CategoryDto> newCategoriesMap) {
    JsArray<CategoryDto> origCats = originalVariable.getCategoriesArray();

    for(int i = 0; i < origCats.length(); i++) {
      CategoryDto origCat = origCats.get(i);
      ValueMapEntry entry = getValueMapEntry(origCat.getName());

      if(entry.isType(ValueMapEntryType.CATEGORY_NAME)) {
        // script
        scriptBuilder.append("\n    '").append(entry.getValue()).append("': ");
        appendNewValue(scriptBuilder, entry);

        if(i < origCats.length() - 1) {
          scriptBuilder.append(",");
        } else {
          scriptBuilder.append("\n");
        }

        // new category
        addNewCategory(newCategoriesMap, origCat, entry);
      }
    }
  }

  private void addNewCategory(Map<String, CategoryDto> newCategoriesMap, CategoryDto origCat, ValueMapEntry entry) {
    if(!entry.getNewValue().isEmpty()) {
      CategoryDto cat = newCategoriesMap.get(entry.getNewValue());
      if(cat == null) {
        cat = newCategory(entry);
        cat.setAttributesArray(copyAttributes(origCat.getAttributesArray()));
        newCategoriesMap.put(cat.getName(), cat);
      } else {
        // merge attributes
        mergeAttributes(origCat.getAttributesArray(), cat.getAttributesArray());
      }
    }
  }

  private boolean estimateIsMissing(CategoryDto cat) {
    boolean missing = false;

    if(estimateIsMissing(cat.getName())) {
      missing = true;
    } else if(cat.hasIsMissing()) {
      missing = cat.getIsMissing();
    }

    return missing;
  }

  private boolean estimateIsMissing(String value) {
    if(value == null || value.isEmpty()) return true;
    return RegExp.compile(MISSING_REGEXP, "i").test(value);
  }
}
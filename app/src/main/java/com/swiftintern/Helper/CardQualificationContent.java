package com.swiftintern.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mukesh on 12/18/2015.
 */
public class CardQualificationContent {

    public List<DummyItem> ITEMS = new ArrayList<DummyItem>();
    public Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    public void addItem(DummyItem item ) {
        ITEMS.add(item);
        ITEM_MAP.put(item.status, item);
    }

    public void clear(){
        this.ITEMS.clear();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String organisation;
        public String category;
        public String location;
        public String stipend;
        public String status;

        public DummyItem(String org, String cat, String loc, String sti, String sta) {
            this.organisation = org;
            this.category = cat;
            this.location = loc;
            this.stipend = sti;
            this.status = sta;
        }

        @Override
        public String toString() {
            return organisation;
        }
    }
}

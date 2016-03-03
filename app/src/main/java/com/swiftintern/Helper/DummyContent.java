package com.swiftintern.Helper;

/**
 * Created by Mukesh on 12/8/2015.
 */
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public List<DummyItem> ITEMS = new ArrayList<DummyItem>();
    public Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    public void addItem(DummyItem item ) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public void clear(){
        this.ITEMS.clear();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;
        public Bitmap image;
        public String opp_id;
        public String org_id;

        public DummyItem(String id, String content, String opp_id, String org, Bitmap image) {
            this.id = id;
            this.content = content;
            this.image = image;
            this.org_id = org;
            this.opp_id = opp_id;
        }

        public void setBitmap(Bitmap B ){
            this.image = B;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}

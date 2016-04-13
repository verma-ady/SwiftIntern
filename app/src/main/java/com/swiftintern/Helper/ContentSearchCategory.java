package com.swiftintern.Helper;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mukesh on 4/14/2016.
 */
public class ContentSearchCategory {

    public List<ContentSearchCategory.DummyItem> ITEMS = new ArrayList<DummyItem>();

    public void addItem(ContentSearchCategory.DummyItem item ) {
        ITEMS.add(item);
    }

    public void clear(){
        this.ITEMS.clear();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String name;
        public Bitmap pic;

        public DummyItem(String vName, Bitmap vpic) {
            name = vName;
            pic = vpic;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}

package com.nolanlawson.japanesenamegenerator.v3.kanji;

import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;

/**
 * transforms roomaji strings using simple string replacement
 * @author nolan
 */

    public class ReplacementRoomajiTransformer implements RoomajiTransformer {

        private String replace;
        private String with;

        public ReplacementRoomajiTransformer(String replace, String with) {
            this.replace = replace;
            this.with = with;
        }

        public boolean appliesToString(String roomaji) {
            return roomaji.contains(replace);
        }

        public String apply(String roomaji) {
            return StringUtil.quickReplace(roomaji, replace, with);
        }

        @Override
        public String toString() {
            return "ReplacementTransformer:" + replace+"->" + with;
        }
    }


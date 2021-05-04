package air;

import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {

    public static final ElementDictionary DICTIONARY = StandardElementDictionary.getStandardElementDictionary();

    public static VR getVR(int tag){
        return DICTIONARY.vrOf(tag);

    }
    public static String getDescription(int tag) {
        return DICTIONARY.keywordOf(tag);

    }

    public static List<String> getTagList() {

        List<String> output = new ArrayList<>();
        Tag TagList = new Tag();
        Field[] tagFileds = TagList.getClass().getFields();
        for(Field tmpFileds: tagFileds)
            output.add(tmpFileds.getName());

        return output;
    }

    public static int getTagByDes(String des){



        return DICTIONARY.tagForKeyword(des);
    }

    public static String getTag2PutteyString(int tag){

        String tagString = String.format("%08X",tag);
        return  "("+tagString.substring(0,4)+", "+tagString.substring(4,8)+")";
    }


    public static String[][] getTagListAll() {

        Tag TagList = new Tag();
        Field[] tagFileds = TagList.getClass().getFields();

       String[][] output = new String[tagFileds.length-46][2];
       int idx = 0;
       for(int i=46; i<tagFileds.length;i++){

           output[idx][1] =tagFileds[i].getName();
           String tagString = String.format("%08X",DICTIONARY.tagForKeyword(tagFileds[i].getName()));
           output[idx][0] =  "("+tagString.substring(0,4)+", "+tagString.substring(4,8)+")";
           idx++;
       }



        return output;
    }

    public static List<String> getTagList(int len) {

        List<String> output = new ArrayList<>();
        Tag TagList = new Tag();
        Field[] tagFileds = TagList.getClass().getFields();
        int i=0;
        for(Field tmpFileds: tagFileds) {
            if(i++>=len)
                break;
            output.add(tmpFileds.getName());
        }
        return output;
    }



}

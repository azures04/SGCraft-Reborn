package fr.azures04.sgcraftreborn.client.models;

import com.google.gson.Gson;
import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.exceptions.MissingSMEGFile;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;

public class SMEGLoader {

    public static SMEGModel load(ResourceLocation location) {
        String file = String.format("/assets/%s/%s", location.getNamespace(), location.getPath());
        InputStream is = SGCraftReborn.class.getResourceAsStream(file);
        InputStreamReader reader = null;
        if (is == null) {
            throw new MissingSMEGFile("Model file is missing");
        }
        reader = new InputStreamReader(is);
        Gson gson = new Gson();
        return gson.fromJson(reader, SMEGModel.class);
    }

}

package dev.voxira.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String id, String name, boolean defaultValue) {
        super(id, name, defaultValue);
    }

    public void toggle() {
        value = !value;
    }

    @Override
    public String display() {
        return value ? "On" : "Off";
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        value = json.getAsBoolean();
    }
}

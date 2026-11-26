package dev.voxira.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

/** One choice out of a fixed list; clicking cycles to the next option. */
public class ModeSetting extends Setting<String> {
    private final List<String> options;

    public ModeSetting(String id, String name, String defaultValue, String... options) {
        super(id, name, defaultValue);
        this.options = List.of(options);
    }

    public void cycle(boolean forward) {
        int i = options.indexOf(value);
        int next = Math.floorMod(i + (forward ? 1 : -1), options.size());
        value = options.get(next);
    }

    public boolean is(String option) {
        return value.equals(option);
    }

    @Override
    public String display() {
        return value;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        String v = json.getAsString();
        if (options.contains(v)) {
            value = v;
        }
    }
}

package dev.voxira.client.module.setting;

import com.google.gson.JsonElement;

/** A configurable value of a module, shown in the settings screen and saved to the config. */
public abstract class Setting<T> {
    private final String id;
    private final String name;
    private final T defaultValue;
    protected T value;

    protected Setting(String id, String name, T defaultValue) {
        this.id = id;
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void reset() {
        this.value = defaultValue;
    }

    /** Value as shown in the UI. */
    public abstract String display();

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement json);
}

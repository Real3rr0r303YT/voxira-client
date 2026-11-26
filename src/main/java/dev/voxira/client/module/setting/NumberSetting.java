package dev.voxira.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final String unit;

    public NumberSetting(String id, String name, double defaultValue, double min, double max, double step, String unit) {
        super(id, name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.unit = unit;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    /** Position between 0 and 1, for sliders. */
    public double progress() {
        return (value - min) / (max - min);
    }

    public void setFromProgress(double progress) {
        set(min + Math.max(0, Math.min(1, progress)) * (max - min));
    }

    @Override
    public void set(Double v) {
        double snapped = Math.round((v - min) / step) * step + min;
        super.set(Math.max(min, Math.min(max, snapped)));
    }

    public int intValue() {
        return (int) Math.round(value);
    }

    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public String display() {
        String number = step >= 1 ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
        return unit.isEmpty() ? number : number + " " + unit;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        set(json.getAsDouble());
    }
}

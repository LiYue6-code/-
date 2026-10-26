package com.employment.entity;

/**
 * 统计条目：用于条形图/排行列表展示。percent 为相对最大值的百分比，便于前端画条形。
 */
public class StatItem {
    private String label;
    private String extra;
    private long amount;
    private double percent;

    public StatItem() {
    }

    public StatItem(String label, String extra, long amount) {
        this.label = label;
        this.extra = extra;
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}

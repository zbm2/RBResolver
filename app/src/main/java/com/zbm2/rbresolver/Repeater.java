package com.zbm2.rbresolver;
public class Repeater {
    private final long id;
    private final String call;
    private final String location;
    private final Double distance;
    private final String compassHeading;
    private final String serviceTxt;
    private final Double RX;
    private final Double TX;
    private final String Offset;
    private final Double CTCSS;
    private final Integer DCS;

    public Repeater(long id, String call, String location, Double distance, String compassHeading, String serviceTxt, Double RX, Double TX, String Offset, Double CTCSS, Integer DCS) {
        this.id = id;
        this.call = call;
        this.location = location;
        this.distance = distance;
        this.compassHeading = compassHeading;
        this.serviceTxt = serviceTxt;
        this.RX = RX;
        this.TX = TX;
        this.Offset = Offset;
        this.CTCSS = CTCSS;
        this.DCS = DCS;
    }

    public long getId() {
        return id;
    }
    public String getCall() {
        return call;
    }
    public String getLocation() { return location;}
    public Double getDistance() { return distance;}
    public String getCompassHeading() { return compassHeading;}
    public String getserviceTxt() { return serviceTxt;}
    public Double getRX() { return RX;}
    public Double getTX() { return TX;}
    public String getOffset() { return Offset;}
    public Double getCTCSS() { return CTCSS;}
    public Integer getDCS() { return DCS;}
}

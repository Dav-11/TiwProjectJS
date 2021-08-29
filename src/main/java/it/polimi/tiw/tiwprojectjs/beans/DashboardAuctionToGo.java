package it.polimi.tiw.tiwprojectjs.beans;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class DashboardAuctionToGo {

    private int id;
    private String description;
    private String name;
    private String picture; // base64 encoded
    private Date end_date;
    private Date start_date;
    private int id_user;
    private int item_id;
    private Boolean open;
    private float initial_price;
    private float min_rise;

    private Long hoursRemaining;
    private Long daysRemaining;
    private float winningBet;

    public DashboardAuctionToGo(DashboardAuction auction) {
        try {
            this.id = auction.getId();
            this.description = auction.getDescription();
            this.name = auction.getName();
            this.picture = auction.getBase64Picture();
            this.end_date = auction.getEnd_date();
            this.start_date = auction.getStart_date();
            this.id_user = auction.getId_user();
            this.item_id = auction.getItem_id();
            this.open = auction.isOpen();
            this.initial_price = auction.getInitial_price();
            this.min_rise = auction.getMin_rise();
            this.hoursRemaining = auction.getHoursRemaining();
            this.daysRemaining = auction.getDaysRemaining();
            this.winningBet = auction.getWinningBet();
        } catch (IOException e){
            e.printStackTrace();

        }
    }
}

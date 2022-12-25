package com.dotaherostatchecker;

public class HeroStatsDTO {

    private String match_id;
    private String kills;
    private String deaths;
    private boolean win;


    public HeroStatsDTO(String match_id, String kills, String deaths, boolean win){
        this.match_id = match_id;
        this.kills = kills;
        this.deaths = deaths;
        this.win = win;
    }

    public HeroStatsDTO(){

    }

    public String getMatch_id() {
        return match_id;
    }

    public void setMatch_id(String match_id) {
        this.match_id = match_id;
    }

    public String getKills() {
        return kills;
    }

    public void setKills(String kills) {
        this.kills = kills;
    }

    public String getDeaths() {
        return deaths;
    }

    public void setDeaths(String deaths) {
        this.deaths = deaths;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }
}

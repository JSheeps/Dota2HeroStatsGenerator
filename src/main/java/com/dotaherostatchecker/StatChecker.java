package com.dotaherostatchecker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class StatChecker {
    public static void main(String[] args) throws Exception
    {
        String userId = getUserId();
        Hero hero = getHeroId();
        String heroId = hero.getId();
        String heroName = hero.getLocalized_name();
        String pastDays = getPastDays();

        String killDeathURL = "https://api.opendota.com/api/players/" + userId + "/matches?hero_id=" + heroId + "&date=" + pastDays;
        String kdData = getHTML(killDeathURL);

        if(kdData.length() == 0){
            System.out.println("No Information Found for Hero with id: " + heroId + " in the past day(s): " + pastDays);
        }
        JSONArray kdArray = new JSONArray(kdData);

        ArrayList<HeroStatsDTO> stats = new ArrayList<HeroStatsDTO>();
        for(int i=0; i < kdArray.length(); i++){
            JSONObject kdObject = kdArray.getJSONObject(i);
            String matchURL = "https://api.opendota.com/api/matches/" + kdObject.getLong("match_id");
            String matchData = getHTML(matchURL);
            matchData = "[" + matchData + "]";
            JSONArray matchArray = new JSONArray(matchData);
            JSONObject matchObject = matchArray.getJSONObject(0);

            HeroStatsDTO stat = new HeroStatsDTO();
            stat.setKills(Integer.toString(kdObject.getInt("kills")));
            stat.setDeaths(Integer.toString(kdObject.getInt("deaths")));
            stat.setMatch_id(Long.toString(kdObject.getLong("match_id")));
            for(int j =0 ; j<matchObject.getJSONArray("players").length(); j++){
                JSONObject playerDataFromMatch = matchObject.getJSONArray("players").getJSONObject(j);
                Object account_id = playerDataFromMatch.get("account_id");
                if(!JSONObject.NULL.equals(account_id)){
                    if((int)account_id == Integer.parseInt(userId)){
                        int w = playerDataFromMatch.getInt("win");
                        boolean win = w != 0;
                        stat.setWin(win);
                        break;
                    }
                }

            }
            stats.add(stat);
        }

        for(HeroStatsDTO s : stats){
            String result = "Hero Name: " + heroName + " Deaths: "+ s.getDeaths() + " Kills: " + s.getKills() + " Match id: " +s.getMatch_id() + " Win: " + s.getWin();
            System.out.println(result);
        }

        convertToCSV(stats, heroName);

    }

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return result.toString();
    }



    public static void convertToCSV(ArrayList<HeroStatsDTO> stats, String heroName){
        File file = new File("./stats.csv");

        try {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);
            float totalKills = 0;
            float totalDeaths = 0;
            float totalWins = 0;
            float totalLosses = 0;

            // adding header to csv
            String[] header = { "Hero Name", "Match_Id", "Kills", "Deaths", "Win" };
            writer.writeNext(header);

            ArrayList<String[]> data = new ArrayList<String []>();
            for(HeroStatsDTO stat : stats){
                ArrayList<String> statList=  new ArrayList<String>();
                statList.add(heroName);
                statList.add(stat.getMatch_id());
                statList.add(stat.getKills());
                statList.add(stat.getDeaths());
                statList.add(stat.getWin().toString());
                data.add(statList.toArray(new String[0]));

                totalKills += Integer.parseInt(stat.getKills());
                totalDeaths += Integer.parseInt(stat.getDeaths());

                if(stat.getWin() == true){
                    totalWins++;
                }
                else{
                    totalLosses++;
                }
            }

            writer.writeAll(data);
            writer.writeNext(new String[0]);

            String[] totalStatsHeader = {"K/D", "W/L"};
            writer.writeNext(totalStatsHeader);

            if (totalLosses == 0){
                totalLosses = 1;
            }
            String[] totalStats = {Float.toString(totalKills/totalDeaths), Float.toString(totalWins/totalLosses)};
            writer.writeNext(totalStats);

            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getPastDays(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of days to get stats for hero: ");
        String numDays = scanner.nextLine();

        try{
            if(numDays == null || numDays.isEmpty() || Integer.parseInt(numDays) == 0){
                System.out.println("ERROR past days cannot be null or 0");
                return getPastDays();
            }
        } catch (NumberFormatException e){
            System.out.println("ERROR entered value: " + numDays + " is not a number");
            return getPastDays();
        }

        return numDays;
    }

    public static String getUserId(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter user ID from open dota: ");
        String userId = scanner.nextLine();

        if(userId == null || userId.isEmpty()){
            System.out.println("ERROR User ID cannot be null");
            getUserId();
        }

        return userId;
    }

    public static Hero getHeroId() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Hero ID or type help to see hero ids: ");
        String heroId = scanner.nextLine();
        ArrayList<Hero> heroes = new ArrayList<>();
        InputStream in = StatChecker.class.getResourceAsStream("/heroes.json");

        String json = new BufferedReader(new InputStreamReader(in))
                .lines().collect(Collectors.joining("\n"));
        ObjectMapper mapper = new ObjectMapper();
        heroes = (ArrayList<Hero>) mapper.readValue(json, new TypeReference<List<Hero>>() {});

        if(heroId.equals("help")){

            for(Hero hero : heroes){
                System.out.println(hero.getLocalized_name() + ": " + hero.getId());
            }
            return getHeroId();
        }
        try {
            int value = Integer.parseInt(heroId);
            if(value <=0 || value > 137){
                System.out.println("Hero ID cannot be less than 0 or greater than 120");
            }
        } catch (NumberFormatException e) {
            System.out.println("Hero ID cannot be parsed to Integer.");
            return getHeroId();
        }
        for(Hero hero : heroes){
            if(hero.getId().equals(heroId)){
                return hero;
            }
        }

        System.out.println("Hero Id not recognized please try again");
        return getHeroId();
    }
}
package com.sixthday.store.data;

public class GotWWWResponseBuilder {
    private String statusCode;
    private String statusDescription;
    private String latitude;
    private String longitude;
    private int noOfStores;


    public GotWWWResponseBuilder withStatusCodeDescription(String statusCode, String statusDescription) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
        return this;
    }

    public GotWWWResponseBuilder withLongitudeLatitude(String longitude, String latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    public GotWWWResponseBuilder withNoOfStores(int noOfStores) {
        this.noOfStores = noOfStores;
        return this;
    }

    public String build(){
        StringBuilder gotWWWResponse = new StringBuilder();
        gotWWWResponse.append(
                "<nm_geo_response>\n" +
                "    <status_code>" + statusCode + "</status_code>\n" +
                "    <status_desc>" + statusDescription + "</status_desc>\n" +
                "    <lookup>\n" +
                "       <starting_latitude>" + latitude + "</starting_latitude>\n" +
                "       <starting_longitude>" + longitude + "</starting_longitude>\n" +
                "    </lookup>\n"
        );
        if (noOfStores > 0) {
            gotWWWResponse.append(
                "    <stores>\n"
            );
            for (int storeNumber = 1; storeNumber <= noOfStores; storeNumber++) {
                gotWWWResponse.append(
                "        <store>\n" +
                "            <nbr>" + storeNumber + "</nbr>\n" +
                "        </store>\n"
                );
            }
            gotWWWResponse.append(
                "    </stores>\n"
            );
        }
        gotWWWResponse.append(
                "</nm_geo_response>"
        );
        return gotWWWResponse.toString();
    }
}

package ug.go.agriculture.MAAIF_Extension.farmer.weather;

public class Name {
    private String id;
    private String parish_id;
    private String latitude;
    private String longitude;
    private String forecast_date;
    private String maximum_temperature;
    private String minimum_temperature;
    private String average_temperature;
    private String temperature_units;
    private String rainfall_chance;
    private String rainfall_amount;
    private String rainfall_units;
    private String windspeed_average;
    private String windspeed_units;
    private String wind_direction;
    private String windspeed_maximum;
    private String windspeed_minimum;
    private String cloudcover;
    private String sunshine_level;
    private String soil_temperature;
    private String created_at;
    private String updated_at;
    private String icon;
    private String desc;


    public Name(String id, String parish_id,String latitude,String longitude,String forecast_date,String maximum_temperature,String minimum_temperature,String average_temperature,String temperature_units,
                String rainfall_chance,String rainfall_amount,String rainfall_units,String windspeed_average,String windspeed_units,String wind_direction,String windspeed_maximum,String windspeed_minimum,
                String cloudcover,String sunshine_level,String soil_temperature,String created_at,String updated_at, String icon, String desc
    ) {
        this.id = id;
        this.parish_id = parish_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.forecast_date = forecast_date;
        this.maximum_temperature = maximum_temperature;
        this.minimum_temperature = minimum_temperature;
        this.average_temperature = average_temperature;
        this.temperature_units = temperature_units;
        this.rainfall_chance = rainfall_chance;
        this.rainfall_amount = rainfall_amount;
        this.rainfall_units = rainfall_units;
        this.windspeed_average = windspeed_average;
        this.windspeed_units = windspeed_units;
        this.wind_direction = wind_direction;
        this.windspeed_maximum = windspeed_maximum;
        this.windspeed_minimum = windspeed_minimum;
        this.cloudcover = cloudcover;
        this.sunshine_level = sunshine_level;
        this.soil_temperature = soil_temperature;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.icon= icon;
        this.desc = desc;


    }

    public String getId() {
        return id;
    }

    public String getParish_id() {
        return parish_id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getForecast_date() {
        return forecast_date;
    }

    public String getMaximum_temperature() {
        return maximum_temperature;
    }

    public String getMinimum_temperature() {
        return minimum_temperature;
    }

    public String getAverage_temperature() {
        return average_temperature;
    }

    public String getTemperature_units() {
        return temperature_units;
    }

    public String getRainfall_chance() {
        return rainfall_chance;
    }

    public String getRainfall_amount() {
        return rainfall_amount;
    }

    public String getRainfall_units() {
        return rainfall_units;
    }

    public String getWindspeed_average() {
        return windspeed_average;
    }

    public String getWindspeed_units() {
        return windspeed_units;
    }

    public String getWind_direction() {
        return wind_direction;
    }

    public String getWindspeed_maximum() {
        return windspeed_maximum;
    }

    public String getWindspeed_minimum() {
        return windspeed_minimum;
    }

    public String getCloudcover() {
        return cloudcover;
    }

    public String getSunshine_level() {
        return sunshine_level;
    }

    public String getSoil_temperature() {
        return soil_temperature;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }
    public String getIcon() {
        return icon;
    }
    public String getDesc() {
        return desc;
    }
}
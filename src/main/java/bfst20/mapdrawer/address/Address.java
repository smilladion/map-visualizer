package bfst20.mapdrawer.address;

import java.util.regex.Pattern;

public class Address {

    private static final Pattern ADDRESS_REGEX = Pattern.compile(
        " *(?<street>[\\p{L} ]*?) +" +
            "(?<house>[0-9]+) *" +
            "(?<floor>[\\p{L}0-9]*) *" +
            "(?<side>[\\p{L}0-9 ]*?) *" +
            "(?<postcode>[0-9]*) *" +
            "(?<city>[\\p{L}]*)$"
    );

    public final String street;
    public final String city;
    public final String postcode;
    public final String side;
    public final String floor;
    public final String house;

    public Address(String street, String city, String postcode, String side, String floor, String house) {
        this.street = street;
        this.city = city;
        this.postcode = postcode;
        this.side = side;
        this.floor = floor;
        this.house = house;
    }

    public static Address fromString(String input) {
        // Replaces "sal" with nothing
        input = input.replace(" sal ", "");

        // Replaces special characters with spaces [.,]
        input = input.replace(',', ' ');
        input = input.replace('.', ' ');

        // Replaces several space characters with one space
        input = input.replaceAll("\\s{2,}", " ");

        // Removes spaces in front of or after a string
        input = input.trim();

        var matcher = ADDRESS_REGEX.matcher(input);
        if (matcher.matches()) {
            return new Address(
                matcher.group("street"),
                matcher.group("house"),
                matcher.group("floor"),
                matcher.group("side"),
                matcher.group("postcode"),
                matcher.group("city")
            );
        } else {
            throw new IllegalArgumentException("Cannot parse: " + input);
        }
    }

    public String toString() {
        return street + " " + house + ", " + floor + " " + side + "\n" + postcode + " " + city;
    }
}

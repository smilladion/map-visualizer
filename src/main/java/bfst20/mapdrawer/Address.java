package bfst20.mapdrawer;

import java.util.regex.Pattern;

public class Address {
    public final String street, house, floor, side, postcode, city;

    private Address(String street, String house, String floor, String side, String postcode, String city) {
        this.street = street;
        this.house = house;
        this.floor = floor;
        this.side = side;
        this.postcode = postcode;
        this.city = city;
    }

    public String toString() {
        return street + " " + house + ", " + floor + " " + side + "\n" + postcode + " " + city;
    }

    static String regex = " *(?<street>[\\p{L} ]*?) +(?<house>[0-9]+) *(?<floor>[\\p{L}0-9]*) *(?<side>[\\p{L}0-9 ]*?) *(?<postcode>[0-9]*) *(?<city>[\\p{L}]*)$";
    static Pattern pattern = Pattern.compile(regex);

    public static Address parse(String input) {
        // Erstatter "sal" med ingenting
        input = input.replace(" sal ", "");

        // Erstatter specielle karakterer med mellemrum [.,]
        input = input.replace(',', ' ');
        input = input.replace('.', ' ');

        // Erstatter flere mellemrum med ét mellemrum
        input = input.replaceAll("\\s{2,}", " ");

        // Fjerner mellemrum i starten/slutningen af et string
        input = input.trim();

        var matcher = pattern.matcher(input);
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
}

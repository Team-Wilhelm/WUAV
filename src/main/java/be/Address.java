package be;

public class Address {
    private int addressID;
    private String streetName, streetNumber, postcode, town, country;

    public Address(String streetName, String streetNumber, String postcode, String town, String country) {
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.postcode = postcode;
        this.town = town;
        this.country = country;
    }

    public Address(int addressID, String streetName, String streetNumber, String postcode, String town, String country) {
        this(streetName, streetNumber, postcode, town, country);
        this.addressID = addressID;
    }

    public int getAddressID() {
        return addressID;
    }

    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

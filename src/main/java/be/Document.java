package be;

import javafx.scene.image.Image;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Document {
    private UUID documentID;
    private Customer customer;
    private Date dateOfCreation;
    private List<User> technicians;
    private List<Image> electricalDrawings, sitePhotos;
    private String jobTitle, jobDescription, optionalNotes;

    public Document (){
        this.technicians = new ArrayList<>();
        this.electricalDrawings = new ArrayList<>();
        this.sitePhotos = new ArrayList<>();
    }

    public Document(Customer customer, String jobTitle, Date dateOfCreation){
        this.customer = customer;
        this.jobTitle = jobTitle;
        this.dateOfCreation = dateOfCreation;
    }

    public Document(Customer customer, String jobTitle, String jobDescription, String optionalNotes, Date dateOfCreation) {
        this();
        this.customer = customer;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.optionalNotes = optionalNotes;
        this.dateOfCreation = dateOfCreation;
    }

    public Document(UUID documentID, Customer customer, String jobTitle, String jobDescription, String optionalNotes, Date dateOfCreation) {
       this(customer, jobTitle, jobDescription, optionalNotes, dateOfCreation);
         this.documentID = documentID;
    }

    public UUID getDocumentID() {
        return documentID;
    }

    public void setDocumentID(UUID documentID) {
        this.documentID = documentID;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public List<User> getTechnicians() {
        return technicians;
    }

    public void setTechnicians(List<User> technicians) {
        this.technicians = technicians;
    }

    public List<Image> getElectricalDrawings() {
        return electricalDrawings;
    }

    public void setElectricalDrawings(List<Image> electricalDrawings) {
        this.electricalDrawings = electricalDrawings;
    }

    public List<Image> getSitePhotos() {
        return sitePhotos;
    }

    public void setSitePhotos(List<Image> sitePhotos) {
        this.sitePhotos = sitePhotos;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getOptionalNotes() {
        return optionalNotes;
    }

    public void setOptionalNotes(String optionalNotes) {
        this.optionalNotes = optionalNotes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        return documentID.equals(document.documentID);
    }

    @Override
    public int hashCode() {
        return documentID.hashCode();
    }
}
